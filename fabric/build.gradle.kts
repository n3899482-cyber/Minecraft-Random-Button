@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask

plugins {
    alias(libs.plugins.fabricloom)
}

// Put a repositories block here for fabric-only dependencies that do not use modrinth maven.

val targetMinecraftVersion = rootProject.providers.gradleProperty("minecraft_version").get()
val parchmentVersion = libs.versions.parchment.get()

dependencies {
    minecraft("com.mojang:minecraft:$targetMinecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$targetMinecraftVersion:$parchmentVersion@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
    // This line can be removed if you don't need fabric api
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"]}")

    implementation(project.project(":common").sourceSets.getByName("main").output)

    // Add fabric-only dependencies here.
}

loom {
    mixin.useLegacyMixinAp = false

    runs {
        val vmArgs = arrayOf(
            "-XX:+UseZGC",
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-XX:+AllowEnhancedClassRedefinition",
            "-Xms500M",
            "-Xmx2G"
        )
        named("client") {
            client()
            runDir("../run/client/$targetMinecraftVersion")
            configName = "Fabric/Client"
            ideConfigGenerated(true)
            vmArgs(*vmArgs)
        }
        named("server") {
            server()
            serverWithGui()
            runDir("../run/server/$targetMinecraftVersion")
            configName = "Fabric/Server"
            ideConfigGenerated(true)
            vmArgs(*vmArgs)
        }
    }

    // include access wideners from common
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks {
    withType<JavaCompile> {
        // include common code in compiled jar
        source(project(":common").sourceSets.main.get().allSource)
    }

    // put all artifacts in the right directory
    withType<Jar> {
        destinationDirectory = rootDir.resolve("build").resolve("libs_fabric")
    }
    withType<RemapJarTask> {
        destinationDirectory = rootDir.resolve("build").resolve("libs_fabric")
    }

    // add common javadoc to jar
    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        // add common resources to jar
        from(project(":common").sourceSets.main.get().resources)

        // the properties listed here can be used in the fabric.mod.json
        val properties = listOf(
            "minecraft_version",
            "fabric_loader_version",
            "mod_version",
            "mod_id",
            "mod_name",
            "mod_description",
            "mod_authors",
            "mod_license"
        )

        val map = mutableMapOf<String, String>()
        properties.forEach { map[it] = rootProject.properties[it].toString() }
        inputs.property("property_map", map)

        filesMatching("fabric.mod.json") {
            @Suppress("UNCHECKED_CAST") expand(inputs.properties["property_map"] as Map<String, String>)
        }
    }

    named("compileTestJava").configure {
        enabled = false
    }

    named("test").configure {
        enabled = false
    }
}
