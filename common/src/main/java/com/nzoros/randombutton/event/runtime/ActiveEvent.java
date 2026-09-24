package com.nzoros.randombutton.event.runtime;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface ActiveEvent {
    UUID playerId();

    boolean tick(MinecraftServer server);
}
