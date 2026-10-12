package com.voresfeatures.client.module;

import com.voresfeatures.client.Config;
import com.voresfeatures.client.gui.ClickGuiScreen;
import com.voresfeatures.client.gui.VoresScreens;
import com.voresfeatures.client.module.BuiltinModules.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();
    public static General GENERAL;
    private static boolean applied;
    private static KeyBinding guiKey, cosmeticsKey;

    private ModuleManager() {}

    public static void init() {
        GENERAL = new General();
        MODULES.add(GENERAL);
        Collections.addAll(MODULES,
                // HUD
                new FpsDisplay(), new CpsDisplay(), new CoordsDisplay(), new DirectionDisplay(),
                new PingDisplay(), new ServerDisplay(), new ClockDisplay(), new MemoryDisplay(),
                new BiomeDisplay(), new DayDisplay(), new SpeedDisplay(),
                new Keystrokes(), new ArmorHud(), new PotionHud(),
                // Performans
                new FpsBoost(), new SmoothLightingOff(), new UnlimitedFps(),
                new RenderDistanceCap(), new NoViewBob(),
                // Görsel
                new Zoom(), new Fullbright(), new HitEffect(), new TimeChanger(),
                new ClearWeather(), new CinematicCamera(),
                // Hareket
                new ToggleSprint());

        guiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.voresfeatures.gui", GLFW.GLFW_KEY_G, "category.voresfeatures"));
        cosmeticsKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.voresfeatures.cosmetics", GLFW.GLFW_KEY_J, "category.voresfeatures"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!applied) {
                applied = true; // seçenekler hazır olsun diye ilk tick'te uygula
                for (Module m : MODULES)
                    m.load(Config.data.modules.getOrDefault(m.name, m.defaultOn));
            }
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
            }
            while (cosmeticsKey.wasPressed()) {
                if (mc.currentScreen == null) VoresScreens.openCosmetics(mc, null);
            }
            for (Module m : MODULES) m.onTick(mc);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null || mc.options.debugEnabled) return;

        List<Module> custom = new ArrayList<>();
        int accent = GENERAL.accent();

        ctx.getMatrices().push();
        ctx.getMatrices().translate((float) GENERAL.x.num(), (float) GENERAL.y.num(), 0f);
        float sc = (float) GENERAL.scale.num();
        ctx.getMatrices().scale(sc, sc, 1f);
        int y = 0;
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            String t = m.text(mc);
            if (t != null) {
                int w = mc.textRenderer.getWidth(t);
                if (GENERAL.background.bool()) {
                    ctx.fill(0, y - 2, w + 10, y + 10, 0x90000000);
                }
                ctx.fill(0, y - 2, 2, y + 10, accent);
                ctx.drawTextWithShadow(mc.textRenderer, t, 5, y, 0xFFFFFFFF);
                y += 14;
            } else {
                custom.add(m);
            }
        }
        ctx.getMatrices().pop();

        for (Module m : custom) m.render(ctx, mc);
    }
}
