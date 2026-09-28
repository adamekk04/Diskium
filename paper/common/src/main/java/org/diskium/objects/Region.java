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

    public Region(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

    public Region(Chunk chunk) {
        int[] coords = WorldUtils.chunkToRegion(chunk.getX(), chunk.getZ());
        this.x = coords[0];
        this.z = coords[1];
        this.world = chunk.getWorld();
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

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

    public boolean canDeleteEntireRegion(Map<Chunk, Boolean> chunks) {
        return this.chunks.stream().allMatch(chunk -> Boolean.TRUE.equals(chunks.get(chunk)));
    }
}
