package com.nzoros.randombutton;

import com.nzoros.randombutton.lobby.LobbyManager;
import com.nzoros.randombutton.lobby.LobbyProtectionManager;
import com.nzoros.randombutton.lobby.LobbyReturnManager;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;

public class MyModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MyMod.init(new FabricPlatform());
        PayloadTypeRegistry.playS2C().register(CubeStatePayload.TYPE, CubeStatePayload.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            RandomButtonCommands.register(dispatcher));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LobbyManager.onPlayerJoin(handler.player);
            CubeState.sync(handler.player);
        });
        ServerTickEvents.END_SERVER_TICK.register(EventRuntime::tick);
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
            CubeState.copy(oldPlayer, newPlayer));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            LobbyReturnManager.restoreItem(newPlayer);
            CubeState.sync(newPlayer);
        });
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !LobbyProtectionManager.isProtected(level, pos)
        );
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.getBlockState(hit.getBlockPos()).is(ModBlocks.RANDOM_BUTTON.value())) {
                return InteractionResult.PASS;
            }
            boolean blocked = LobbyProtectionManager.isProtected(level, hit.getBlockPos())
                || LobbyProtectionManager.isProtected(level, hit.getBlockPos().relative(hit.getDirection()));
            return blocked ? InteractionResult.FAIL : InteractionResult.PASS;
        });
    }

    public static class FabricPlatform implements Platform {

        @Override
        public void sendCubeState(ServerPlayer player, CubeStatePayload payload) {
            ServerPlayNetworking.send(player, payload);
        }

        @Override
        public CreativeModeTab.Builder creativeTabBuilder() {
            return FabricItemGroup.builder();
        }
    }
}
