package com.voresfeatures.client.gui;

import com.voresfeatures.client.Config;
import com.voresfeatures.client.module.Module;
import com.voresfeatures.client.module.ModuleManager;
import com.voresfeatures.client.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** G tuşuyla açılan VORES menüsü: sekmeler, modül listesi, ayarlar (sağ tık). */
public class ClickGuiScreen extends Screen {
    private static final int PW = 390;
    private static Module.Category selected = Module.Category.HUD;
    private static final Set<Module> EXPANDED = new HashSet<>();

    private double scroll;
    private Setting dragging;
    private int px, py, ph, cy0, cy1;

    private record Row(Module m, Setting s, int y, int h) {}

    public ClickGuiScreen() { super(Text.literal("VORES")); }

    @Override
    public boolean shouldPause() { return false; }

    private void layout() {
        ph = Math.min(height - 20, 300);
        px = (width - PW) / 2;
        py = (height - ph) / 2;
        cy0 = py + 44;
        cy1 = py + ph - 32;
    }

    private List<Row> rows() {
        List<Row> list = new ArrayList<>();
        int y = 0;
        for (Module m : ModuleManager.MODULES) {
            if (m.category != selected) continue;
            list.add(new Row(m, null, y, 20));
            y += 22;
            if (EXPANDED.contains(m)) {
                for (Setting s : m.settings) {
                    list.add(new Row(m, s, y, 16));
                    y += 17;
                }
                y += 3;
            }
        }
        return list;
    }

    private static int total(List<Row> rows) {
        if (rows.isEmpty()) return 0;
        Row last = rows.get(rows.size() - 1);
        return last.y() + last.h() + 4;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        layout();
        super.render(ctx, mx, my, delta);

        int accent = ModuleManager.GENERAL.accent();
        int dim = 0xFF000000 | ((accent & 0xFEFEFE) >> 1);

        ctx.fill(px, py, px + PW, py + ph, 0xE0101018);
        ctx.fill(px, py, px + PW, py + 2, accent);
        ctx.drawTextWithShadow(textRenderer, "VORES CLIENT", px + 8, py + 8, accent);
        String close = "G: kapat";
        ctx.drawTextWithShadow(textRenderer, close, px + PW - 8 - textRenderer.getWidth(close), py + 8, 0xFF888888);

        // sekmeler
        Module.Category[] cats = Module.Category.values();
        int tw = (PW - 16) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int tx = px + 8 + i * tw;
            boolean sel = cats[i] == selected;
            boolean hover = mx >= tx && mx < tx + tw - 2 && my >= py + 22 && my < py + 38;
            ctx.fill(tx, py + 22, tx + tw - 2, py + 38, sel ? accent : (hover ? 0xFF303040 : 0xFF202028));
            ctx.drawCenteredTextWithShadow(textRenderer, cats[i].label, tx + (tw - 2) / 2, py + 26,
                    sel ? 0xFF000000 : 0xFFFFFFFF);
        }

        // satırlar
        List<Row> rows = rows();
        int viewH = cy1 - cy0;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, total(rows) - viewH)));
        int x = px + 8, w = PW - 16;

        ctx.enableScissor(px + 4, cy0, px + PW - 4, cy1);
        for (Row r : rows) {
            int ry = cy0 + r.y() - (int) scroll;
            if (ry + r.h() < cy0 || ry > cy1) continue;
            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + r.h() && my >= cy0 && my < cy1;
            if (r.s() == null) {
                Module m = r.m();
                int bg = m.isEnabled() ? (hover ? accent : dim) : (hover ? 0xFF383846 : 0xFF22222C);
                if (m.isSettingsOnly()) bg = hover ? 0xFF383846 : 0xFF2A2A38;
                ctx.fill(x, ry, x + w, ry + r.h(), bg);
                ctx.drawTextWithShadow(textRenderer, m.name, x + 6, ry + 6, 0xFFFFFFFF);
                String right = m.isSettingsOnly() ? "" : (m.isEnabled() ? "AÇIK" : "KAPALI");
                int rx = x + w - 6;
                if (!m.settings.isEmpty()) {
                    String arrow = EXPANDED.contains(m) ? "v" : ">";
                    rx -= textRenderer.getWidth(arrow);
                    ctx.drawTextWithShadow(textRenderer, arrow, rx, ry + 6, 0xFFCCCCCC);
                    rx -= 8;
                }
                if (!right.isEmpty()) {
                    rx -= textRenderer.getWidth(right);
                    ctx.drawTextWithShadow(textRenderer, right, rx, ry + 6,
                            m.isEnabled() ? 0xFFFFFFFF : 0xFF999999);
                }
            } else {
                Setting s = r.s();
                ctx.fill(x + 8, ry, x + w, ry + r.h(), hover ? 0xFF2A2A36 : 0xFF1A1A22);
                ctx.drawTextWithShadow(textRenderer, s.display(), x + 14, ry + 2, 0xFFDDDDDD);
                if (s.type == Setting.Type.NUM) {
                    int sx = x + 14, sw = w - 14 - 6;
                    ctx.fill(sx, ry + 12, sx + sw, ry + 14, 0xFF404050);
                    ctx.fill(sx, ry + 12, sx + (int) (sw * s.ratio()), ry + 14, accent);
                }
            }
        }
        ctx.disableScissor();

        // alt bilgi
        String hint = "Sol tık: aç/kapat  |  Sağ tık: ayarlar  |  Kaydır: liste";
        ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, py + ph - 26, 0xFF777777);
        ctx.drawCenteredTextWithShadow(textRenderer, VoresScreens.SOCIAL, width / 2, py + ph - 14, accent);
    }

    private void setFromMouse(Setting s, double mx) {
        int x = px + 8, w = PW - 16;
        int sx = x + 14, sw = w - 14 - 6;
        double ratio = Math.max(0, Math.min(1, (mx - sx) / sw));
        s.set(s.min + ratio * (s.max - s.min), false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        layout();
        Module.Category[] cats = Module.Category.values();
        int tw = (PW - 16) / cats.length;
        if (my >= py + 22 && my < py + 38) {
            for (int i = 0; i < cats.length; i++) {
                int tx = px + 8 + i * tw;
                if (mx >= tx && mx < tx + tw - 2) {
                    selected = cats[i];
                    scroll = 0;
                    return true;
                }
            }
        }
        int x = px + 8, w = PW - 16;
        if (mx >= x && mx < x + w && my >= cy0 && my < cy1) {
            for (Row r : rows()) {
                int ry = cy0 + r.y() - (int) scroll;
                if (my < ry || my >= ry + r.h()) continue;
                if (r.s() == null) {
                    if (button == 0) {
                        r.m().toggle();
                    } else if (button == 1 && !r.m().settings.isEmpty()) {
                        if (!EXPANDED.remove(r.m())) EXPANDED.add(r.m());
                    }
                } else if (button == 0) {
                    if (r.s().type == Setting.Type.BOOL) {
                        r.s().toggle();
                    } else {
                        dragging = r.s();
                        setFromMouse(dragging, mx);
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            setFromMouse(dragging, mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) {
            dragging = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scroll -= vertical * 14;
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_G) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
