package com.atomicx.epe;

import java.util.Arrays;

/**
 * Frame-time monitor. Called once per rendered frame (from the chunk draw-list
 * builder). Every 10 seconds it logs average FPS, 1% low FPS, the worst frame
 * time and how many chunk sections / draw calls were rendered.
 *
 * Automatically bridges real-time metrics to Euphoria Pacing Bridge if present.
 */
public final class EpePacing {
    private static final long WINDOW_NANOS = 10_000_000_000L;
    private static final long[] SAMPLES = new long[32768];

    private static long lastFrame, windowStart;
    private static int count, lastSections, lastDraws;

    private EpePacing() {}

    public static void onFrame(int sections) {
        if (!EpeConfig.frameStatsLog) return;
        long now = System.nanoTime();
        lastSections = sections;
        if (lastFrame != 0L) {
            long dt = now - lastFrame;
            if (dt < 2_000_000_000L && count < SAMPLES.length) { // ignore pauses / loading screens
                SAMPLES[count++] = dt;
            }
        }
        lastFrame = now;
        if (windowStart == 0L) windowStart = now;
        if (now - windowStart >= WINDOW_NANOS) {
            report();
            count = 0;
            windowStart = now;
        }
    }

    public static void setDraws(int draws) {
        lastDraws = draws;
    }

    private static void report() {
        if (count < 10) return;
        long sum = 0;
        for (int i = 0; i < count; i++) sum += SAMPLES[i];
        double avgNanos = (double) sum / count;
        Arrays.sort(SAMPLES, 0, count);
        long worst = SAMPLES[count - 1];
        int tail = Math.max(1, count / 100);
        long tailSum = 0;
        for (int i = count - tail; i < count; i++) tailSum += SAMPLES[i];
        double lowNanos = (double) tailSum / tail;

        int avgFps = (int) Math.round(1.0e9 / avgNanos);
        int lowFps = (int) Math.round(1.0e9 / lowNanos);

        EuphoriaPacingExtended.LOGGER.info(
            "EPE frame stats (10s): {} FPS avg | {} FPS 1% low | worst frame {} ms | sections {} | draws {}",
            avgFps, lowFps,
            String.format("%.1f", worst / 1.0e6), lastSections, lastDraws);

        // Optional bridge integration: report directly to Euphoria Pacing Bridge if installed
        try {
            Class<?> bridge = Class.forName("com.atomicx.euphoriapacingbridge.api.BridgeState");
            bridge.getMethod("updateMetrics", int.class, int.class, long.class)
                  .invoke(null, avgFps, lowFps, worst);
        } catch (Throwable ignored) {
            // Bridge not present, standalone mode
        }
    }
}
