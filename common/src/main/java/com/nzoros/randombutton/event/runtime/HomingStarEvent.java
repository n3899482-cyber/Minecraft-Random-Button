package com.nzoros.randombutton.event.runtime;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class HomingStarEvent implements ActiveEvent {
    private static final double SPEED = 0.48;
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private Vec3 position;
    private Vec3 velocity;
    private int ticks;

    public HomingStarEvent(ServerPlayer player) {
        playerId = player.getUUID();
        dimension = player.level().dimension();
        position = player.position().add(8.0, 3.0, 0.0);
        velocity = new Vec3(-0.4, -0.12, 0.0);
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public boolean tick(MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null || !player.level().dimension().equals(dimension) || !player.isAlive() || ticks++ >= 120) {
            return true;
        }

        Vec3 target = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 toTarget = target.subtract(position);
        if (toTarget.lengthSqr() > 0.0001) {
            Vec3 desired = toTarget.normalize().scale(SPEED);
            velocity = velocity.scale(0.78).add(desired.scale(0.22)).normalize().scale(SPEED);
        }

        Vec3 previous = position;
        Vec3 next = position.add(velocity);
        Vec3 segment = next.subtract(previous);
        double progress = segment.lengthSqr() > 0.0
            ? Math.max(0.0, Math.min(1.0, target.subtract(previous).dot(segment) / segment.lengthSqr()))
            : 0.0;
        Vec3 closest = previous.add(segment.scale(progress));
        if (closest.distanceToSqr(target) < 0.85 * 0.85) {
            hit(player, closest);
            return true;
        }

        position = next;
        player.serverLevel().sendParticles(ParticleTypes.GUST, position.x, position.y, position.z,
            1, 0.05, 0.05, 0.05, 0.0);
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, position.x, position.y, position.z,
            2, 0.12, 0.12, 0.12, 0.005);
        if (ticks % 8 == 0) {
            player.serverLevel().playSound(null, position.x, position.y, position.z,
                SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.25F, 1.5F);
        }
        return false;
    }

    private void hit(ServerPlayer player, Vec3 impact) {
        Vec3 horizontal = new Vec3(player.getX() - impact.x, 0.0, player.getZ() - impact.z);
        if (horizontal.lengthSqr() < 0.01) horizontal = new Vec3(velocity.x, 0.0, velocity.z);
        if (horizontal.lengthSqr() < 0.01) horizontal = new Vec3(1.0, 0.0, 0.0);
        horizontal = horizontal.normalize();

        player.hurt(player.damageSources().magic(), 3.0F);
        player.push(horizontal.x * 1.45, 0.75, horizontal.z * 1.45);
        player.hurtMarked = true;
        player.serverLevel().sendParticles(ParticleTypes.GUST, impact.x, impact.y, impact.z,
            8, 0.45, 0.45, 0.45, 0.04);
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, impact.x, impact.y, impact.z,
            16, 0.45, 0.45, 0.45, 0.09);
        player.serverLevel().playSound(null, impact.x, impact.y, impact.z,
            SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.randombutton.homing_wind_complete"), true);
    }
}
