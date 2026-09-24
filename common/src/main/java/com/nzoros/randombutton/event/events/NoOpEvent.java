package com.nzoros.randombutton.event.events;

import com.nzoros.randombutton.event.RandomEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class NoOpEvent implements RandomEvent {
    @Override
    public String name() {
        return "Nothing Happened";
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        // Intentionally empty: suspense is also an event.
    }
}
