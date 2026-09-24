package com.nzoros.randombutton;

import com.nzoros.randombutton.lobby.LobbyManager;
import com.nzoros.randombutton.lobby.LobbyProtectionManager;
import com.nzoros.randombutton.lobby.LobbyReturnManager;
import com.nzoros.randombutton.event.runtime.EventRuntime;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod(MyMod.MOD_ID)
public class MyModNeoforge {

    public static IEventBus modEventBus;

    public MyModNeoforge(IEventBus modEventBus) {
        MyModNeoforge.modEventBus = modEventBus;

        MyMod.init(new NeoforgePlatform());
        modEventBus.addListener(MyModNeoforge::onRegisterPayloads);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MyModNeoforgeClient.register();
        }
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onServerTick);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onExplosion);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onItemToss);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(MyModNeoforge::onRegisterCommands);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            LobbyManager.onPlayerJoin(player);
            CubeState.sync(player);
        }
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(CubeStatePayload.TYPE, CubeStatePayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientCubeHud.update(payload)));
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        EventRuntime.tick(event.getServer());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        RandomButtonCommands.register(event.getDispatcher());
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level
            && LobbyProtectionManager.isProtected(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level
            && LobbyProtectionManager.isProtected(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var clickedPos = event.getPos();
        if (level.getBlockState(clickedPos).is(ModBlocks.RANDOM_BUTTON.value())) return;

        var placedPos = clickedPos.relative(event.getFace());
        if (LobbyProtectionManager.isProtected(level, clickedPos)
            || LobbyProtectionManager.isProtected(level, placedPos)) {
            event.setCanceled(true);
        }
    }

    private static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> LobbyProtectionManager.isProtected(event.getLevel(), pos));
    }

    private static void onItemToss(ItemTossEvent event) {
        if (LobbyReturnManager.isRecallItem(event.getEntity().getItem())) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
                LobbyReturnManager.restoreItem(player);
            }
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            LobbyReturnManager.restoreItem(player);
            CubeState.sync(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof net.minecraft.server.level.ServerPlayer oldPlayer
            && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer newPlayer) {
            CubeState.copy(oldPlayer, newPlayer);
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            event.getDrops().removeIf(drop -> LobbyReturnManager.isRecallItem(drop.getItem()));
        }
    }

    public static class NeoforgePlatform implements Platform {

        @Override
        public void sendCubeState(net.minecraft.server.level.ServerPlayer player, CubeStatePayload payload) {
            PacketDistributor.sendToPlayer(player, payload);
        }

        private final Map<Registry<?>, DeferredRegister<?>> registers = new HashMap<>();

        @SuppressWarnings("unchecked")
        private <T> DeferredRegister<T> getRegister(Registry<T> registry) {
            if (registers.containsKey(registry)) {
                return (DeferredRegister<T>) registers.get(registry);
            }
            var register = DeferredRegister.create(registry, MyMod.MOD_ID);
            register.register(modEventBus);
            registers.put(registry, register);
            return register;
        }

        @Override
        public <T> Holder<T> register(Registry<T> registry, ResourceLocation rl, Supplier<T> value) {
            return getRegister(registry).register(rl.getPath(), value);
        }

        @Override
        public CreativeModeTab.Builder creativeTabBuilder() {
            return CreativeModeTab.builder();
        }
    }
}
