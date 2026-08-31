package org.diskium.management;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManagement {
    public static File getRegionFile(int x, int z, World world) {
        return new File(world.getWorldFolder(), "region/r." + x + "." + z + ".mca");
    }

    public static void makeFiles(List<Chunk> chunks) {
        Map<Integer, Integer> regions = new HashMap<>();

        while (!chunks.isEmpty()) {
            Chunk target = chunks.getFirst();
            List<Chunk> inRegion = WorldManagement.getGeneratedChunksInRegion(target);
            for (Chunk chunk : inRegion) {
                chunks.remove(chunk);
            }
            regions.put(target.getX(), target.getZ());
        }

        // TODO: Finish this
    }

    public static void makeFiles(int x, int z, World world, boolean isChunk) {
        // TODO: Finish this
    }
}
