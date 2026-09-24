package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.EventDisposition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import java.util.List;

public final class HarmlessMobEvent implements RandomEvent {
    private static final List<EntityType<? extends Mob>> MOBS = List.of(
        EntityType.CHICKEN,
        EntityType.COW,
        EntityType.SHEEP
    );

    @Override
    public EventDisposition disposition() {
        return EventDisposition.BENEFICIAL;
    }

    @Override
    public String name() {
        return "Friendly Visitors";
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        EntityType<? extends Mob> type = MOBS.get(level.random.nextInt(MOBS.size()));
        for (int i = 0; i < 3; i++) {
            Mob mob = type.create(level);
            if (mob != null) {
                double x = origin.getX() + level.random.nextInt(7) - 3 + 0.5;
                double z = origin.getZ() + level.random.nextInt(7) - 3 + 0.5;
                mob.moveTo(x, origin.getY() + 1.0, z, level.random.nextFloat() * 360.0F, 0.0F);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.EVENT, null);
                level.addFreshEntity(mob);
            }
        }
    }
}
