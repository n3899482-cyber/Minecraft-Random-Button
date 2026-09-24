package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

public final class TimeEvent implements RandomEvent {
    @Override
    public String name() {
        return "Night Hunt / Dawn Rush";
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        long nextTime = level.getDayTime() % 24000L < 12000L ? 13000L : 1000L;
        level.setDayTime(level.getDayTime() / 24000L * 24000L + nextTime);
        if (nextTime == 13000L) {
            EventContext context = new EventContext(level, player, origin);
            for (int i = 0; i < 4; i++) {
                var zombie = EntityType.ZOMBIE.create(level);
                if (zombie == null) continue;
                zombie.moveTo(context.randomGroundOutsideLobby(), level.random.nextFloat() * 360.0F, 0.0F);
                zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()),
                    MobSpawnType.EVENT, null);
                zombie.setTarget(player);
                zombie.setPersistenceRequired();
                level.addFreshEntity(zombie);
                EventRuntime.trackTemporary(zombie, 20 * 90);
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 45, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 45, 1));
        }
    }
}
