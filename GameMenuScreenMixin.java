package com.voresfeatures.mixin;

import com.voresfeatures.client.gui.VoresScreens;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** ESC menüsünün sol üstüne Kozmetikler / Modlar / Skin butonları ekler. */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void vores$addButtons(CallbackInfo ci) {
        Screen self = this;
        addDrawableChild(ButtonWidget.builder(Text.literal("Kozmetikler"),
                b -> VoresScreens.openCosmetics(client, self)).dimensions(6, 6, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Modlar"),
                b -> VoresScreens.openMods(client)).dimensions(6, 30, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Skin"),
                b -> VoresScreens.openSkin(client, self)).dimensions(6, 54, 90, 20).build());
    }
}
