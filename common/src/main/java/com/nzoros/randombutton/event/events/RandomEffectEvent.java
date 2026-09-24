package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.EventDisposition;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class RandomEffectEvent implements RandomEvent {
    private static final List<Holder<MobEffect>> GOOD_EFFECTS = List.of(
        MobEffects.MOVEMENT_SPEED,
        MobEffects.DAMAGE_RESISTANCE,
        MobEffects.NIGHT_VISION,
        MobEffects.REGENERATION
    );
    private static final List<Holder<MobEffect>> BAD_EFFECTS = List.of(
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.WEAKNESS,
        MobEffects.DIG_SLOWDOWN,
        MobEffects.HUNGER,
        MobEffects.POISON,
        MobEffects.BLINDNESS
    );

    @Override
    public String name() {
        return "Effect";
    }

    @Override
    public Component displayName() {
        return Component.translatable("event.randombutton.effect");
    }

    @Override
    public EventDisposition disposition() {
        return EventDisposition.CHAOTIC;
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        boolean harmful = level.random.nextInt(100) < 70;
        List<Holder<MobEffect>> effects = harmful ? BAD_EFFECTS : GOOD_EFFECTS;
        Holder<MobEffect> effect = effects.get(level.random.nextInt(effects.size()));
        int duration = harmful ? 20 * (15 + level.random.nextInt(16)) : 20 * 30;
        int amplifier = harmful && level.random.nextInt(4) == 0 ? 1 : 0;
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }
}
