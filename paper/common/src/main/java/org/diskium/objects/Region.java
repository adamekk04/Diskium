package org.diskium.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.management.WorldManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Region {
    int x;
    int z;
    World world;
    List<Chunk> chunks = new ArrayList<>();

    public Region(int x, int z, World world) {
        this(x, z, world, null);
        this.chunks = WorldManagement.getGeneratedChunksInRegion(this);
    }

    Region(int x, int z, World world, List<Chunk> chunks) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.chunks = chunks == null ? new ArrayList<>() : new ArrayList<>(chunks);
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
