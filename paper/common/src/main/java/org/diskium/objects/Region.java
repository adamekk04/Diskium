package org.diskium.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.management.WorldManagement;
import org.diskium.mca.Sector;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Region {
    int x;
    int z;
    World world;
    List<Chunk> chunks = new ArrayList<>();

/**
     * Creates a region.
     *
     * @param x X coordinate.
     * @param z Z coordinate.
     * @param world World the region is located in.
     */
    public Region(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

/**
     * Creates a region.
     *
     * @param chunk Chunk, that is located inside the target region.
     */
    public Region(Chunk chunk) {
        int[] coords = WorldUtils.chunkToRegion(chunk.getX(), chunk.getZ());
        this.x = coords[0];
        this.z = coords[1];
        this.world = chunk.getWorld();
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

/**
     * Creates a region.
     *
     * @param file A region file.
     * @param world The world containing the region.
     */
    public Region(File file, World world) {
        Sector sector = new Sector(file);
        this.x = sector.getX();
        this.z = sector.getZ();
        this.world = world;
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public World getWorld() {
        return this.world;
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

/**
     * Checks that every chunk recorded in this region is marked for deletion.
     *
     * @param chunks deletion decisions; missing entries count as {@code false}
     * @return {@code true} if every recorded chunk maps to {@code true},
     *         including when the region has no recorded chunks
     */
    public boolean canDeleteEntireRegion(Map<Chunk, Boolean> chunks) {
        return this.chunks.stream().allMatch(chunk -> Boolean.TRUE.equals(chunks.get(chunk)));
    }
}
