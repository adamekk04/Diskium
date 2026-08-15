package org.diskium.management;

import org.bukkit.*;
import org.diskium.Diskium;
import org.diskium.FileUtils;
import org.diskium.WorldUtils;

import java.util.ArrayList;
import java.util.List;

public class WorldManagement {
    public static String getBlock(Location loc, boolean existing) {
        if (existing) {
            return loc.getWorld().getName() + ": " + loc.getBlock().getType().toString();
        } else {
            World newWorld = genWorld(loc.getWorld());
            String toReturn = loc.getWorld().getName() + ": " + newWorld.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getType().toString();
            delWorld(newWorld);
            return toReturn;
        }
    }

    public static World genWorld(World template) {
        WorldCreator creator = new WorldCreator(template.getName() + WorldUtils.getSalt());
        creator.copy(template);
        return creator.createWorld();
    }

    public static String info(World world) {
        return "Name: " + world.getName() + "\nPlayers: " + world.getPlayers() + " (" + world.getPlayerCount() + ")\nSeed: " + world.getSeed() + "\nWorld border radius: " + world.getWorldBorder().getSize();
    }

    public static boolean delWorld(World world) {
        Bukkit.unloadWorld(world, false); // TODO: Check for side effects that unloading world midtick can have: https://jd.papermc.io/paper/26.2/org/bukkit/Bukkit.html#unloadWorld(org.bukkit.World,boolean)
        return world.getWorldFolder().delete(); // TODO: Provide more information, if something fails while deleting the world
    }

    public static boolean del(World world, boolean in, int radius, boolean checkForBuilds) {
        Chunk[] chunks = getAllChunks(world, radius, in);

        for (Chunk chunk : chunks) {
            if (checkForBuilds) {
                World tempWorld = genWorld(world);
                // continue: make compare system
            } else {
                // TODO: Use List<List<Chunks>>: all: List<regions: List<Chunks>>, for better management with deleting whole regions
                if (WorldUtils.isRegionSafeToDelete(radius, chunk.getX(), chunk.getZ(), in)) {
                    FileUtils.del(
                            FileManagement.getRegionFile(chunk.getX(), chunk.getZ(), world),
                            (boolean) ConfigManagement.getSingleConfig("delete-world-while-running"),
                            Diskium.getInstance().getDataFolder());
                }
            }
        }


    }


    private static Chunk[] getAllChunks(World world, int radius, boolean in) {
        int chunkRadius = WorldUtils.chunkParser(radius);
        List<Chunk> allChunks = new ArrayList<>();

        // TODO: Use custom chunk getter from region files and don't use API, which is extremely slow
        if (in) {
            for (int x = -chunkRadius; x < chunkRadius; x++) {
                for (int z = -chunkRadius; z < chunkRadius; z++) {
                    if (world.isChunkGenerated(x, z)) allChunks.add(world.getChunkAt(x, z));
                }
            }
        } else {
            int border = (WorldUtils.chunkParser(world.getWorldBorder().getSize()) + 1) / 2;
            for (int x = -border; x < border; x++) {
                for (int z = -border; z < border; z++) {
                    if (Math.min(Math.abs(x), Math.abs(z)) > radius) {
                        allChunks.add(world.getChunkAt(x, z));
                    }
                }
            }
        }

        return allChunks.toArray(Chunk[]::new);
    }

    private static boolean worldDel(World world, Chunk[] chunks, boolean in, boolean checkForBuilds) {

    }
}
