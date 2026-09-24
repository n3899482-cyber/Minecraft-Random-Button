package com.nzoros.randombutton;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class MyModNeoforgeClient {
    private MyModNeoforgeClient() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MyModNeoforgeClient::onRenderGui);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        ClientCubeHud.render(event.getGuiGraphics());
    }
}
