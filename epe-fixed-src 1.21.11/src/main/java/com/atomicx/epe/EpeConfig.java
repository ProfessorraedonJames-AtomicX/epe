package com.atomicx.epe;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = EuphoriaPacingExtended.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EpeConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ADAPTIVE =
        B.comment("Automatically lower entity distance and particles when FPS is low")
         .define("adaptiveQuality", true);
    private static final ForgeConfigSpec.IntValue TARGET_FPS =
        B.comment("FPS EPE tries to stay above")
         .defineInRange("targetFps", 60, 1, 5000);
    private static final ForgeConfigSpec.DoubleValue MIN_ENTITY_SCALE =
        B.comment("Lowest entity distance scaling the controller may use (0.5 is the game's own minimum)")
         .defineInRange("minEntityDistanceScale", 0.5, 0.5, 5.0);

    private static final ForgeConfigSpec.BooleanValue BUDGET_UPLOADS =
        B.comment("Spread chunk mesh uploads over several frames instead of all in one frame (reduces stutter)")
         .define("budgetedUploads", true);
    private static final ForgeConfigSpec.DoubleValue UPLOAD_BUDGET_MS =
        B.comment("Max milliseconds per frame spent uploading chunk meshes (at least one upload always happens). 0 = no limit (vanilla behavior)")
         .defineInRange("uploadBudgetMs", 2.0, 0.0, 1000.0);
    private static final ForgeConfigSpec.DoubleValue SYNC_REBUILD_BUDGET_MS =
        B.comment("Max milliseconds per frame spent rebuilding nearby chunk sections on the main thread; the rest go to worker threads. 0 = no limit (vanilla behavior)")
         .defineInRange("syncRebuildBudgetMs", 3.0, 0.0, 1000.0);

    private static final ForgeConfigSpec.BooleanValue ADAPTIVE_VISUALS =
        B.comment("Governor may also switch off clouds, entity shadows and shrink weather when FPS is low")
         .define("adaptiveVisuals", true);
    private static final ForgeConfigSpec.BooleanValue ADAPTIVE_SIM_DISTANCE =
        B.comment("Governor may lower simulation distance (less CPU use from the built-in server in singleplayer)")
         .define("adaptiveSimulationDistance", true);
    private static final ForgeConfigSpec.BooleanValue ADAPTIVE_RENDER_DISTANCE =
        B.comment("Governor may lower render distance as a last resort when FPS stays low")
         .define("adaptiveRenderDistance", true);
    private static final ForgeConfigSpec.IntValue MIN_RENDER_DISTANCE =
        B.comment("Render distance (chunks) the governor will never go below (2 is the game's own minimum)")
         .defineInRange("minRenderDistance", 8, 2, 32);
    private static final ForgeConfigSpec.IntValue PARTICLE_BURST_CAP =
        B.comment("Max particles accepted per tick (stops explosion/firework lag spikes). 0 = no cap (vanilla behavior)")
         .defineInRange("particleBurstCap", 1500, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue SMART_PARTICLE_CULLING =
        B.comment("Culls distant and non-visible particles to save CPU tick time and GPU fillrate")
         .define("smartParticleCulling", true);

    private static final ForgeConfigSpec.BooleanValue BLOCK_ENTITY_CULLING =
        B.comment("Adaptively culls distant tile entities (chests, signs, banners) in large storage rooms and mob farms")
         .define("blockEntityCulling", true);

    private static final ForgeConfigSpec.IntValue JOIN_GRACE_SECONDS =
        B.comment("Seconds to wait after joining a world before the governor starts judging FPS (0 = no wait)")
         .defineInRange("joinGraceSeconds", 10, 0, 3600);

    private static final ForgeConfigSpec.BooleanValue UNLOCK_FRAMERATE =
        B.comment("Optional benchmark helper: turns VSync off and sets the framerate limit to Max")
         .define("unlockFramerate", false);

    private static final ForgeConfigSpec.BooleanValue ENGINE_DRAWLIST =
        B.comment("EPE engine: use EPE's own chunk draw-list builder (less per-frame CPU work and garbage). Turn off to use the game's version.")
         .define("engineDrawListOptimizer", true);
    private static final ForgeConfigSpec.BooleanValue EARLY_Z =
        B.comment("Sort opaque chunks front-to-back to enable GPU hardware Early-Z depth test rejection (reduces GPU pixel overdraw)")
         .define("earlyZOptimization", true);
    private static final ForgeConfigSpec.BooleanValue FRAME_STATS =
        B.comment("Every 10 seconds in a world, log average FPS, 1% low FPS, worst frame time, section and draw counts to latest.log")
         .define("frameStatsLog", true);

    static final ForgeConfigSpec SPEC = B.build();

    public static volatile boolean adaptive = true;
    public static volatile int targetFps = 60;
    public static volatile double minEntityScale = 0.5;
    public static volatile boolean engineDrawListOptimizer = true;
    public static volatile boolean earlyZOptimization = true;
    public static volatile boolean frameStatsLog = true;
    public static volatile boolean unlockFramerate = false;
    public static volatile int joinGraceSeconds = 10;
    public static volatile boolean adaptiveVisuals = true;
    public static volatile boolean adaptiveSimulationDistance = true;
    public static volatile boolean adaptiveRenderDistance = true;
    public static volatile int minRenderDistance = 8;
    public static volatile int particleBurstCap = 1500;
    public static volatile boolean smartParticleCulling = true;
    public static volatile boolean blockEntityCulling = true;
    public static volatile boolean budgetedUploads = true;
    public static volatile long uploadBudgetNanos = 2_000_000L;
    public static volatile long syncRebuildBudgetNanos = 3_000_000L;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        try {
            adaptive = ADAPTIVE.get();
            targetFps = TARGET_FPS.get();
            minEntityScale = MIN_ENTITY_SCALE.get();
            engineDrawListOptimizer = ENGINE_DRAWLIST.get();
            earlyZOptimization = EARLY_Z.get();
            frameStatsLog = FRAME_STATS.get();
            unlockFramerate = UNLOCK_FRAMERATE.get();
            joinGraceSeconds = JOIN_GRACE_SECONDS.get();
            adaptiveVisuals = ADAPTIVE_VISUALS.get();
            adaptiveSimulationDistance = ADAPTIVE_SIM_DISTANCE.get();
            adaptiveRenderDistance = ADAPTIVE_RENDER_DISTANCE.get();
            minRenderDistance = MIN_RENDER_DISTANCE.get();
            particleBurstCap = PARTICLE_BURST_CAP.get();
            smartParticleCulling = SMART_PARTICLE_CULLING.get();
            blockEntityCulling = BLOCK_ENTITY_CULLING.get();
            budgetedUploads = BUDGET_UPLOADS.get();
            uploadBudgetNanos = (long) (UPLOAD_BUDGET_MS.get() * 1_000_000.0);
            syncRebuildBudgetNanos = (long) (SYNC_REBUILD_BUDGET_MS.get() * 1_000_000.0);
        } catch (IllegalStateException ignored) {
            // not loaded yet
        }
    }

    public static void setTargetFps(int value) {
        targetFps = value;
        try {
            TARGET_FPS.set(value);
            TARGET_FPS.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save targetFps to config", e);
        }
    }

    public static void setAdaptive(boolean value) {
        adaptive = value;
        try {
            ADAPTIVE.set(value);
            ADAPTIVE.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save adaptiveQuality to config", e);
        }
    }

    public static void setEngineDrawListOptimizer(boolean value) {
        engineDrawListOptimizer = value;
        try {
            ENGINE_DRAWLIST.set(value);
            ENGINE_DRAWLIST.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save engineDrawListOptimizer to config", e);
        }
    }

    public static void setEarlyZOptimization(boolean value) {
        earlyZOptimization = value;
        try {
            EARLY_Z.set(value);
            EARLY_Z.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save earlyZOptimization to config", e);
        }
    }

    public static void setBlockEntityCulling(boolean value) {
        blockEntityCulling = value;
        try {
            BLOCK_ENTITY_CULLING.set(value);
            BLOCK_ENTITY_CULLING.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save blockEntityCulling to config", e);
        }
    }

    public static void setBudgetedUploads(boolean value) {
        budgetedUploads = value;
        try {
            BUDGET_UPLOADS.set(value);
            BUDGET_UPLOADS.save();
        } catch (RuntimeException e) {
            EuphoriaPacingExtended.LOGGER.warn("EPE: could not save budgetedUploads to config", e);
        }
    }
}
