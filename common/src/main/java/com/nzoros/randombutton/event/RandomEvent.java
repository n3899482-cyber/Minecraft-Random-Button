package com.nzoros.randombutton.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public interface RandomEvent {
    String name();

    default String id() {
        return name().toLowerCase(java.util.Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    default Component displayName() {
        return Component.literal(name());
    }

    default EventDisposition disposition() {
        return EventDisposition.CHAOTIC;
    }

    default boolean canRun(ServerLevel level, ServerPlayer player, BlockPos origin) {
        return true;
    }

    void execute(ServerLevel level, ServerPlayer player, BlockPos origin);
}
