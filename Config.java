package com.voresfeatures.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    public static class Data {
        public Map<String, Boolean> modules = new HashMap<>();
        public Map<String, Double> settings = new HashMap<>();
        public String skinName = "";
    }

    public static Data data = new Data();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vores-client.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Data d = GSON.fromJson(Files.readString(p), Data.class);
                if (d != null) data = d;
            }
        } catch (Exception ignored) {
        }
        if (data.modules == null) data.modules = new HashMap<>();
        if (data.settings == null) data.settings = new HashMap<>();
        if (data.skinName == null) data.skinName = "";
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(data));
        } catch (IOException ignored) {
        }
    }
}
