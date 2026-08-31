package org.diskium.management;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.RegionObj;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagement {
    public static File getRegionFile(int x, int z, World world) {
        return new File(world.getWorldFolder(), "region/r." + x + "." + z + ".mca");
    }

    public static void makeFiles(List<Chunk> chunks) {
        List<RegionObj> regions = new ArrayList<>();

        while (!chunks.isEmpty()) {
            Chunk target = chunks.getFirst();
            List<Chunk> inRegion = WorldManagement.getGeneratedChunksInRegion(target);
            for (Chunk chunk : inRegion) {
                chunks.remove(chunk);
            }
            regions.add(new RegionObj(target.getX(), target.getZ()));
        }

        // TODO: Finish this
    }

    public static void makeFiles(int x, int z, World world, boolean isChunk) {
        // TODO: Finish this
    }
}
