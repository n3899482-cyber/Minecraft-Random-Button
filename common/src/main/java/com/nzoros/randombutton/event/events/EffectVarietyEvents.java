package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.ConfiguredRandomEvent;
import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.EventDisposition;
import com.nzoros.randombutton.event.RandomEventRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class EffectVarietyEvents {
    private record EffectChoice(Holder<MobEffect> effect, int minSeconds, int maxSeconds, int maxAmplifier) { }

    private static final List<EffectChoice> BOONS = List.of(
        choice(MobEffects.MOVEMENT_SPEED, 25, 50, 1),
        choice(MobEffects.DIG_SPEED, 25, 50, 1),
        choice(MobEffects.JUMP, 20, 40, 1),
        choice(MobEffects.FIRE_RESISTANCE, 25, 45, 0),
        choice(MobEffects.WATER_BREATHING, 30, 60, 0),
        choice(MobEffects.NIGHT_VISION, 40, 70, 0),
        choice(MobEffects.REGENERATION, 8, 14, 0),
        choice(MobEffects.ABSORPTION, 25, 45, 0),
        choice(MobEffects.SLOW_FALLING, 25, 45, 0),
        choice(MobEffects.LUCK, 40, 70, 0)
    );
    private static final List<EffectChoice> CURSES = List.of(
        choice(MobEffects.MOVEMENT_SLOWDOWN, 12, 25, 1),
        choice(MobEffects.DIG_SLOWDOWN, 15, 30, 1),
        choice(MobEffects.WEAKNESS, 15, 30, 1),
        choice(MobEffects.HUNGER, 12, 24, 0),
        choice(MobEffects.POISON, 6, 12, 0),
        choice(MobEffects.BLINDNESS, 6, 12, 0),
        choice(MobEffects.DARKNESS, 8, 16, 0),
        choice(MobEffects.LEVITATION, 2, 4, 0),
        choice(MobEffects.UNLUCK, 25, 45, 0)
    );

    private EffectVarietyEvents() { }

    public static void register() {
        RandomEventRegistry.register(new ConfiguredRandomEvent("Lucky Blessing", EventDisposition.BENEFICIAL, c -> applyRandom(c, BOONS, 1)));
        RandomEventRegistry.register(new ConfiguredRandomEvent("Double Blessing", EventDisposition.BENEFICIAL, c -> applyRandom(c, BOONS, 2)));
        RandomEventRegistry.register(new ConfiguredRandomEvent("Bad Omen", EventDisposition.HARMFUL, c -> applyRandom(c, CURSES, 1)));
        RandomEventRegistry.register(new ConfiguredRandomEvent("Mixed Fortune", EventDisposition.CHAOTIC, c -> {
                applyRandom(c, BOONS, 1);
                applyRandom(c, CURSES, 1);
            }));
    }

    private static EffectChoice choice(Holder<MobEffect> effect, int min, int max, int amplifier) {
        return new EffectChoice(effect, min, max, amplifier);
    }

    private static void applyRandom(EventContext context, List<EffectChoice> choices, int count) {
        int first = context.level().random.nextInt(choices.size());
        for (int i = 0; i < count; i++) {
            EffectChoice choice = choices.get((first + i + (i == 0 ? 0 : context.level().random.nextInt(choices.size() - 1)))
                % choices.size());
            int seconds = choice.minSeconds + context.level().random.nextInt(choice.maxSeconds - choice.minSeconds + 1);
            int amplifier = context.level().random.nextInt(choice.maxAmplifier + 1);
            context.player().addEffect(new MobEffectInstance(choice.effect, seconds * 20, amplifier));
        }
    }
}
