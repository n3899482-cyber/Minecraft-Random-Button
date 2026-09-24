plugins {
    alias(libs.plugins.moddevgradle)
}

// Put a repositories block here for neoforge-only dependencies that do not use modrinth maven.

dependencies {
    implementation(project.project(":common").sourceSets.getByName("main").output)

    // Add neoforge-only dependencies here.
}

val targetMinecraftVersion = rootProject.providers.gradleProperty("minecraft_version").get()
val parchmentVersion = libs.versions.parchment.get()

neoForge {
    version = rootProject.properties["neoforge_version"].toString()

    parchment {
        minecraftVersion = targetMinecraftVersion
        mappingsVersion = parchmentVersion
    }

    runs {
        val vmArgs = arrayOf(
            "-XX:+UseZGC",
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-XX:+AllowEnhancedClassRedefinition",
            "-Xms500M",
            "-Xmx2G",
        )
        create("Client") {
            client()
            gameDirectory = rootProject.file("run/client/$targetMinecraftVersion")
            ideName = "NeoForge/Client" // match fabric names
            jvmArguments.addAll(*vmArgs)
        }
        create("Server") {
            server()
            gameDirectory = rootProject.file("run/server/$targetMinecraftVersion")
            ideName = "NeoForge/Server" // match fabric names
            jvmArguments.addAll(*vmArgs)
        }
        create("Data") {
            data()
            gameDirectory = rootProject.file("run/data/$targetMinecraftVersion")
            ideName = "NeoForge/Data" // match fabric names
            jvmArguments.addAll(*vmArgs)
        }
    }

    mods {
        create(rootProject.properties["mod_id"].toString()) {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks {
    // IntelliJ's "Run main()" action creates this temporary JavaExec task after
    // project evaluation. Ensure ModDevGradle has regenerated its argument files,
    // especially after `clean`, before that task starts.
    matching { it.name == "net.neoforged.devlaunch.Main.main()" }.configureEach {
        dependsOn("prepareClientRun")
    }

    jar {
        // add common code to jar
        val main = project.project(":common").sourceSets.main.get()
        from(main.output.classesDirs)
        from(main.output.resourcesDir)
    }

    named("compileTestJava").configure {
        enabled = false
    }

    // NeoGradle compiles the game, but we don't want to add our common code to the game's code
    val notNeoTask: (Task) -> Boolean = { !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

    // add common code & Javadoc to built jars (except for NeoGradle jars)
    withType<JavaCompile>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }
    withType<Javadoc>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<ProcessResources>().matching(notNeoTask).configureEach {
        // include common resources
        from(project(":common").sourceSets.main.get().resources)

        // the properties listed here can be used in the mods.toml
        val properties = listOf(
            "minecraft_version",
            "neoforge_version_range",
            "neo_loader_version_range",
            "mod_version",
            "mod_id",
            "mod_name",
            "mod_description",
            "mod_authors",
            "mod_license"
        )

        // store a map of the properties so the configuration cache can be used
        val map = mutableMapOf<String, String>()
        properties.forEach { map[it] = rootProject.properties[it].toString() }
        inputs.property("property_map", map)

        filesMatching("META-INF/neoforge.mods.toml") {
            @Suppress("UNCHECKED_CAST") expand(inputs.properties["property_map"] as Map<String, String>)
        }
    }
}
