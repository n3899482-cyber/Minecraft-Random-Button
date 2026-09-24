package com.nzoros.randombutton.lobby;

import com.nzoros.randombutton.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class LobbyProtectionManager {
    private static final int HORIZONTAL_RADIUS = 6;
    private static final int BELOW_BUTTON = 4;
    private static final int ABOVE_BUTTON = 5;
    private static final Map<ResourceKey<Level>, ProtectedLobby> LOBBIES = new HashMap<>();

    private LobbyProtectionManager() { }

    public static void register(ServerLevel level, BlockPos buttonPos) {
        ProtectedLobby existing = LOBBIES.get(level.dimension());
        if (existing != null && existing.buttonPos().equals(buttonPos)) return;

        Map<BlockPos, BlockState> snapshot = new HashMap<>();
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int y = -BELOW_BUTTON; y <= ABOVE_BUTTON; y++) {
                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    BlockPos pos = buttonPos.offset(x, y, z);
                    snapshot.put(pos.immutable(), level.getBlockState(pos));
                }
            }
        }
        LOBBIES.put(level.dimension(), new ProtectedLobby(buttonPos.immutable(), snapshot));
    }

    public static boolean isProtected(Level level, BlockPos pos) {
        ProtectedLobby lobby = LOBBIES.get(level.dimension());
        return lobby != null && lobby.snapshot().containsKey(pos);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 10 != 0) return;

        for (Map.Entry<ResourceKey<Level>, ProtectedLobby> entry : LOBBIES.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) continue;

            ProtectedLobby lobby = entry.getValue();
            for (Map.Entry<BlockPos, BlockState> block : lobby.snapshot().entrySet()) {
                BlockState current = level.getBlockState(block.getKey());
                BlockState expected = block.getValue();
                if (block.getKey().equals(lobby.buttonPos())
                    && current.is(ModBlocks.RANDOM_BUTTON.value())) {
                    continue;
                }
                if (current != expected) level.setBlock(block.getKey(), expected, 3);
            }
        }
    }

    private record ProtectedLobby(BlockPos buttonPos, Map<BlockPos, BlockState> snapshot) { }
}
