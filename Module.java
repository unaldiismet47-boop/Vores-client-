package com.voresfeatures.client.module;

import com.voresfeatures.client.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category {
        HUD("HUD"), PERFORMANCE("Performans"), VISUAL("Görsel"), MOVEMENT("Hareket"), GENERAL("Genel");
        public final String label;
        Category(String label) { this.label = label; }
    }

    public final String name;
    public final Category category;
    public final boolean defaultOn;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;
    private boolean settingsOnly;

    protected Module(String name, Category category, boolean defaultOn) {
        this.name = name;
        this.category = category;
        this.defaultOn = defaultOn;
    }

    /** Sadece ayar paneli olan modül (aç/kapat yok). */
    protected void alwaysOn() {
        settingsOnly = true;
        enabled = true;
    }

    public boolean isSettingsOnly() { return settingsOnly; }
    public boolean isEnabled() { return enabled; }

    protected Setting num(String n, double min, double max, double step, double def) {
        Setting s = new Setting(n, Setting.Type.NUM, min, max, step, def);
        s.owner = this;
        s.init(Config.data.settings.getOrDefault(name + "." + n, def));
        settings.add(s);
        return s;
    }

    protected Setting bool(String n, boolean def) {
        Setting s = new Setting(n, Setting.Type.BOOL, 0, 1, 1, def ? 1 : 0);
        s.owner = this;
        s.init(Config.data.settings.getOrDefault(name + "." + n, def ? 1.0 : 0.0));
        settings.add(s);
        return s;
    }

    public void setEnabled(boolean v) {
        if (settingsOnly || v == enabled) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
        Config.data.modules.put(name, v);
        Config.save();
    }

    public void toggle() { setEnabled(!enabled); }

    /** Kayıtlı durumu yüklerken kullanılır (config'e tekrar yazmaz). */
    void load(boolean v) {
        if (settingsOnly) return;
        enabled = v;
        if (v) onEnable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(MinecraftClient mc) {}

    /** Tek satırlık HUD yazısı. null dönerse render() kullanılır. */
    public String text(MinecraftClient mc) { return null; }

    public void render(DrawContext ctx, MinecraftClient mc) {}
}
