package com.nzoros.randombutton;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static com.nzoros.randombutton.MyMod.platform;

public class ModCreativeTab {

    public static final Holder<CreativeModeTab> RANDOM_BUTTON_TAB = platform.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "random_button"),
        () -> platform.creativeTabBuilder()
            .icon(() -> new ItemStack(ModBlocks.RANDOM_BUTTON.value()))
            .title(Component.translatable("itemGroup.randombutton"))
            .displayItems(((itemDisplayParameters, output) -> {
                for (var item : ModItems.ALL_ITEMS) output.accept(item.value());
            })).build()
    );

    public static void init() { }
}
