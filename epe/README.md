# Euphoria Pacing Extended (EPE) - by AtomicX
Forge 1.21.11 (61.2.4). Original code. JDK 21.

    ./gradlew runClient
    ./gradlew build        # jar in build/libs

Config: `config/euphoria_pacing_extended-client.toml`

## Features & Optimizations
- **EPE Engine Draw-List Optimizer (Phase 1)**:
  - Custom builder for per-frame chunk draw lists (`prepareChunkRenders`).
  - Uses a single shared model-view matrix across sections instead of allocating one per section.
  - Hoists clock and texture atlas lookups out of the loop.
  - Uses presized layer lists and zero-allocation cached uniform uploaders (`EpeDrawCache`).
  - Toggle in Video Settings or config: `engineDrawListOptimizer`.
- **Hardware Early-Z Depth Sorter (Phase 2)**:
  - Sorts opaque terrain sections front-to-back using a zero-allocation primitive long packed-key sort (`EpeSectionSorter`).
  - Allows modern GPU Early-Z hardware rasterizers to discard occluded terrain fragments before executing fragment shaders and texture lookups.
  - Reverses sorting for translucent layers for pixel-perfect water/glass alpha blending.
  - Toggle in Video Settings or config: `earlyZOptimization`.
- **Dead Draw Call Pruning**:
  - Automatically filters out 0-quad / 0-index empty buffer submissions.
- **Adaptive Block Entity Distance Culling**:
  - Dynamically scales rendering distance for chests, signs, banners, and skulls in large storage rooms and mob farms.
- **Intelligent Adaptive Quality Governor**:
  - Samples frame times with an exponential moving average (EMA).
  - VSync and framerate limit aware: never gets trapped into permanent quality drops on 60Hz/75Hz displays.
  - Anti-freeze cooldown prevents recursive chunk rebuilding spikes.
- **Elastic Chunk Mesh Upload Budgeting**:
  - Spreads GPU buffer uploads across frames with dynamic backlog scaling to eliminate terrain holes during fast movement.
- **Proximity-Aware Chunk Rebuilds**:
  - Instant synchronous rebuilds for blocks directly mined or placed by the player.
  - Smoothly offloads background world generation sections to background worker threads.
- **Smart Particle Culling & Burst Cap**:
  - Distance-culls ambient particles out of the player's view cone while capping per-tick burst queues.
- **Frame-Time Stats Monitor**:
  - Every 10 seconds, logs rolling average FPS, 1% low FPS, worst frame time, section count, and draw call count to `latest.log`.
- **Full Compatibility with Sodium / Embeddium**:
  - Non-destructive mixin hooks (`require = 0`) yield gracefully if Sodium is detected.
