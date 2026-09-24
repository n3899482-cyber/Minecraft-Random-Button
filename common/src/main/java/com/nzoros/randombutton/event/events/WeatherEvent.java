package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import com.nzoros.randombutton.event.EventDisposition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public final class WeatherEvent implements RandomEvent {
    @Override
    public String name() {
        return "Thunder Trial";
    }

    @Override
    public EventDisposition disposition() {
        return EventDisposition.HARMFUL;
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        level.setWeatherParameters(0, 20 * 45, true, true);
        var lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(player.getX(), player.getY(), player.getZ());
            lightning.setVisualOnly(true);
            lightning.setCause(player);
            level.addFreshEntity(lightning);
        }
        player.hurt(player.damageSources().lightningBolt(), 4.0F);
    }
}
