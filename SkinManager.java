package com.voresfeatures.client.skin;

import com.voresfeatures.client.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Orijinal bir hesabın nickinden skin çeker. Sadece SENİN ekranında görünür (client-side). */
public final class SkinManager {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Identifier TEX_ID = Identifier.of("voresfeatures", "skin/custom");
    private static volatile SkinTextures custom;

    private SkinManager() {}

    /** Mixin tarafından çağrılır. Özel skin yoksa null döner. */
    public static SkinTextures override(SkinTextures original) {
        SkinTextures c = custom;
        if (c == null) return null;
        return new SkinTextures(c.texture(), null, original.capeTexture(), original.elytraTexture(), c.model(), original.secure());
    }

    public static void loadSaved() {
        String n = Config.data.skinName;
        if (n != null && !n.isEmpty()) load(n, s -> {});
    }

    public static void reset() {
        custom = null;
        Config.data.skinName = "";
        Config.save();
    }

    public static void load(String name, Consumer<String> status) {
        CompletableFuture.runAsync(() -> {
            try {
                String enc = URLEncoder.encode(name, StandardCharsets.UTF_8);
                JsonObject profile = getJson("https://api.mojang.com/users/profiles/minecraft/" + enc);
                String id = profile.get("id").getAsString();
                JsonObject session = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
                String b64 = session.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                JsonObject textures = JsonParser.parseString(new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonObject("textures");
                if (!textures.has("SKIN")) throw new IllegalStateException("skin yok");
                JsonObject skin = textures.getAsJsonObject("SKIN");
                String url = skin.get("url").getAsString();
                boolean slim = skin.has("metadata") && skin.getAsJsonObject("metadata").has("model")
                        && "slim".equals(skin.getAsJsonObject("metadata").get("model").getAsString());

                byte[] png = HTTP.send(HttpRequest.newBuilder(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofByteArray()).body();
                NativeImage img = NativeImage.read(new ByteArrayInputStream(png));

                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> {
                    mc.getTextureManager().destroyTexture(TEX_ID);
                    mc.getTextureManager().registerTexture(TEX_ID, new NativeImageBackedTexture(img));
                    custom = new SkinTextures(TEX_ID, null, null, null,
                            slim ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE, true);
                    Config.data.skinName = name;
                    Config.save();
                    status.accept("Skin uygulandı: " + name);
                });
            } catch (Exception e) {
                status.accept("Hesap veya skin bulunamadı");
            }
        });
    }

    private static JsonObject getJson(String url) throws Exception {
        HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new IllegalStateException("HTTP " + r.statusCode());
        return JsonParser.parseString(r.body()).getAsJsonObject();
    }
}
