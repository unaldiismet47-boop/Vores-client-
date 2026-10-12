package com.voresfeatures.client;

import com.voresfeatures.client.gui.IntroAnimation;
import com.voresfeatures.client.gui.ScreenHooks;
import com.voresfeatures.client.module.ModuleManager;
import com.voresfeatures.client.skin.SkinManager;
import net.fabricmc.api.ClientModInitializer;

public class VoresClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        ModuleManager.init();
        IntroAnimation.init();
        ScreenHooks.init();
        SkinManager.loadSaved();
    }
}
