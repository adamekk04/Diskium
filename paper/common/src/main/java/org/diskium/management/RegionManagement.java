package org.diskium.management;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.objects.Region;

import java.io.File;

public class RegionManagement {
    public static Region getRegion(int x, int z, World world) {
        Region region = new Region(x, z, world);
        region.addChunks(WorldManagement.getGeneratedChunksInRegion(world.getChunkAt(x * 32, z * 32)));
        return region;
    }

    public static Region getRegion(Chunk chunk) {
        Region region = new Region(chunk.getX(), chunk.getZ(), chunk.getWorld());
        region.addChunks(WorldManagement.getGeneratedChunksInRegion(chunk));
        return region;
    }

    public static Region getRegion(File file, World world) {
        String[] parts = file.getName().replace("r.", "").replace(".mca", "").split("\\.");

        int x = Integer.parseInt(parts[0]);
        int z = Integer.parseInt(parts[1]);

        Region region = new Region(x, z, world);
        region.addChunks(WorldManagement.getGeneratedChunksInRegion(region));
        return region;
    }
}
