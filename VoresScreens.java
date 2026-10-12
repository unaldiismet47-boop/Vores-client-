package com.voresfeatures.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/** Butonların açtığı ekranlar tek yerde. */
public final class VoresScreens {
    public static final String SOCIAL = "YT: vores123   |   Instagram: vores123";

    private VoresScreens() {}

    /** Asıl VORES client'taki (içine gömülü jar) CosmeticsScreen'i açar. */
    public static void openCosmetics(MinecraftClient mc, Screen parent) {
        try {
            Class<?> c = Class.forName("com.voresfeatures.client.gui.CosmeticsScreen");
            var k = c.getDeclaredConstructor(Screen.class);
            k.setAccessible(true);
            mc.setScreen((Screen) k.newInstance(parent));
        } catch (Throwable ignored) {
            // sınıf bulunamazsa sessizce geç
        }
    }

    public static void openMods(MinecraftClient mc) { mc.setScreen(new ClickGuiScreen()); }

    public static void openSkin(MinecraftClient mc, Screen parent) { mc.setScreen(new SkinScreen(parent)); }
}
