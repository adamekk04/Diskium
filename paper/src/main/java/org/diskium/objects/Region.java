package org.diskium.objects;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Region {
    int x;
    int z;
    World world;
    final List<Chunk> chunks = new ArrayList<>();

    public Region(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
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

    public void addChunks(List<Chunk> chunks) {
        this.chunks.addAll(chunks);
    }

    public List<Chunk> toChunks() {
        return this.chunks;
    }

    public boolean fullDel(Map<Chunk, Boolean> chunks) {
        int amount = this.chunks.size();
        int buffer = 0;

        for (Chunk chunk : this.chunks) {
            if (chunks.containsKey(chunk)) {
                buffer++;
            }
        }

        return amount == buffer;
    }
}
