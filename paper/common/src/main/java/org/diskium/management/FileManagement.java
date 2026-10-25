package org.diskium.management;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.diskium.Diskium;
import org.diskium.MultiplatformLogger;
import org.diskium.mca.Parser;
import org.diskium.objects.BackupObj;
import org.diskium.objects.Region;
import org.diskium.objects.TaskObj;
import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileManagement {
    /**
     * Gets a .mca region file from given world and coordinates.
     *
     * @param x     X coordinate of the chunk.
     * @param z     Z coordinate of the chunk.
     * @param world World whose region directory contains the file.
     * @return Region file from given coordinates and world.
     */
    public static File getRegionFile(int x, int z, World world) {
        int[] reg = WorldUtils.chunkToRegion(x, z);

        return new File(world.getWorldFolder(), "region/r." + reg[0] + "." + reg[1] + ".mca");
    }

    /**
     * Gets a .mca region file from given region.
     *
     * @param region Region object.
     * @return Region file from given region.
     */
    public static File getRegionFile(Region region) {
        return new File(region.getWorld().getWorldFolder(), "region/r." + region.getX() + "." + region.getZ() + ".mca");
    }

    /**
     * Safely deletes all region files of all given chunks.
     *
     * @param chunks All chunks to get checked and safely deleted.
     */
    public static void deleteRegions(Map<Chunk, Boolean> chunks) {
        Map<Region, Boolean> del = new HashMap<>(); // true: safe; false: unsafe

        for (Map.Entry<Chunk, Boolean> entry : chunks.entrySet()) {
            Region region = new Region(entry.getKey());

            if (region.canDeleteEntireRegion(chunks)) {
                del.put(region, true);
            } else {
                del.put(region, false);
            }
        }

        for (Map.Entry<Region, Boolean> entry : del.entrySet()) {
            if (entry.getValue()) {
                FileUtils.safeDel(getRegionFile(entry.getKey()), FileUtils.DelSpecifier.USE_TASK_QUEUE);
            }
        }
    }

    /**
     * Safely deletes a sector in a given world from coordinates.
     *
     * @param x       X coordinate of the sector.
     * @param z       Z coordinate of the sector.
     * @param world   World the sector is located in.
     * @param isChunk Whether the sector is a chunk or a region.
     */
    public static void makeFiles(int x, int z, World world, boolean isChunk) {
        if (isChunk) {
            Chunk chunk = world.getChunkAt(x, z);

            chunk.unload(false);

            File pluginDir = Diskium.getInstance().getDataFolder();
            File worldCopies = new File(pluginDir, "world-copies/" + WorldUtils.getSalt());

            if (!worldCopies.mkdirs()) {
                MultiplatformLogger.error("Something went wrong while creating world-copies folder");
                return;
            }

            Region regionObj = new Region(chunk);
            File originalRegion = getRegionFile(regionObj);
            File region = new File(worldCopies, originalRegion.getName());

            Optional<TaskObj> task = Arrays.stream(TasksUtils.getTasks()).filter(taskObj -> !taskObj.getDelete() && originalRegion.equals(taskObj.getReplacementFile())).findFirst();
            if (task.isPresent()) {
                region = task.get().getFile();
            } else {
                if (!FileUtils.copy(originalRegion, region)) {
                    return;
                }

            }

            if (!FileUtils.createBackup(originalRegion)) {
                return;
            }

            if (!Parser.removeChunk(x, z, region)) {
                return;
            }

            if (task.isEmpty()) {
                TasksUtils.add(new TaskObj(false, region, originalRegion, TaskObj.Types.WORLD));
            }
        } else {
            int chunkX = WorldUtils.regionToChunk(x);
            int chunkZ = WorldUtils.regionToChunk(z);

            FileUtils.safeDel(getRegionFile(chunkX, chunkZ, world), FileUtils.DelSpecifier.USE_TASK_QUEUE);
        }
    }

    /**
     * Removes selected chunks from a region copy and queues replacement of the original.
     * Attempts a separate backup if enabled in the configuration.
     *
     * <p>All chunks must belong to the same region and world.
     *
     * @param safeToDelete chunks mapped to {@code true} for removal or {@code false}
     *                     for retention; keys and values must be non-null
     */
    public static void makeFiles(Map<Chunk, Boolean> safeToDelete) {
        File pluginDir = Diskium.getInstance().getDataFolder();
        File worldCopies = new File(pluginDir, "world-copies/" + WorldUtils.getSalt());

        if (!worldCopies.mkdirs()) {
            MultiplatformLogger.error("Something went wrong while creating world-copies folder");
            return;
        }

        if (safeToDelete.isEmpty()) {
            MultiplatformLogger.error("Got empty map of chunks");
            return;
        }

        Region regionObj = new Region(safeToDelete.keySet().iterator().next());
        File originalRegion = getRegionFile(regionObj);
        File region = new File(worldCopies, originalRegion.getName());

        Optional<TaskObj> task = Arrays.stream(TasksUtils.getTasks()).filter(taskObj -> !taskObj.getDelete() && originalRegion.equals(taskObj.getReplacementFile())).findFirst();
        if (task.isPresent()) {
            region = task.get().getFile();
        } else {
            if (!FileUtils.copy(originalRegion, region)) {
                return;
            }
        }

        if (!FileUtils.createBackup(originalRegion)) {
            return;
        }

        for (Map.Entry<Chunk, Boolean> entry : safeToDelete.entrySet()) {
            Chunk chunk = entry.getKey();
            if (entry.getValue()) {
                if (!Parser.removeChunk(chunk.getX(), chunk.getZ(), region)) {
                    return;
                }
            }
        }

        if (task.isEmpty()) {
            TasksUtils.add(new TaskObj(false, region, originalRegion, TaskObj.Types.WORLD));
        }
    }
}
