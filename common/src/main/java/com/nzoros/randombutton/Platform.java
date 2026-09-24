package com.nzoros.randombutton;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;

import java.util.function.Supplier;

public interface Platform {

    default <T> Holder<T> register(Registry<T> registry, ResourceLocation rl, Supplier<T> value) {
        return Registry.registerForHolder(registry, rl, value.get());
    }

    @Contract(value = " -> new", pure = true)
    CreativeModeTab.Builder creativeTabBuilder();

    void sendCubeState(ServerPlayer player, CubeStatePayload payload);

    default boolean isClient() {
        try {
            //noinspection ResultOfMethodCallIgnored
            Minecraft.getInstance();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
