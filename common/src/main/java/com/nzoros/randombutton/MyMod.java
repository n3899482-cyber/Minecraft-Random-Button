package com.nzoros.randombutton;

import com.nzoros.randombutton.event.RandomEventRegistry;

public class MyMod {
    public static final String MOD_ID = "randombutton";

    public static Platform platform;

    static void init(Platform platform) {
        // Your common initialisation code here
        System.out.println("Initializing Minecraft Random Button");

        MyMod.platform = platform;

        ModBlocks.init();
        ModItems.init();
        ModCreativeTab.init();
        RandomEventRegistry.init();
    }
}
