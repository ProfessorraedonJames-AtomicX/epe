package com.atomicx.epe;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

/** Options shown in Video Settings. They read and write EPE's config file. */
public final class EpeOptions {
    private EpeOptions() {}

    /** Slider from 15 to 240 FPS in steps of 5. */
    public static OptionInstance<Integer> targetFps() {
        int initial = Math.min(240, Math.max(15, Math.round(EpeConfig.targetFps / 5.0f) * 5));
        return new OptionInstance<Integer>(
            "options.epe.targetFps",
            OptionInstance.<Integer>cachedConstantTooltip(Component.translatable("options.epe.targetFps.tooltip")),
            (caption, value) -> Options.genericValueLabel(caption, value),
            new OptionInstance.IntRange(3, 48).xmap(i -> i * 5, v -> v / 5, false),
            initial,
            EpeConfig::setTargetFps
        );
    }

    public static OptionInstance<Boolean> autoQuality() {
        return OptionInstance.createBoolean(
            "options.epe.autoQuality",
            OptionInstance.<Boolean>cachedConstantTooltip(Component.translatable("options.epe.autoQuality.tooltip")),
            EpeConfig.adaptive,
            EpeConfig::setAdaptive
        );
    }

    public static OptionInstance<Boolean> engineDrawListOptimizer() {
        return OptionInstance.createBoolean(
            "options.epe.engineOptimizer",
            OptionInstance.<Boolean>cachedConstantTooltip(Component.translatable("options.epe.engineOptimizer.tooltip")),
            EpeConfig.engineDrawListOptimizer,
            EpeConfig::setEngineDrawListOptimizer
        );
    }

    public static OptionInstance<Boolean> earlyZOptimization() {
        return OptionInstance.createBoolean(
            "options.epe.earlyZ",
            OptionInstance.<Boolean>cachedConstantTooltip(Component.translatable("options.epe.earlyZ.tooltip")),
            EpeConfig.earlyZOptimization,
            EpeConfig::setEarlyZOptimization
        );
    }

    public static OptionInstance<Boolean> blockEntityCulling() {
        return OptionInstance.createBoolean(
            "options.epe.blockEntityCulling",
            OptionInstance.<Boolean>cachedConstantTooltip(Component.translatable("options.epe.blockEntityCulling.tooltip")),
            EpeConfig.blockEntityCulling,
            EpeConfig::setBlockEntityCulling
        );
    }

    public static OptionInstance<Boolean> budgetedUploads() {
        return OptionInstance.createBoolean(
            "options.epe.budgetedUploads",
            OptionInstance.<Boolean>cachedConstantTooltip(Component.translatable("options.epe.budgetedUploads.tooltip")),
            EpeConfig.budgetedUploads,
            EpeConfig::setBudgetedUploads
        );
    }
}
