package com.nzoros.randombutton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ClientCubeHud {
    private static final int EDGE_MARGIN = 10;
    private static CubeStatePayload state = new CubeStatePayload(0, 0, 0, 0);

    private ClientCubeHud() { }

    public static void update(CubeStatePayload payload) {
        state = payload;
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;

        int width = 148;
        int lines = 2 + (state.lucky() > 0 ? 1 : 0) + (state.unlucky() > 0 ? 1 : 0);
        int x = graphics.guiWidth() - width - EDGE_MARGIN;
        int y = EDGE_MARGIN;
        int height = 8 + lines * 11;
        graphics.fill(x, y, x + width, y + height, 0xB0101018);
        graphics.fill(x, y, x + width, y + 1, 0xFFE5B943);
        graphics.drawString(minecraft.font, Component.translatable("hud.randombutton.title"),
            x + 6, y + 5, 0xFFE6C35A, true);
        graphics.drawString(minecraft.font,
            Component.translatable("hud.randombutton.activations", state.activations()), x + 6, y + 16,
            0xFFFFFFFF, true);

        int row = y + 27;
        if (state.lucky() > 0) {
            Component text = Component.translatable("hud.randombutton.lucky", state.lucky())
                .append(state.mode() == 1 ? " *" : "");
            graphics.drawString(minecraft.font, text, x + 6, row, 0xFF98E46B, true);
            row += 11;
        }
        if (state.unlucky() > 0) {
            Component text = Component.translatable("hud.randombutton.unlucky", state.unlucky())
                .append(state.mode() == 2 ? " *" : "");
            graphics.drawString(minecraft.font, text, x + 6, row, 0xFFD4A3ED, true);
        }
    }
}
