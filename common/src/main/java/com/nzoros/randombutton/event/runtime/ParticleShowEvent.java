package com.nzoros.randombutton.event.runtime;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.UUID;

public final class ParticleShowEvent implements ActiveEvent {
    private final UUID playerId;
    private int ticks;

    public ParticleShowEvent(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public boolean tick(MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null || ticks++ >= 80) return true;

        if (ticks % 4 == 0) {
            var level = player.serverLevel();
            level.sendParticles(ParticleTypes.FIREWORK,
                player.getX(), player.getY() + 2.0, player.getZ(),
                18, 2.5, 1.5, 2.5, 0.08);
        }
        if (ticks % 20 == 0) {
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 0.7F, 1.1F);
        }
        return false;
    }
}
