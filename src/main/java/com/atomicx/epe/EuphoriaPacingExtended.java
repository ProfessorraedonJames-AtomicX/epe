package com.atomicx.epe;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/** Euphoria Pacing Extended (EPE) - client-side rendering and frame-pacing optimizer. */
@Mod(EuphoriaPacingExtended.MODID)
public final class EuphoriaPacingExtended {
    public static final String MODID = "euphoria_pacing_extended";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EuphoriaPacingExtended(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            var modBusGroup = context.getModBusGroup();
            context.registerConfig(ModConfig.Type.CLIENT, EpeConfig.SPEC);
            FMLClientSetupEvent.getBus(modBusGroup).addListener(this::clientSetup);
            LOGGER.info("Euphoria Pacing Extended loaded.");
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        AdaptiveQuality.start();
    }
}
