package org.diskium.management;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.diskium.Diskium;
import org.diskium.utils.FileUtils;
import org.diskium.utils.WorldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Bukkit.unloadWorld(world, false);
        FileUtils.del(world.getWorldFolder());
    }

    public static void del(World world, boolean in, int radius, boolean checkForBuilds) {
        Chunk[] chunks = getAllChunks(world, radius, in);
        Map<Chunk, Boolean> chunksQueue = new HashMap<>();

        if (checkForBuilds) {
            World tempWorld = genWorld(world);

            for (Chunk chunk : chunks) {
                chunksQueue.put(chunk, compareChunks(chunk, tempWorld.getChunkAt(chunk.getX(), chunk.getZ())));
            }
        } else {
            for (Chunk chunk : chunks) {
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
        Map<Chunk, Boolean> chunksQueue = new HashMap<>();

        if (checkForBuilds) {
            World tempWorld = genWorld(world);

            for (Chunk chunk : chunks) {
                chunksQueue.put(chunk, compareChunks(chunk, tempWorld.getChunkAt(chunk.getX(), chunk.getZ())));
            }
        } else {
            for (Chunk chunk : chunks) {
                chunksQueue.put(chunk, true);
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
        int regionRadius = WorldUtils.blockToRegion(radius);
        List<Chunk> allChunks = new ArrayList<>();

        if (in) {
            for (int x = -regionRadius; x < regionRadius; x++) {
                for (int z = -regionRadius; z < regionRadius; z++) {
                    allChunks.addAll(RegionManagement.getRegion(x, z, world).toChunks());
                }
            }
        } else {
            int border = (WorldUtils.blockToChunk(world.getWorldBorder().getSize()) + 1) / 2;
            for (int x = -border; x < border; x++) {
                for (int z = -border; z < border; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) > radius) {
                        allChunks.addAll(RegionManagement.getRegion(x, z, world).toChunks());
                    }
                }
            }
        }

        return allChunks.toArray(Chunk[]::new);
    }

    private static Chunk[] getAllChunks(World world) { // TODO: Optimize, with mca parser
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
