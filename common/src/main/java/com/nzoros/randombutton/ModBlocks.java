package com.nzoros.randombutton;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static com.nzoros.randombutton.MyMod.platform;

public class ModBlocks {

    public static final Holder<Block> RANDOM_BUTTON = registerWithItem(
        "random_button",
        () -> new RandomButtonBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK)
            .strength(3.0F, 6.0F)
            .noOcclusion()
            .lightLevel(state -> state.getValue(RandomButtonBlock.ACTIVATED) ? 10 : 5))
    );


    private static Holder<Block> register(String id, Supplier<Block> bl) {
        return platform.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, id), bl);
    }

    private static Holder<Block> registerWithItem(String id, Supplier<Block> supplier) {
        var rl = ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, id);
        var block = register(id, supplier);
        ModItems.ALL_ITEMS.add(
            platform.register(BuiltInRegistries.ITEM, rl, () -> new BlockItem(block.value(), new Item.Properties()))
        );
        return block;
    }

    public static void init() { }
}
