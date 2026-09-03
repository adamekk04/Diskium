package org.diskium.management;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.diskium.Diskium;
import org.diskium.utils.FileUtils;
import org.diskium.MultiplatformLogger;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

    public static void delWorld(World world) {
        Bukkit.unloadWorld(world, false); // TODO: Check for side effects that unloading world midtick can have: https://jd.papermc.io/paper/26.2/org/bukkit/Bukkit.html#unloadWorld(org.bukkit.World,boolean)
        FileUtils.del(world.getWorldFolder());
    }

    public static void del(World world, boolean in, int radius, boolean checkForBuilds) {
        Chunk[] chunks = getAllChunks(world, radius, in);
        List<Chunk> chunksQueue = new ArrayList<>();

        for (Chunk chunk : chunks) {
            if (checkForBuilds) {
                World tempWorld = genWorld(world);

                if (compareChunks(chunk, tempWorld.getChunkAt(chunk.getX(), chunk.getZ()))) {
                    chunksQueue.add(chunk);
                }
            } else {
                // TODO: Use List<List<Chunks>>: all: List<regions: List<Chunks>>, for better management with deleting whole regions
                if (WorldUtils.isRegionSafeToDelete(radius, chunk.getX(), chunk.getZ(), in)) {
                    FileUtils.safeDel(
                            FileManagement.getRegionFile(chunk.getX(), chunk.getZ(), world),
                            (boolean) ConfigManagement.getSingleConfig("delete-world-while-running"),
                            Diskium.getInstance().getDataFolder(), "world");
                }
            }
        }

        FileManagement.makeFiles(chunksQueue);
    }

    public static void del(World world, boolean checkForBuilds) {
        Chunk[] chunks = getAllChunks(world);
        List<Chunk> chunksQueue = new ArrayList<>();

        for (Chunk chunk : chunks) {
            if (checkForBuilds) {
                World tempWorld = genWorld(world);

                if (compareChunks(chunk, tempWorld.getChunkAt(chunk.getX(), chunk.getZ()))) {
                    chunksQueue.add(chunk);
                }
            }
        }

        FileManagement.makeFiles(chunksQueue);
    }

    public static void delSector(int x, int z, boolean isChunk, boolean checkForBuilds, World world) {
        World newWorld = genWorld(world);

        if (isChunk) {
            Chunk chunk = world.getChunkAt(x, z);
            if (checkForBuilds) {
                Chunk freshChunk = newWorld.getChunkAt(x, z);
                if (compareChunks(chunk, freshChunk)) {
                    // TODO: somehow delete single chunk
                }
            } else {
                // TODO: somehow delete single chunk
            }
        } else {
            if (checkForBuilds) {
                // TODO: Finish this
            } else {
                FileManagement.getRegionFile(x, z, world);
                FileManagement.makeFiles(x, z, world, false);
            }
        }
    }

    public static boolean compareChunks(Chunk a, Chunk b) {
        ChunkSnapshot sa = a.getChunkSnapshot();
        ChunkSnapshot sb = b.getChunkSnapshot();

        int minY = a.getWorld().getMinHeight();
        int maxY = a.getWorld().getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockData blockA = sa.getBlockData(x, y, z);
                    BlockData blockB = sb.getBlockData(x, y, z);

                    if (!blockA.matches(blockB)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static List<Chunk> getGeneratedChunksInRegion(Chunk ch) {
        int chunkX = ch.getX();
        int chunkZ = ch.getZ();

        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);

        List<Chunk> chunks = new ArrayList<>();

        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                int targetX = regionX * 32 + x;
                int targetZ = regionZ * 32 + z;

                Chunk chunk = ch.getWorld().getChunkAt(targetX, targetZ);

                if (chunk.isGenerated()) {
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }


    private static Chunk[] getAllChunks(World world, int radius, boolean in) {
        int chunkRadius = WorldUtils.blockToChunk(radius);
        List<Chunk> allChunks = new ArrayList<>();

        // TODO: Use custom chunk getter from region files and don't use API, which is extremely slow
        if (in) {
            for (int x = -chunkRadius; x < chunkRadius; x++) {
                for (int z = -chunkRadius; z < chunkRadius; z++) {
                    if (world.isChunkGenerated(x, z)) allChunks.add(world.getChunkAt(x, z));
                }
            }
        } else {
            int border = (WorldUtils.blockToChunk(world.getWorldBorder().getSize()) + 1) / 2;
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

    private static Chunk[] getAllChunks(World world) {
        int border = (int) world.getWorldBorder().getSize() / 2;
        List<Chunk> chunks = new ArrayList<>();

        for (int x = -border; x < border; x++) {
            for (int z = -border; z < border; z++) {
                Chunk ch = world.getChunkAt(x, z);
                if (ch.isGenerated()) {
                    chunks.add(ch);
                }
            }
        }

        return chunks.toArray(Chunk[]::new);
    }
}
