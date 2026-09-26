package org.diskium.management;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.Diskium;
import org.diskium.mca.Parser;
import org.diskium.objects.Region;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileManagement {
    public static File getRegionFile(int x, int z, World world) {
        int[] reg = WorldUtils.chunkToRegion(x, z);

        return new File(world.getWorldFolder(), "region/r." + reg[0] + "." + reg[1] + ".mca");
    }

    public static File getRegionFile(Region region) {
        return new File(region.getWorld().getWorldFolder(), "region/r." + region.getX() + "." + region.getZ() + ".mca");
    }

    public static void makeFiles(Map<Chunk, Boolean> chunks) {
        Map<Region, Boolean> del = new HashMap<>(); // true: unsafe; false: safe

        for (Map.Entry<Chunk, Boolean> entry : chunks.entrySet()) {
            Region region = RegionManagement.getRegion(entry.getKey());

            if (region.fullDel(chunks)) {
                del.put(region, false);
            } else {
                del.put(region, true);
            }
        }

        for (Map.Entry<Region, Boolean> entry : del.entrySet()) {
            if (!entry.getValue()) {
                FileUtils.safeDel(getRegionFile(entry.getKey()), FileUtils.DelSpecifier.WORLD);
            }
        }
    }

    public static boolean makeFiles(int x, int z, World world, boolean isChunk) {
        if (isChunk) {
            if (FileUtils.getDeleteWhileRunningWorld()) {
                File regionFile = getRegionFile(x, z, world);
                File taskSource = createTaskSource(regionFile);
                boolean success = true;

                success = Parser.removeChunk(x, z, taskSource) && success;
                return TasksUtils.add(new TaskObj(false, taskSource, regionFile, TaskObj.Types.WORLD)) && success;
            } else {
                int[] coords = WorldUtils.chunkToRegion(x, z);
                Chunk chunk = world.getChunkAt(coords[0], coords[1]);

                chunk.unload(true);

                return Parser.removeChunk(x, z, getRegionFile(coords[0], coords[1], world));
            }
        } else {
            return FileUtils.safeDel(getRegionFile(x, z, world), FileUtils.DelSpecifier.WORLD);
        }
    }

    private static File createTaskSource(File file) {
        File taskSourceRoot = new File(Diskium.getInstance().getDataFolder(), "taskSource");
        taskSourceRoot.mkdir();
        File taskSource = new File(taskSourceRoot, file.getName());

        FileUtils.move(file, taskSource);

        return taskSource;
    }
}
