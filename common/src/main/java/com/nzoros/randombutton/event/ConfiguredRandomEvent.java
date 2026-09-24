package com.nzoros.randombutton.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ConfiguredRandomEvent implements RandomEvent {
    private final String name;
    private final String id;
    private final EventDisposition disposition;
    private final EventAction action;

    public ConfiguredRandomEvent(
        String name, EventDisposition disposition, EventAction action
    ) {
        this(name, null, disposition, action);
    }

    public ConfiguredRandomEvent(
        String name, String id, EventDisposition disposition, EventAction action
    ) {
        this.name = name;
        this.id = id;
        this.disposition = disposition;
        this.action = action;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String id() {
        return id == null ? RandomEvent.super.id() : id;
    }

    @Override
    public EventDisposition disposition() {
        return disposition;
    }

    @Override
    public void execute(ServerLevel level, ServerPlayer player, BlockPos origin) {
        action.execute(new EventContext(level, player, origin));
    }

    @FunctionalInterface
    public interface EventAction {
        void execute(EventContext context);
    }
}
