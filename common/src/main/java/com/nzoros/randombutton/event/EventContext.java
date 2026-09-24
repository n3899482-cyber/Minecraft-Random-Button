package com.nzoros.randombutton.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

public record EventContext(ServerLevel level, ServerPlayer player, BlockPos buttonPos) {
    private static final int LOBBY_SAFE_RADIUS = 10;

    public BlockPos randomGroundOutsideLobby() {
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        int distance = LOBBY_SAFE_RADIUS + 2 + level.random.nextInt(7);
        int x = buttonPos.getX() + (int) Math.round(Math.cos(angle) * distance);
        int z = buttonPos.getZ() + (int) Math.round(Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }
}
