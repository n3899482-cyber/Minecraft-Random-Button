package com.nzoros.randombutton;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class MyModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CubeStatePayload.TYPE,
            (payload, context) -> context.client().execute(() -> ClientCubeHud.update(payload)));
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> ClientCubeHud.render(graphics));
    }
}
