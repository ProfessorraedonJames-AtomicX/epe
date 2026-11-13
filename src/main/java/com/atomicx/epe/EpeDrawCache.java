package com.atomicx.epe;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * Reusable objects for the chunk draw-list builder, so the renderer does not
 * allocate a new uploader lambda for every section on every frame.
 * Render thread only.
 */
public final class EpeDrawCache {
    private static final ChunkSectionLayer[] LAYERS = ChunkSectionLayer.values(); // values() clones; cache once

    @SuppressWarnings("unchecked")
    private static BiConsumer<GpuBufferSlice[], RenderPass.UniformUploader>[] uploaders = new BiConsumer[2048];

    private EpeDrawCache() {}

    public static ChunkSectionLayer[] layers() {
        return LAYERS;
    }

    /** Uploader that binds the "ChunkSection" uniform slice number {@code index}. */
    public static BiConsumer<GpuBufferSlice[], RenderPass.UniformUploader> uploader(int index) {
        BiConsumer<GpuBufferSlice[], RenderPass.UniformUploader>[] array = uploaders;
        if (index >= array.length) {
            array = Arrays.copyOf(array, Math.max(index + 1, array.length * 2));
            uploaders = array;
        }
        BiConsumer<GpuBufferSlice[], RenderPass.UniformUploader> uploader = array[index];
        if (uploader == null) {
            uploader = (slices, up) -> up.upload("ChunkSection", slices[index]);
            array[index] = uploader;
        }
        return uploader;
    }
}
