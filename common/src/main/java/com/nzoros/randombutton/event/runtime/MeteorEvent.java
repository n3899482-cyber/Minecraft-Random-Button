package com.nzoros.randombutton.event.runtime;

import com.nzoros.randombutton.lobby.LobbyProtectionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class MeteorEvent implements ActiveEvent {
    private static final int FLIGHT_TICKS = 28;
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final BlockPos landing;
    private final int radius;
    private int age;

    public MeteorEvent(UUID playerId, ResourceKey<Level> dimension, BlockPos landing, int diameter) {
        this.playerId = playerId;
        this.dimension = dimension;
        this.landing = landing.immutable();
        this.radius = diameter / 2;
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public boolean tick(MinecraftServer server) {
        ServerLevel level = server.getLevel(dimension);
        if (level == null || server.getPlayerList().getPlayer(playerId) == null) return true;

        double x = landing.getX() + 0.5;
        double z = landing.getZ() + 0.5;
        double y = landing.getY() + radius + 0.5 + (FLIGHT_TICKS - age) * 0.7;
        if (age < FLIGHT_TICKS) {
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 8 + radius * 5,
                0.25 + radius * 0.35, 0.25 + radius * 0.35, 0.25 + radius * 0.35, 0.02);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 4 + radius * 3,
                0.2 + radius * 0.3, 0.3, 0.2 + radius * 0.3, 0.01);
            if (age % 7 == 0) {
                level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS,
                    0.6F, 0.65F);
            }
            age++;
            return false;
        }

        placeMeteor(level);
        level.sendParticles(ParticleTypes.EXPLOSION, x, landing.getY() + 1.0, z,
            4 + radius * 5, 0.5 + radius, 0.4 + radius * 0.4, 0.5 + radius, 0.0);
        level.sendParticles(ParticleTypes.DUST_PLUME, x, landing.getY() + 0.5, z,
            20 + radius * 15, 0.7 + radius, 0.3, 0.7 + radius, 0.04);
        level.playSound(null, landing, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS,
            0.8F + radius * 0.25F, 0.85F);
        return true;
    }

    private void placeMeteor(ServerLevel level) {
        BlockPos center = landing.above(radius);
        double limit = (radius + 0.5) * (radius + 0.5);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > limit) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (LobbyProtectionManager.isProtected(level, pos) || level.getBlockEntity(pos) != null) continue;
                    BlockState stone = surface(level, dx, dy, dz);
                    level.setBlock(pos, stone, 3);
                }
            }
        }
    }

    private BlockState surface(ServerLevel level, int dx, int dy, int dz) {
        int roll = level.random.nextInt(12);
        if (roll == 0 && (dx != 0 || dy != 0 || dz != 0)) return Blocks.MAGMA_BLOCK.defaultBlockState();
        if (roll < 3) return Blocks.OBSIDIAN.defaultBlockState();
        if (roll < 6) return Blocks.BLACKSTONE.defaultBlockState();
        return Blocks.BASALT.defaultBlockState();
    }
}
