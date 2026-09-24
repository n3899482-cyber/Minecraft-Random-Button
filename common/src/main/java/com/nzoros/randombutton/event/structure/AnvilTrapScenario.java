package com.nzoros.randombutton.event.structure;

import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public final class AnvilTrapScenario implements StructureScenario {
    @Override
    public void start(EventContext context, BlockPos center) {
        buildRuinedTrap(context, center);
        context.player().teleportTo(context.level(), center.getX() + 0.5, center.getY() + 1.0,
            center.getZ() + 0.5, context.player().getYRot(), 0.0F);
        EventRuntime.start(new AnvilTrapEvent(context.player().getUUID(), context.level().dimension(), center));
    }

    private static void buildRuinedTrap(EventContext context, BlockPos center) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos floor = center.offset(x, 0, z);
                int surfaceY = context.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    floor.getX(), floor.getZ());
                for (int y = surfaceY; y < center.getY(); y++) {
                    context.level().setBlock(new BlockPos(floor.getX(), y, floor.getZ()),
                        Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                }
                context.level().setBlock(floor, (Math.abs(x) == 3 || Math.abs(z) == 3
                    ? Blocks.CRACKED_DEEPSLATE_BRICKS : Blocks.DEEPSLATE_TILES).defaultBlockState(), 3);
                for (int y = 1; y <= 6; y++) {
                    context.level().setBlock(floor.above(y), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int y = 1; y <= 3; y++) {
            for (int i = -3; i <= 3; i++) {
                if (i != 0 || y == 3) {
                    context.level().setBlock(center.offset(i, y, -3), Blocks.IRON_BARS.defaultBlockState(), 3);
                    context.level().setBlock(center.offset(i, y, 3), Blocks.IRON_BARS.defaultBlockState(), 3);
                    context.level().setBlock(center.offset(-3, y, i), Blocks.IRON_BARS.defaultBlockState(), 3);
                    context.level().setBlock(center.offset(3, y, i), Blocks.IRON_BARS.defaultBlockState(), 3);
                }
            }
        }

        for (int x : new int[]{-3, 3}) {
            for (int z : new int[]{-3, 3}) {
                for (int y = 1; y <= 4; y++) {
                    context.level().setBlock(center.offset(x, y, z),
                        Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 3);
                }
            }
        }

        BlockPos barrelPos = center.offset(2, 1, 2);
        context.level().setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), 3);
        if (context.level().getBlockEntity(barrelPos) instanceof Container container) {
            container.setItem(0, new ItemStack(Items.GOLDEN_CARROT, 3));
            container.setItem(4, new ItemStack(Items.IRON_INGOT, 4));
            container.setItem(8, new ItemStack(Items.ARROW, 8));
        }
    }
}
