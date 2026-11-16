package com.atomicx.epe;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/** Options shown in Video Settings. They read and write EPE's config file. */
public final class EpeOptions {
    private EpeOptions() {}

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
