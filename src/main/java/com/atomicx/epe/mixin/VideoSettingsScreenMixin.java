package com.atomicx.epe.mixin;

import com.atomicx.epe.EpeOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the EPE Performance section to Video Settings. */
@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {
    protected VideoSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"), require = 0)
    private void epe$addEuphoriaOptions(CallbackInfo ci) {
        if (this.list == null) return;
        this.list.addHeader(Component.translatable("options.epe.header"));
        this.list.addSmall(new OptionInstance<?>[] {
            EpeOptions.targetFps(),
            EpeOptions.autoQuality()
        });
        this.list.addSmall(new OptionInstance<?>[] {
            EpeOptions.engineDrawListOptimizer(),
            EpeOptions.earlyZOptimization()
        });
        this.list.addSmall(new OptionInstance<?>[] {
            EpeOptions.blockEntityCulling(),
            EpeOptions.budgetedUploads()
        });
    }
}
