package com.voresfeatures.client.gui;

import com.voresfeatures.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;

/** Ana ekran ve ESC menüsüne sosyal medya yazısını çizer. */
public final class ScreenHooks {
    private ScreenHooks() {}

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> social(ctx, s.width, s.height - 26));
            } else if (screen instanceof GameMenuScreen) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> social(ctx, s.width, s.height - 14));
            }
        });
    }

    private static void social(DrawContext ctx, int width, int y) {
        int c = ModuleManager.GENERAL == null ? 0xFF55AAFF : ModuleManager.GENERAL.accent();
        ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, VoresScreens.SOCIAL, width / 2, y, c);
    }
}
