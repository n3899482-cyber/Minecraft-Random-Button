package com.nzoros.randombutton.lobby;

import com.nzoros.randombutton.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Set;

public final class LobbyReturnManager {
    private static final String DIMENSION_PREFIX = "randombutton.lobby_dimension_";
    private static final String X_PREFIX = "randombutton.lobby_x_";
    private static final String Y_PREFIX = "randombutton.lobby_y_";
    private static final String Z_PREFIX = "randombutton.lobby_z_";

    private LobbyReturnManager() { }

    public static void rememberLobby(ServerPlayer player, ServerLevel level, BlockPos buttonPos) {
        clearLocationTags(player);
        player.addTag(DIMENSION_PREFIX + level.dimension().location());
        player.addTag(X_PREFIX + buttonPos.getX());
        player.addTag(Y_PREFIX + buttonPos.getY());
        player.addTag(Z_PREFIX + buttonPos.getZ());
        ensureItem(player);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (readLocation(player) == null) {
                ServerLevel overworld = server.overworld();
                BlockPos lobby = LobbyManager.findLobby(overworld);
                if (lobby != null) rememberLobby(player, overworld, lobby);
            }
            ensureItem(player);
        }
    }

    public static boolean returnToLobby(ServerPlayer player) {
        LobbyLocation location = readLocation(player);
        if (location == null) {
            ServerLevel overworld = player.getServer().overworld();
            BlockPos lobby = LobbyManager.findLobby(overworld);
            if (lobby == null) {
                player.sendSystemMessage(Component.translatable("message.randombutton.no_lobby"), true);
                return false;
            }
            rememberLobby(player, overworld, lobby);
            location = new LobbyLocation(overworld.dimension(), lobby);
        }

        ServerLevel level = player.getServer().getLevel(location.dimension());
        if (level == null) return false;
        BlockPos safe = findSafeReturn(level, location.buttonPos());
        if (safe == null) return false;

        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
            40, 0.5, 0.8, 0.5, 0.15);
        player.teleportTo(level, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, 180.0F, 0.0F);
        level.playSound(null, safe, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.1F);
        level.sendParticles(ParticleTypes.PORTAL, safe.getX() + 0.5, safe.getY() + 1.0,
            safe.getZ() + 0.5, 40, 0.5, 0.8, 0.5, 0.15);
        return true;
    }

    public static BlockPos buttonPosition(ServerPlayer player) {
        LobbyLocation location = readLocation(player);
        if (location == null || !player.level().dimension().equals(location.dimension())) return null;
        BlockPos pos = location.buttonPos();
        return player.serverLevel().getBlockState(pos).is(com.nzoros.randombutton.ModBlocks.RANDOM_BUTTON.value())
            ? pos : null;
    }

    public static boolean isRecallItem(ItemStack stack) {
        return stack.is(ModItems.RECALL_SIGIL.value());
    }

    public static boolean hasRecallItem(Player player) {
        if (isRecallItem(player.containerMenu.getCarried())) return true;
        if (isRecallItem(player.inventoryMenu.getCarried())) return true;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (isRecallItem(player.getInventory().getItem(slot))) return true;
        }
        for (var slot : player.containerMenu.slots) {
            if (isRecallItem(slot.getItem())) return true;
        }
        return false;
    }

    public static void restoreItem(ServerPlayer player) {
        ensureItem(player);
    }

    private static void ensureItem(ServerPlayer player) {
        boolean found = isRecallItem(player.containerMenu.getCarried())
            || isRecallItem(player.inventoryMenu.getCarried());
        for (var slot : player.containerMenu.slots) {
            if (slot.container != player.getInventory() && isRecallItem(slot.getItem())) {
                found = true;
            }
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!isRecallItem(stack)) continue;
            if (!found) {
                found = true;
            } else {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
        if (found) return;

        ItemStack sigil = new ItemStack(ModItems.RECALL_SIGIL.value());
        if (player.getInventory().add(sigil)) return;

        int slot = player.getInventory().selected;
        ItemStack displaced = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, sigil);
        if (!displaced.isEmpty()) player.drop(displaced, false);
    }

    private static BlockPos findSafeReturn(ServerLevel level, BlockPos buttonPos) {
        for (int radius = 3; radius <= 6; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                    for (int y = buttonPos.getY() - 2; y <= buttonPos.getY() + 2; y++) {
                        BlockPos feet = new BlockPos(buttonPos.getX() + x, y, buttonPos.getZ() + z);
                        if (isSafe(level, feet)) return feet;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).isAir()
            && level.getBlockState(feet.above()).isAir()
            && level.getFluidState(feet).isEmpty()
            && level.getFluidState(feet.below()).isEmpty()
            && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP);
    }

    private static LobbyLocation readLocation(ServerPlayer player) {
        String dimension = value(player, DIMENSION_PREFIX);
        Integer x = integerValue(player, X_PREFIX);
        Integer y = integerValue(player, Y_PREFIX);
        Integer z = integerValue(player, Z_PREFIX);
        if (dimension == null || x == null || y == null || z == null) return null;
        ResourceLocation id = ResourceLocation.tryParse(dimension);
        if (id == null) return null;
        return new LobbyLocation(ResourceKey.create(Registries.DIMENSION, id), new BlockPos(x, y, z));
    }

    private static String value(ServerPlayer player, String prefix) {
        return player.getTags().stream().filter(tag -> tag.startsWith(prefix))
            .map(tag -> tag.substring(prefix.length())).findFirst().orElse(null);
    }

    private static Integer integerValue(ServerPlayer player, String prefix) {
        String value = value(player, prefix);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static void clearLocationTags(ServerPlayer player) {
        Set<String> tags = Set.copyOf(player.getTags());
        tags.stream().filter(tag -> tag.startsWith(DIMENSION_PREFIX) || tag.startsWith(X_PREFIX)
            || tag.startsWith(Y_PREFIX) || tag.startsWith(Z_PREFIX)).forEach(player::removeTag);
    }

    private record LobbyLocation(ResourceKey<Level> dimension, BlockPos buttonPos) { }
}
