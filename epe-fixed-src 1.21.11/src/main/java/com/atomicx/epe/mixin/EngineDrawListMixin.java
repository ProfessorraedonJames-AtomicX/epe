package com.atomicx.epe.mixin;

import com.atomicx.epe.EpeConfig;
import com.atomicx.epe.EpeDrawCache;
import com.atomicx.epe.EpePacing;
import com.atomicx.epe.EpeSectionSorter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * EPE chunk draw-list builder (engine phase 1 & 2).
 *
 * Optimizes per-frame chunk draw lists:
 * - Shared matrix across sections instead of per-section allocations.
 * - Hoisted clock and texture atlas lookups out of the render loop.
 * - Zero-allocation cached uniform uploaders.
 * - Front-to-back Early-Z depth sorting for opaque geometry, letting GPU hardware
 *   discard hidden terrain before running fragment shaders.
 * - Back-to-front sorting for translucent geometry for correct alpha blending.
 * - Pruning of empty (0 index count) buffers to eliminate dead draw calls.
 * - Zero-reload dynamic distance culling when commanded by Euphoria Pacing Bridge.
 */
@Mixin(LevelRenderer.class)
public abstract class EngineDrawListMixin {
    @Shadow @Final private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;
    @Shadow @Final private Minecraft minecraft;

    private static final DynamicUniforms.ChunkSectionInfo[] EMPTY_INFOS = new DynamicUniforms.ChunkSectionInfo[0];

    @SuppressWarnings("unchecked")
    @Inject(method = "prepareChunkRenders", at = @At("HEAD"), cancellable = true, require = 0)
    private void epe$prepareChunkRenders(Matrix4fc view, double camX, double camY, double camZ,
                                         CallbackInfoReturnable<ChunkSectionsToRender> cir) {
        ObjectArrayList<SectionRenderDispatcher.RenderSection> sections = this.visibleSections;
        int count = sections.size();
        EpePacing.onFrame(count);
        if (!EpeConfig.engineDrawListOptimizer) return; // run the game's own version

        ChunkSectionLayer[] layers = EpeDrawCache.layers();
        List<RenderPass.Draw<GpuBufferSlice[]>>[] lists = new List[layers.length];
        EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> byLayer = new EnumMap<>(ChunkSectionLayer.class);
        for (int i = 0; i < layers.length; i++) {
            lists[i] = new ArrayList<>(count);
            byLayer.put(layers[i], lists[i]);
        }

        List<DynamicUniforms.ChunkSectionInfo> infos = new ArrayList<>(count);
        GpuTextureView atlas = this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        int atlasWidth = atlas.getWidth(0);
        int atlasHeight = atlas.getHeight(0);
        long now = Util.getMillis();
        Matrix4f sharedMatrix = new Matrix4f(view);
        int maxIndices = 0;
        int draws = 0;

        // Query optional dynamic distance culling scale from Euphoria Pacing Bridge
        double bridgeDistanceScale = 1.0;
        try {
            Class<?> bridge = Class.forName("com.atomicx.euphoriapacingbridge.api.BridgeState");
            bridgeDistanceScale = (double) bridge.getMethod("getEngineDistanceScale").invoke(null);
        } catch (Throwable ignored) {}

        // Horizontal-only (Chebyshev) culling radius in blocks. Vanilla's render area is a square of
        // chunk columns with full world height, so vertical distance must never cull a section.
        // The radius never drops below EpeConfig.minRenderDistance (the player's chosen floor).
        double maxBlockDist = Double.MAX_VALUE;
        if (bridgeDistanceScale < 1.0) {
            int rd = this.minecraft.options.getEffectiveRenderDistance();
            double floorChunks = Math.min(rd, EpeConfig.minRenderDistance);
            double scaledChunks = Math.max(floorChunks, rd * bridgeDistanceScale);
            // +1 chunk so sections straddling the edge never pop out
            maxBlockDist = (scaledChunks + 1.0) * 16.0;
        }

        // Front-to-back sort for Early-Z depth test optimization
        int[] sortedIndices = null;
        if (EpeConfig.earlyZOptimization && count > 1) {
            sortedIndices = EpeSectionSorter.sortFrontToBack(sections, count, camX, camY, camZ);
        }

        // Section info map to track uniform indices
        int[] sectionToInfoIndex = new int[count];
        for (int i = 0; i < count; i++) {
            sectionToInfoIndex[i] = -1;
        }

        for (int li = 0; li < layers.length; li++) {
            ChunkSectionLayer layer = layers[li];
            boolean isTranslucent = (layer == ChunkSectionLayer.TRANSLUCENT);

            for (int s = 0; s < count; s++) {
                int sectionIdx = sortedIndices != null
                    ? (isTranslucent ? sortedIndices[count - 1 - s] : sortedIndices[s])
                    : s;

                SectionRenderDispatcher.RenderSection section = sections.get(sectionIdx);
                if (section == null) continue;

                // Dynamic distance culling check (zero reload spikes)
                BlockPos origin = section.getRenderOrigin();
                if (origin != null && maxBlockDist < Double.MAX_VALUE) {
                    double dx = Math.abs((origin.getX() + 8.0) - camX);
                    double dz = Math.abs((origin.getZ() + 8.0) - camZ);
                    if (Math.max(dx, dz) > maxBlockDist) {
                        continue;
                    }
                }

                SectionMesh mesh = section.getSectionMesh();
                if (mesh == null) continue;

                SectionBuffers buffers = mesh.getBuffers(layer);
                if (buffers == null || buffers.getIndexCount() <= 0) continue;

                int infoIndex = sectionToInfoIndex[sectionIdx];
                if (infoIndex == -1) {
                    infoIndex = infos.size();
                    sectionToInfoIndex[sectionIdx] = infoIndex;
                    if (origin == null) origin = BlockPos.ZERO;
                    infos.add(new DynamicUniforms.ChunkSectionInfo(
                        sharedMatrix, origin.getX(), origin.getY(), origin.getZ(),
                        section.getVisibility(now), atlasWidth, atlasHeight));
                }

                GpuBuffer indexBuffer = buffers.getIndexBuffer();
                VertexFormat.IndexType indexType = null;
                if (indexBuffer == null) {
                    if (buffers.getIndexCount() > maxIndices) maxIndices = buffers.getIndexCount();
                } else {
                    indexType = buffers.getIndexType();
                }

                lists[li].add(new RenderPass.Draw<>(
                    0, buffers.getVertexBuffer(), indexBuffer, indexType,
                    0, buffers.getIndexCount(), EpeDrawCache.uploader(infoIndex)));
                draws++;
            }
        }

        GpuBufferSlice[] slices = RenderSystem.getDynamicUniforms()
            .writeChunkSections(infos.isEmpty() ? EMPTY_INFOS : infos.toArray(EMPTY_INFOS));
        EpePacing.setDraws(draws);
        cir.setReturnValue(new ChunkSectionsToRender(atlas, byLayer, maxIndices, slices));
    }
}
