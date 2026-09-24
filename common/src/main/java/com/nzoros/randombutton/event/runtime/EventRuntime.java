package com.nzoros.randombutton.event.runtime;

import com.nzoros.randombutton.lobby.LobbyProtectionManager;
import com.nzoros.randombutton.lobby.LobbyReturnManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EventRuntime {
    private static final Map<UUID, ActiveEvent> ACTIVE_EVENTS = new HashMap<>();
    private static final List<ActiveEvent> VISUAL_EVENTS = new ArrayList<>();
    private static final List<TemporaryEntity> TEMPORARY_ENTITIES = new ArrayList<>();

    private EventRuntime() { }

    public static boolean hasActive(UUID playerId) {
        return ACTIVE_EVENTS.containsKey(playerId);
    }

    public static boolean start(ActiveEvent event) {
        return ACTIVE_EVENTS.putIfAbsent(event.playerId(), event) == null;
    }

    public static void startVisual(ActiveEvent event) {
        VISUAL_EVENTS.add(event);
    }

    public static void trackTemporary(Entity entity, int lifetimeTicks) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        TEMPORARY_ENTITIES.add(new TemporaryEntity(level.dimension(), entity.getUUID(),
            level.getServer().getTickCount() + lifetimeTicks));
    }

    public static void tick(MinecraftServer server) {
        LobbyProtectionManager.tick(server);
        LobbyReturnManager.tick(server);
        Iterator<ActiveEvent> iterator = ACTIVE_EVENTS.values().iterator();
        while (iterator.hasNext()) {
            ActiveEvent event = iterator.next();
            if (event.tick(server)) iterator.remove();
        }

        VISUAL_EVENTS.removeIf(event -> event.tick(server));


        Iterator<TemporaryEntity> temporaryIterator = TEMPORARY_ENTITIES.iterator();
        while (temporaryIterator.hasNext()) {
            TemporaryEntity temporary = temporaryIterator.next();
            if (server.getTickCount() < temporary.expiresAt()) continue;
            ServerLevel level = server.getLevel(temporary.dimension());
            Entity entity = level == null ? null : level.getEntity(temporary.entityId());
            if (entity != null) entity.discard();
            temporaryIterator.remove();
        }
    }

    private record TemporaryEntity(ResourceKey<Level> dimension, UUID entityId, int expiresAt) { }
}
