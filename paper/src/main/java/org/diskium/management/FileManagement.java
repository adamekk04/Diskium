package org.diskium.management;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.Diskium;
import org.diskium.objects.Region;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileManagement {
    public static File getRegionFile(int x, int z, World world) {
        return new File(world.getWorldFolder(), "region/r." + x + "." + z + ".mca");
    }

    public static File getRegionFile(Region region) {
        return new File(region.getWorld().getWorldFolder(), "region/r." + region.getX() + "." + region.getZ() + ".mca");
    }

    public static void makeFiles(Map<Chunk, Boolean> chunks) {
        Map<Integer, Integer> regions = new HashMap<>();
        Map<Region, Boolean> del = new HashMap<>(); // true: unsafe; false: safe

        for (Map.Entry<Chunk, Boolean> entry : chunks.entrySet()) {
            Region region = RegionManagement.getRegion(entry.getKey());

            if (region.fullDel(chunks)) {
                del.put(region, false);
            } else {
                del.put(region, true);
            }
        }

        if ((boolean) ConfigManagement.getSingleConfig("delete-while-running.world")) {
            for (Map.Entry<Region, Boolean> entry : del.entrySet()) {
                if (!entry.getValue()) {
                    FileUtils.del(getRegionFile(entry.getKey()));
                }
            }
        } else {
            for (Map.Entry<Region, Boolean> entry : del.entrySet()) {
                if (!entry.getValue()) {
                    TasksUtils.add(Diskium.getInstance().getDataFolder(), new TaskObj(true, getRegionFile(entry.getKey()), null, "World"));
                }
            }
        }
    }

    public static void makeFiles(int x, int z, World world, boolean isChunk) {
        // TODO: Finish this
    }
}
