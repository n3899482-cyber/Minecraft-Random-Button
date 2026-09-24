package com.nzoros.randombutton.lobby;

import com.nzoros.randombutton.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class LobbyManager {
    private static final String VISITED_TAG = "randombutton.lobby_visited";
    private static final int RADIUS = 6;
    private static final int SITE_RADIUS = RADIUS + 2;
    private static final int MAX_SLOPE = 3;
    private static final int CLEAR_HEIGHT = 7;
    private static final ResourceLocation LOBBY_TEMPLATE =
        ResourceLocation.fromNamespaceAndPath("randombutton", "lobby");

    private LobbyManager() { }

    public static void onPlayerJoin(ServerPlayer player) {
        ServerLevel level = player.getServer().overworld();
        BlockPos center = findLobby(level);
        if (center == null) {
            center = createLobby(level);
        }
        refreshLobbyLights(level, center.offset(-RADIUS, -4, -RADIUS),
            new Vec3i(RADIUS * 2 + 1, 10, RADIUS * 2 + 1));
        LobbyProtectionManager.register(level, center);
        LobbyReturnManager.rememberLobby(player, level, center);

        if (player.getTags().contains(VISITED_TAG)) return;

        player.teleportTo(level, center.getX() + 0.5, center.getY() - 0.9, center.getZ() + 3.5, 180.0F, 0.0F);
        player.addTag(VISITED_TAG);
        player.sendSystemMessage(Component.translatable("message.randombutton.welcome"));
    }

    public static BlockPos findLobby(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockPos candidate = new BlockPos(spawn.getX() + x, y, spawn.getZ() + z);
                    if (level.getBlockState(candidate).is(ModBlocks.RANDOM_BUTTON.value())) return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos createLobby(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        Site site = findBestSite(level, spawn);
        int floorY = site.floorY();
        BlockPos center = new BlockPos(site.getX(), floorY + 1, site.getZ());

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = 0; y < CLEAR_HEIGHT; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
                int surfaceY = surfaceY(level, site.getX() + x, site.getZ() + z);
                for (int y = surfaceY; y < floorY; y++) {
                    level.setBlock(new BlockPos(site.getX() + x, y, site.getZ() + z),
                        Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }
        }

        var template = level.getStructureManager().getOrCreate(LOBBY_TEMPLATE);
        BlockPos origin = center.offset(-RADIUS, -1, -RADIUS);
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.random, 3);

        Vec3i size = template.getSize();
        BlockPos buttonPos = findButton(level, origin, size);
        if (buttonPos == null) {
            buttonPos = center.above();
            level.setBlock(buttonPos, ModBlocks.RANDOM_BUTTON.value().defaultBlockState(), 3);
        }
        refreshLobbyLights(level, origin, size);

        level.setDefaultSpawnPos(buttonPos.offset(0, -1, 3), 180.0F);
        return buttonPos;
    }

    private static Site findBestSite(ServerLevel level, BlockPos spawn) {
        for (int searchRadius : new int[]{32, 64, 96, 128}) {
            Site best = null;
            int bestScore = Integer.MAX_VALUE;
            for (int x = -searchRadius; x <= searchRadius; x += 4) {
                for (int z = -searchRadius; z <= searchRadius; z += 4) {
                    Site candidate = inspectSite(level, spawn.getX() + x, spawn.getZ() + z);
                    if (candidate == null) continue;

                    int score = candidate.variation() * 1000 + x * x + z * z;
                    if (score < bestScore) {
                        best = candidate;
                        bestScore = score;
                    }
                }
            }
            if (best != null) return best;
        }

        Site fallback = findFallbackSite(level, spawn);
        if (fallback != null) return fallback;

        int floorY = highestSurface(level, spawn.getX(), spawn.getZ(), RADIUS);
        return new Site(spawn.getX(), spawn.getZ(), floorY, Integer.MAX_VALUE);
    }

    private static Site findFallbackSite(ServerLevel level, BlockPos spawn) {
        Site best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int x = -128; x <= 128; x += 8) {
            for (int z = -128; z <= 128; z += 8) {
                Site candidate = inspectFallbackSite(level, spawn.getX() + x, spawn.getZ() + z);
                if (candidate == null) continue;
                int score = candidate.variation() * 1000 + x * x + z * z;
                if (score < bestScore) {
                    best = candidate;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    private static Site inspectFallbackSite(ServerLevel level, int centerX, int centerZ) {
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                int worldX = centerX + x;
                int worldZ = centerZ + z;
                int surfaceY = surfaceY(level, worldX, worldZ);
                BlockPos supportPos = new BlockPos(worldX, surfaceY - 1, worldZ);
                BlockState support = level.getBlockState(supportPos);
                if (!support.getFluidState().isEmpty()
                    || !support.isFaceSturdy(level, supportPos, Direction.UP)
                    || support.is(BlockTags.LOGS)
                    || support.is(BlockTags.LEAVES)) {
                    return null;
                }
                minimum = Math.min(minimum, surfaceY);
                maximum = Math.max(maximum, surfaceY);
            }
        }
        return new Site(centerX, centerZ, maximum, maximum - minimum);
    }

    private static int highestSurface(ServerLevel level, int centerX, int centerZ, int radius) {
        int highest = level.getMinBuildHeight();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                highest = Math.max(highest, surfaceY(level, centerX + x, centerZ + z));
            }
        }
        return highest;
    }

    private static Site inspectSite(ServerLevel level, int centerX, int centerZ) {
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;

        for (int x = -SITE_RADIUS; x <= SITE_RADIUS; x++) {
            for (int z = -SITE_RADIUS; z <= SITE_RADIUS; z++) {
                int worldX = centerX + x;
                int worldZ = centerZ + z;
                int surfaceY = surfaceY(level, worldX, worldZ);
                BlockPos supportPos = new BlockPos(worldX, surfaceY - 1, worldZ);
                BlockState support = level.getBlockState(supportPos);

                if (!support.getFluidState().isEmpty()
                    || !support.isFaceSturdy(level, supportPos, Direction.UP)
                    || support.is(BlockTags.LOGS)
                    || support.is(BlockTags.LEAVES)) {
                    return null;
                }

                for (int y = surfaceY; y < surfaceY + CLEAR_HEIGHT; y++) {
                    BlockState obstruction = level.getBlockState(new BlockPos(worldX, y, worldZ));
                    if (obstruction.is(BlockTags.LOGS) || obstruction.is(BlockTags.LEAVES)) return null;
                    if (!obstruction.isAir() && !obstruction.canBeReplaced()) return null;
                }

                minimum = Math.min(minimum, surfaceY);
                maximum = Math.max(maximum, surfaceY);
                if (maximum - minimum > MAX_SLOPE) return null;
            }
        }

        return new Site(centerX, centerZ, maximum, maximum - minimum);
    }

    private static int surfaceY(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static BlockPos findButton(ServerLevel level, BlockPos origin, Vec3i size) {
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (level.getBlockState(pos).is(ModBlocks.RANDOM_BUTTON.value())) return pos;
                }
            }
        }
        return null;
    }

    private static void refreshLobbyLights(ServerLevel level, BlockPos origin, Vec3i size) {
        var lightEngine = level.getChunkSource().getLightEngine();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.getLightEmission() > 0) {
                        level.sendBlockUpdated(pos, state, state, 3);
                        lightEngine.checkBlock(pos);
                        for (Direction direction : Direction.values()) {
                            lightEngine.checkBlock(pos.relative(direction));
                        }
                    }
                }
            }
        }
    }

    private record Site(int x, int z, int floorY, int variation) {
        private int getX() {
            return x;
        }

        private int getZ() {
            return z;
        }
    }
}
