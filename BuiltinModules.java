package com.voresfeatures.client.module;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public final class BuiltinModules {
    private BuiltinModules() {}

    // =====================================================
    //  GENEL (HUD konumu, tema, giriş animasyonu)
    // =====================================================

    public static final class General extends Module {
        private static final int[] PALETTE = {
                0xFF55AAFF, 0xFFFF5555, 0xFF55FF55, 0xFFFFAA00,
                0xFFAA55FF, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF };

        public final Setting x, y, scale, background, theme, intro, introTime;

        public General() {
            super("Genel", Category.GENERAL, true);
            alwaysOn();
            x = num("HUD X", 0, 400, 2, 4);
            y = num("HUD Y", 0, 300, 2, 6);
            scale = num("HUD Ölçek", 0.5, 2.0, 0.1, 1.0);
            background = bool("HUD Arka Plan", true);
            theme = num("Tema Rengi (0-7)", 0, 7, 1, 0);
            intro = bool("Giriş Animasyonu", true);
            introTime = num("Giriş Süresi (sn)", 1.0, 5.0, 0.2, 2.6);
        }

        public int accent() { return PALETTE[Math.max(0, Math.min(PALETTE.length - 1, theme.integer()))]; }
    }

    // =====================================================
    //  HUD
    // =====================================================

    public static final class FpsDisplay extends Module {
        public FpsDisplay() { super("FPS", Category.HUD, true); }
        @Override public String text(MinecraftClient mc) { return "FPS: " + mc.getCurrentFps(); }
    }

    public static final class CpsDisplay extends Module {
        private final Deque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
        private boolean pl, pr;

        public CpsDisplay() { super("CPS", Category.HUD, true); }

        @Override public String text(MinecraftClient mc) {
            long h = mc.getWindow().getHandle();
            long now = System.currentTimeMillis();
            boolean cl = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean cr = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            if (cl && !pl) left.add(now);
            if (cr && !pr) right.add(now);
            pl = cl;
            pr = cr;
            while (!left.isEmpty() && now - left.peekFirst() > 1000) left.pollFirst();
            while (!right.isEmpty() && now - right.peekFirst() > 1000) right.pollFirst();
            return "CPS: " + left.size() + " | " + right.size();
        }
    }

    public static final class CoordsDisplay extends Module {
        public CoordsDisplay() { super("Koordinat", Category.HUD, true); }
        @Override public String text(MinecraftClient mc) {
            if (mc.player == null) return null;
            return String.format(Locale.US, "XYZ: %d / %d / %d",
                    (int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()),
                    (int) Math.floor(mc.player.getZ()));
        }
    }

    public static final class DirectionDisplay extends Module {
        public DirectionDisplay() { super("Yön", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            if (mc.player == null) return null;
            float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
            return String.format(Locale.US, "Yön: %s (%d°)", mc.player.getHorizontalFacing().asString(), Math.round(yaw));
        }
    }

    public static final class PingDisplay extends Module {
        public PingDisplay() { super("Ping", Category.HUD, true); }
        @Override public String text(MinecraftClient mc) {
            if (mc.getNetworkHandler() == null || mc.player == null) return "Ping: -";
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return "Ping: " + (e == null ? 0 : e.getLatency()) + " ms";
        }
    }

    public static final class ServerDisplay extends Module {
        public ServerDisplay() { super("Sunucu IP", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            ServerInfo si = mc.getCurrentServerEntry();
            return si == null ? "Tek Oyunculu" : si.address;
        }
    }

    public static final class ClockDisplay extends Module {
        private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm");
        public ClockDisplay() { super("Saat", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) { return LocalTime.now().format(F); }
    }

    public static final class MemoryDisplay extends Module {
        public MemoryDisplay() { super("RAM", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) / 1048576L;
            return "RAM: " + used + " / " + (r.maxMemory() / 1048576L) + " MB";
        }
    }

    public static final class BiomeDisplay extends Module {
        public BiomeDisplay() { super("Biyom", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            if (mc.world == null || mc.player == null) return null;
            String b = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                    .map(k -> k.getValue().getPath()).orElse("?");
            return "Biyom: " + b;
        }
    }

    public static final class DayDisplay extends Module {
        public DayDisplay() { super("Gün Sayacı", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            if (mc.world == null) return null;
            return "Gün: " + (mc.world.getTimeOfDay() / 24000L);
        }
    }

    public static final class SpeedDisplay extends Module {
        public SpeedDisplay() { super("Hız (BPS)", Category.HUD, false); }
        @Override public String text(MinecraftClient mc) {
            if (mc.player == null) return null;
            double dx = mc.player.getX() - mc.player.prevX;
            double dz = mc.player.getZ() - mc.player.prevZ;
            return String.format(Locale.US, "BPS: %.1f", Math.sqrt(dx * dx + dz * dz) * 20.0);
        }
    }

    public static final class Keystrokes extends Module {
        public Keystrokes() { super("Keystrokes", Category.HUD, true); }

        @Override public void render(DrawContext c, MinecraftClient mc) {
            int s = 22, g = 2;
            int full = s * 3 + g * 2;
            int bx = 6, by = c.getScaledWindowHeight() - 80 - 6;
            GameOptions o = mc.options;
            key(c, mc, "W", o.forwardKey.isPressed(), bx + s + g, by, s, s);
            key(c, mc, "A", o.leftKey.isPressed(), bx, by + s + g, s, s);
            key(c, mc, "S", o.backKey.isPressed(), bx + s + g, by + s + g, s, s);
            key(c, mc, "D", o.rightKey.isPressed(), bx + 2 * (s + g), by + s + g, s, s);
            int half = (full - g) / 2;
            int ry = by + 2 * (s + g);
            key(c, mc, "LMB", o.attackKey.isPressed(), bx, ry, half, 18);
            key(c, mc, "RMB", o.useKey.isPressed(), bx + half + g, ry, half, 18);
            key(c, mc, "SPACE", o.jumpKey.isPressed(), bx, ry + 18 + g, full, 12);
        }

        private void key(DrawContext c, MinecraftClient mc, String label, boolean down, int x, int y, int w, int h) {
            c.fill(x, y, x + w, y + h, down ? 0xCCFFFFFF : 0x90000000);
            c.drawCenteredTextWithShadow(mc.textRenderer, label, x + w / 2, y + (h - 8) / 2,
                    down ? 0xFF000000 : 0xFFFFFFFF);
        }
    }

    public static final class ArmorHud extends Module {
        public ArmorHud() { super("Zırh HUD", Category.HUD, true); }

        @Override public void render(DrawContext c, MinecraftClient mc) {
            if (mc.player == null) return;
            int x = c.getScaledWindowWidth() - 52;
            int y = c.getScaledWindowHeight() / 2 - 36;
            for (int i = 3; i >= 0; i--) {
                ItemStack s = mc.player.getInventory().armor.get(i);
                if (s.isEmpty()) continue;
                c.drawItem(s, x, y);
                if (s.isDamageable()) {
                    int left = s.getMaxDamage() - s.getDamage();
                    float r = left / (float) s.getMaxDamage();
                    int col = r > 0.5f ? 0xFF55FF55 : (r > 0.2f ? 0xFFFFFF55 : 0xFFFF5555);
                    c.drawTextWithShadow(mc.textRenderer, String.valueOf(left), x + 20, y + 4, col);
                }
                y += 18;
            }
        }
    }

    public static final class PotionHud extends Module {
        public PotionHud() { super("İksir HUD", Category.HUD, true); }

        @Override public void render(DrawContext c, MinecraftClient mc) {
            if (mc.player == null) return;
            int y = 6;
            int right = c.getScaledWindowWidth() - 6;
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString();
                int amp = e.getAmplifier();
                int sec = Math.max(0, e.getDuration()) / 20;
                String time = e.getDuration() < 0 ? "inf" : String.format(Locale.US, "%d:%02d", sec / 60, sec % 60);
                String t = name + (amp > 0 ? " " + (amp + 1) : "") + "  " + time;
                int w = mc.textRenderer.getWidth(t);
                c.drawTextWithShadow(mc.textRenderer, t, right - w, y, 0xFFFFFFFF);
                y += 11;
            }
        }
    }

    // =====================================================
    //  PERFORMANS
    // =====================================================

    public static final class FpsBoost extends Module {
        private ParticlesMode oParticles;
        private CloudRenderMode oClouds;
        private GraphicsMode oGraphics;
        private Double oDistance;
        private Boolean oShadows;
        private Integer oBlend;

        public FpsBoost() { super("FPS Boost", Category.PERFORMANCE, false); }

        @Override protected void onEnable() {
            GameOptions o = MinecraftClient.getInstance().options;
            oParticles = o.getParticles().getValue();
            oClouds = o.getCloudRenderMode().getValue();
            oGraphics = o.getGraphicsMode().getValue();
            oDistance = o.getEntityDistanceScaling().getValue();
            oShadows = o.getEntityShadows().getValue();
            oBlend = o.getBiomeBlendRadius().getValue();
            o.getParticles().setValue(ParticlesMode.MINIMAL);
            o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            o.getGraphicsMode().setValue(GraphicsMode.FAST);
            o.getEntityDistanceScaling().setValue(0.5);
            o.getEntityShadows().setValue(false);
            o.getBiomeBlendRadius().setValue(0);
        }

        @Override protected void onDisable() {
            if (oParticles == null) return;
            GameOptions o = MinecraftClient.getInstance().options;
            o.getParticles().setValue(oParticles);
            o.getCloudRenderMode().setValue(oClouds);
            o.getGraphicsMode().setValue(oGraphics);
            o.getEntityDistanceScaling().setValue(oDistance);
            o.getEntityShadows().setValue(oShadows);
            o.getBiomeBlendRadius().setValue(oBlend);
            oParticles = null;
        }
    }

    public static final class SmoothLightingOff extends Module {
        private Boolean old;
        public SmoothLightingOff() { super("Yumuşak Işık Kapalı", Category.PERFORMANCE, false); }

        @Override protected void onEnable() {
            GameOptions o = MinecraftClient.getInstance().options;
            old = o.getAo().getValue();
            o.getAo().setValue(false);
        }

        @Override protected void onDisable() {
            if (old == null) return;
            MinecraftClient.getInstance().options.getAo().setValue(old);
            old = null;
        }
    }

    public static final class UnlimitedFps extends Module {
        private Boolean oVsync;
        private Integer oMax;
        public UnlimitedFps() { super("Sınırsız FPS", Category.PERFORMANCE, false); }

        @Override protected void onEnable() {
            GameOptions o = MinecraftClient.getInstance().options;
            oVsync = o.getEnableVsync().getValue();
            oMax = o.getMaxFps().getValue();
            o.getEnableVsync().setValue(false);
            o.getMaxFps().setValue(260);
        }

        @Override protected void onDisable() {
            if (oVsync == null) return;
            GameOptions o = MinecraftClient.getInstance().options;
            o.getEnableVsync().setValue(oVsync);
            o.getMaxFps().setValue(oMax);
            oVsync = null;
        }
    }

    public static final class RenderDistanceCap extends Module {
        private final Setting cap;
        private Integer old;

        public RenderDistanceCap() {
            super("Mesafe Sınırı", Category.PERFORMANCE, false);
            cap = num("Max Mesafe (chunk)", 2, 32, 1, 8);
        }

        @Override public void onTick(MinecraftClient mc) {
            if (!isEnabled()) return;
            int cur = mc.options.getViewDistance().getValue();
            int c = cap.integer();
            if (cur > c) {
                if (old == null) old = cur;
                mc.options.getViewDistance().setValue(c);
            }
        }

        @Override protected void onDisable() {
            if (old == null) return;
            MinecraftClient.getInstance().options.getViewDistance().setValue(old);
            old = null;
        }
    }

    public static final class NoViewBob extends Module {
        private Boolean old;
        public NoViewBob() { super("Sallanma Kapalı", Category.PERFORMANCE, false); }

        @Override protected void onEnable() {
            GameOptions o = MinecraftClient.getInstance().options;
            old = o.getBobView().getValue();
            o.getBobView().setValue(false);
        }

        @Override protected void onDisable() {
            if (old == null) return;
            MinecraftClient.getInstance().options.getBobView().setValue(old);
            old = null;
        }
    }

    // =====================================================
    //  GÖRSEL
    // =====================================================

    public static final class Zoom extends Module {
        private final KeyBinding key = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.voresfeatures.zoom", GLFW.GLFW_KEY_C, "category.voresfeatures"));
        private final Setting fov, smooth;
        private Integer oldFov;
        private Boolean oldSmooth;

        public Zoom() {
            super("Zoom (C)", Category.VISUAL, true);
            fov = num("Zoom FOV", 10, 60, 5, 30);
            smooth = bool("Yumuşak Kamera", true);
        }

        @Override public void onTick(MinecraftClient mc) {
            boolean down = isEnabled() && key.isPressed() && mc.currentScreen == null;
            if (down && oldFov == null) {
                oldFov = mc.options.getFov().getValue();
                mc.options.getFov().setValue(fov.integer());
                if (smooth.bool()) {
                    oldSmooth = mc.options.smoothCameraEnabled;
                    mc.options.smoothCameraEnabled = true;
                }
            } else if (!down && oldFov != null) {
                mc.options.getFov().setValue(oldFov);
                oldFov = null;
                if (oldSmooth != null) {
                    mc.options.smoothCameraEnabled = oldSmooth;
                    oldSmooth = null;
                }
            }
        }
    }

    public static final class Fullbright extends Module {
        private Double old;
        public Fullbright() { super("Fullbright", Category.VISUAL, false); }

        @Override protected void onEnable() {
            GameOptions o = MinecraftClient.getInstance().options;
            if (old == null) old = o.getGamma().getValue();
            o.getGamma().setValue(16.0);
        }

        @Override protected void onDisable() {
            if (old == null) return;
            MinecraftClient.getInstance().options.getGamma().setValue(old);
            old = null;
        }

        @Override public void onTick(MinecraftClient mc) {
            if (isEnabled() && mc.options.getGamma().getValue() < 16.0) mc.options.getGamma().setValue(16.0);
        }
    }

    public static final class HitEffect extends Module {
        private static final ParticleEffect[] FX = {
                ParticleTypes.CRIT, ParticleTypes.ENCHANTED_HIT, ParticleTypes.HEART,
                ParticleTypes.FLAME, ParticleTypes.END_ROD };
        private final Setting count, type;

        public HitEffect() {
            super("Vuruş Efekti", Category.VISUAL, true);
            count = num("Parçacık Sayısı", 4, 40, 2, 12);
            type = num("Efekt (0 crit,1 büyü,2 kalp,3 ateş,4 rod)", 0, 4, 1, 0);
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
                if (isEnabled() && world.isClient && entity instanceof LivingEntity) {
                    ParticleEffect fx = FX[Math.max(0, Math.min(FX.length - 1, type.integer()))];
                    for (int i = 0; i < count.integer(); i++) {
                        world.addParticle(fx,
                                entity.getX(), entity.getBodyY(0.5), entity.getZ(),
                                (world.random.nextDouble() - 0.5) * 0.8,
                                world.random.nextDouble() * 0.5,
                                (world.random.nextDouble() - 0.5) * 0.8);
                    }
                }
                return ActionResult.PASS;
            });
        }
    }

    public static final class TimeChanger extends Module {
        private final Setting time;
        public TimeChanger() {
            super("Zaman Değiştirici", Category.VISUAL, false);
            time = num("Saat (0 sabah, 6000 öğle, 13000 gece)", 0, 24000, 500, 6000);
        }
        @Override public void onTick(MinecraftClient mc) {
            if (isEnabled() && mc.world != null) mc.world.setTimeOfDay((long) time.num());
        }
    }

    public static final class ClearWeather extends Module {
        public ClearWeather() { super("Yağmur Kapalı", Category.VISUAL, false); }
        @Override public void onTick(MinecraftClient mc) {
            if (!isEnabled() || mc.world == null) return;
            mc.world.setRainGradient(0f);
            mc.world.setThunderGradient(0f);
        }
    }

    public static final class CinematicCamera extends Module {
        public CinematicCamera() { super("Sinematik Kamera", Category.VISUAL, false); }
        @Override public void onTick(MinecraftClient mc) {
            if (isEnabled()) mc.options.smoothCameraEnabled = true;
        }
        @Override protected void onDisable() {
            MinecraftClient.getInstance().options.smoothCameraEnabled = false;
        }
    }

    // =====================================================
    //  HAREKET
    // =====================================================

    public static final class ToggleSprint extends Module {
        public ToggleSprint() { super("Toggle Sprint", Category.MOVEMENT, false); }
        @Override public void onTick(MinecraftClient mc) {
            if (!isEnabled() || mc.player == null || mc.currentScreen != null) return;
            if (mc.options.forwardKey.isPressed() && !mc.player.isSneaking()) mc.options.sprintKey.setPressed(true);
        }
    }
}
