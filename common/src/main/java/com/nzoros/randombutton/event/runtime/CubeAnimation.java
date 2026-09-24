package com.nzoros.randombutton.event.runtime;

import com.mojang.math.Transformation;
import com.nzoros.randombutton.CubeState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public final class CubeAnimation implements ActiveEvent {
    private static final int DURATION = 24;
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final ServerLevel level;
    private final Display.ItemDisplay display;
    private final Vec3 origin;
    private final CubeState.Mode mode;
    private int age;

    public CubeAnimation(ServerPlayer player, ItemStack stack, CubeState.Mode mode) {
        this.playerId = player.getUUID();
        this.dimension = player.level().dimension();
        this.level = player.serverLevel();
        this.mode = mode;
        this.origin = player.getEyePosition().add(player.getLookAngle().scale(1.3)).add(0.0, -0.3, 0.0);
        this.display = EntityType.ITEM_DISPLAY.create(level);
        if (display != null) {
            display.getSlot(0).set(stack);
            display.moveTo(origin.x, origin.y, origin.z);
            CompoundTag data = display.saveWithoutId(new CompoundTag());
            data.putString("item_display", "fixed");
            data.putInt(Display.TAG_TRANSFORMATION_INTERPOLATION_DURATION, 2);
            display.load(data);
            level.addFreshEntity(display);
            level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.65F, mode == CubeState.Mode.LUCKY ? 1.4F : 0.75F);
        }
    }

    @Override
    public UUID playerId() { return playerId; }

    @Override
    public boolean tick(MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (display == null || player == null || !player.isAlive()
            || !player.level().dimension().equals(dimension)) {
            if (display != null) display.discard();
            return true;
        }
        age++;
        float progress = age / (float) DURATION;
        float scale = progress < 0.7F ? 1.0F : Math.max(0.0F, (1.0F - progress) / 0.3F);
        Transformation transform = new Transformation(
            new Vector3f(0.0F, progress * 0.8F, 0.0F),
            new Quaternionf().rotationY(progress * (float) Math.PI * 2.0F),
            new Vector3f(scale, scale, scale), new Quaternionf());
        CompoundTag data = display.saveWithoutId(new CompoundTag());
        Transformation.CODEC.encodeStart(NbtOps.INSTANCE, transform).result()
            .ifPresent(tag -> data.put(Display.TAG_TRANSFORMATION, tag));
        display.load(data);
        if (age < DURATION) return false;

        Vec3 burst = origin.add(0.0, 0.8, 0.0);
        level.sendParticles(mode == CubeState.Mode.LUCKY ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.WITCH,
            burst.x, burst.y, burst.z, 14, 0.22, 0.22, 0.22, 0.04);
        level.playSound(null, burst.x, burst.y, burst.z, SoundEvents.AMETHYST_BLOCK_BREAK,
            SoundSource.PLAYERS, 0.5F, mode == CubeState.Mode.LUCKY ? 1.5F : 0.65F);
        display.discard();
        return true;
    }
}
