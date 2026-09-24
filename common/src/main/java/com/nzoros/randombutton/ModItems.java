package com.nzoros.randombutton;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.nzoros.randombutton.MyMod.platform;

public class ModItems {

    public static List<Holder<Item>> ALL_ITEMS = new ArrayList<>();
    public static final Holder<Item> RECALL_SIGIL = register("recall_sigil",
        () -> new LobbyReturnItem(new Item.Properties().stacksTo(1).fireResistant()));
    public static final Holder<Item> LUCKY_CUBE = register("lucky_cube",
        () -> new CubeItem(CubeState.Mode.LUCKY, new Item.Properties()));
    public static final Holder<Item> UNLUCKY_CUBE = register("unlucky_cube",
        () -> new CubeItem(CubeState.Mode.UNLUCKY, new Item.Properties()));

    private static Holder<Item> register(String id, Supplier<Item> supplier) {
        Holder<Item> item = platform.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, id), supplier);
        ALL_ITEMS.add(item);
        return item;
    }

    public static void init() { }
}
