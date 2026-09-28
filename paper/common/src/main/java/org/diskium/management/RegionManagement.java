package org.diskium.management;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.mca.Sector;
import org.diskium.objects.Region;
import org.diskium.utils.WorldUtils;

import java.io.File;

public class RegionManagement {
    public static Region getRegion(int x, int z, World world) {
        return new Region(x, z, world);
    }

    public static Region getRegion(Chunk chunk) {
        int[] coordinates = WorldUtils.chunkToRegion(chunk.getX(), chunk.getZ());
        return new Region(coordinates[0], coordinates[1], chunk.getWorld());
    }

    public static Region getRegion(File file, World world) {
        Sector sector = new Sector(file);

        return new Region(sector.getX(), sector.getZ(), world);
    }
}
