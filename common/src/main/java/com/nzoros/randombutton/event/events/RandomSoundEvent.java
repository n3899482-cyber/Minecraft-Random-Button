package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.EventContext;
import com.nzoros.randombutton.event.EventDisposition;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

public final class RandomSoundEvent implements RandomEvent {
    @Override
    public String name() {
        return "Spider Panic";
    }

    @Override
    public EventDisposition disposition() {
        return EventDisposition.HARMFUL;
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        level.playSound(null, origin, SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.85F);
        EventContext context = new EventContext(level, player, origin);
        for (int i = 0; i < 2; i++) {
            var spider = EntityType.SPIDER.create(level);
            if (spider == null) continue;
            spider.moveTo(context.randomGroundOutsideLobby(), level.random.nextFloat() * 360.0F, 0.0F);
            spider.finalizeSpawn(level, level.getCurrentDifficultyAt(spider.blockPosition()),
                MobSpawnType.EVENT, null);
            spider.setTarget(player);
            spider.setPersistenceRequired();
            level.addFreshEntity(spider);
            EventRuntime.trackTemporary(spider, 20 * 75);
        }
    }
}
