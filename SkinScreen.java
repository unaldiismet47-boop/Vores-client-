package com.voresfeatures.client.gui;

import com.voresfeatures.client.Config;
import com.voresfeatures.client.skin.SkinManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SkinScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget field;
    private String status = "";

    public SkinScreen(Screen parent) {
        super(Text.literal("Skin"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 30, 200, 20, Text.literal("Nick"));
        field.setMaxLength(16);
        field.setText(Config.data.skinName == null ? "" : Config.data.skinName);
        addDrawableChild(field);
        setInitialFocus(field);

        addDrawableChild(ButtonWidget.builder(Text.literal("Uygula"), b -> {
            String n = field.getText().trim();
            if (n.isEmpty()) return;
            status = "Yükleniyor...";
            SkinManager.load(n, msg -> status = msg);
        }).dimensions(width / 2 - 100, height / 2, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Sıfırla"), b -> {
            SkinManager.reset();
            status = "Sıfırlandı";
        }).dimensions(width / 2 + 2, height / 2, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Geri"), b -> close())
                .dimensions(width / 2 - 100, height / 2 + 26, 200, 20).build());
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Orijinal hesap nicki gir", width / 2, height / 2 - 50, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, status, width / 2, height / 2 + 54, 0xFFAAAAAA);
    }
}
