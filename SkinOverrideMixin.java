package com.voresfeatures.mixin;

import com.voresfeatures.client.skin.SkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class SkinOverrideMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void vores$skin(CallbackInfoReturnable<SkinTextures> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        SkinTextures s = SkinManager.override(cir.getReturnValue());
        if (s != null) cir.setReturnValue(s);
    }
}
