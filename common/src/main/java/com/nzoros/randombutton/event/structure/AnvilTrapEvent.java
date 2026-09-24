package com.nzoros.randombutton.event.structure;

import com.nzoros.randombutton.event.runtime.ActiveEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

public final class AnvilTrapEvent implements ActiveEvent {
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final BlockPos center;
    private int age;

    public AnvilTrapEvent(UUID playerId, ResourceKey<Level> dimension, BlockPos center) {
        this.playerId = playerId;
        this.dimension = dimension;
        this.center = center.immutable();
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public boolean tick(MinecraftServer server) {
        var level = server.getLevel(dimension);
        var player = server.getPlayerList().getPlayer(playerId);
        if (level == null || player == null || !player.isAlive()) return true;

        if (age == 0) {
            level.playSound(null, center, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.4F, 0.5F);
            level.sendParticles(ParticleTypes.DUST_PLUME, center.getX() + 0.5, center.getY() + 1.0,
                center.getZ() + 0.5, 35, 2.0, 0.2, 2.0, 0.03);
        }
        if (age == 25) {
            dropAnvil(level, center.offset(0, 8, 0));
            dropAnvil(level, center.offset(2, 9, 0));
            dropAnvil(level, center.offset(-2, 10, 1));
        }
        age++;
        return age > 100;
    }

    private static void dropAnvil(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        FallingBlockEntity.fall(level, pos, Blocks.ANVIL.defaultBlockState());
    }
}
