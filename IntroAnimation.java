package com.voresfeatures.client.gui;

import com.voresfeatures.client.module.BuiltinModules.General;
import com.voresfeatures.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Util;

/** Oyun açılınca bir kere: siyah ekran, büyüyen logo, çizgi, sosyal medya yazısı, yükleme çubuğu. */
public final class IntroAnimation {
    private static long start = -1;
    private static boolean done;

    private IntroAnimation() {}

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen && !done) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> render(ctx, s.width, s.height));
            }
        });
    }

    private static void render(DrawContext ctx, int w, int h) {
        if (done) return;
        General g = ModuleManager.GENERAL;
        if (g != null && !g.intro.bool()) { done = true; return; }

        float total = g == null ? 2600f : (float) (g.introTime.num() * 1000.0);
        long now = Util.getMeasuringTimeMs();
        if (start < 0) start = now;
        float t = (now - start) / total;
        if (t >= 1f) { done = true; return; }

        float fade = t < 0.7f ? 1f : 1f - (t - 0.7f) / 0.3f;
        int a = Math.max(0, Math.min(255, (int) (fade * 255)));
        ctx.fill(0, 0, w, h, a << 24);
        if (a < 8) return;

        int accent = g == null ? 0x55AAFF : (g.accent() & 0xFFFFFF);
        float tt = Math.min(1f, t / 0.45f);
        float ease = 1f - (1f - tt) * (1f - tt) * (1f - tt);
        var tr = MinecraftClient.getInstance().textRenderer;

        float scale = 2.5f + ease * 1.5f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(w / 2f, h / 2f - 22, 0f);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.drawCenteredTextWithShadow(tr, "VORES", 0, 0, (a << 24) | accent);
        ctx.getMatrices().pop();

        int lw = (int) (ease * 120);
        ctx.fill(w / 2 - lw, h / 2 + 4, w / 2 + lw, h / 2 + 6, (a << 24) | accent);
        ctx.drawCenteredTextWithShadow(tr, "C L I E N T", w / 2, h / 2 + 14, (a << 24) | 0xFFFFFF);

        int a2 = (int) (a * Math.min(1f, Math.max(0f, (t - 0.25f) / 0.2f)));
        if (a2 >= 8) {
            ctx.drawCenteredTextWithShadow(tr, VoresScreens.SOCIAL, w / 2, h / 2 + 32, (a2 << 24) | 0xBBBBBB);
        }

        int bw = 160;
        int fill = (int) (bw * Math.min(1f, t / 0.7f));
        ctx.fill(w / 2 - bw / 2, h - 30, w / 2 + bw / 2, h - 28, (a << 24) | 0x333333);
        ctx.fill(w / 2 - bw / 2, h - 30, w / 2 - bw / 2 + fill, h - 28, (a << 24) | accent);
    }
}
