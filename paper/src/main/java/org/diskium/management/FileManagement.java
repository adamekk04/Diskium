package org.diskium.management;

import org.bukkit.World;

import java.io.File;

public class FileManagement {
    public static File getRegionFile(int x, int z, World world) {
        return new File(world.getWorldFolder(), "region/r." + x + "." + z + ".mca");
    }
}
