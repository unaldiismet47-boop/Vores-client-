package com.voresfeatures.client.module;

import com.voresfeatures.client.Config;

import java.util.Locale;

public final class Setting {
    public enum Type { BOOL, NUM }

    public final String name;
    public final Type type;
    public final double min, max, step;
    Module owner;
    private double value;

    Setting(String name, Type type, double min, double max, double step, double def) {
        this.name = name;
        this.type = type;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = def;
    }

    public boolean bool() { return value >= 0.5; }
    public double num() { return value; }
    public int integer() { return (int) Math.round(value); }

    public float ratio() {
        if (max == min) return 0f;
        return (float) ((value - min) / (max - min));
    }

    void init(double v) { value = clamp(v); }

    public void set(double v, boolean save) {
        value = clamp(v);
        if (owner != null) {
            Config.data.settings.put(owner.name + "." + name, value);
            if (save) Config.save();
        }
    }

    public void toggle() { set(bool() ? 0 : 1, true); }

    private double clamp(double v) {
        if (type == Type.BOOL) return v >= 0.5 ? 1 : 0;
        if (step > 0) v = min + Math.round((v - min) / step) * step;
        return Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (type == Type.BOOL) return name + ": " + (bool() ? "AÇIK" : "KAPALI");
        if (step >= 1) return name + ": " + integer();
        return name + ": " + String.format(Locale.US, "%.1f", value);
    }
}
