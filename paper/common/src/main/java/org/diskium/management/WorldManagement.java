package org.diskium.management;

import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.diskium.mca.Parser;
import org.diskium.mca.Sector;
import org.diskium.objects.Region;
import org.diskium.utils.FileUtils;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WorldManagement {

/**
     * Gets a block from given location.
     *
     * @param loc Location of the block.
     * @param existing Whether the world should be taken from the location or should be created a new one.
     * @return A {@code CompletableFuture<TextComponent>}, that can be sent to a sender.
     */
    public static CompletableFuture<TextComponent> getBlock(Location loc, boolean existing) {
        if (existing) {
            return CompletableFuture.completedFuture(Component.text(loc.getWorld().getName(), NamedTextColor.DARK_GREEN)
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(Component.text(loc.getBlock().getType().toString(), NamedTextColor.GREEN)));
        }

        World world = genWorld(loc.getWorld());
        Location clonedLoc = loc.clone();
        clonedLoc.setWorld(world);

        return getBlockAsync(clonedLoc).thenApply(block -> {
            TextComponent component = Component.text(loc.getWorld().getName(), NamedTextColor.DARK_GREEN)
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(Component.text(block.toString(), NamedTextColor.GREEN));

            delWorld(world);

            return component;
        });
    }

/**
     * Generates a new world from its template
     *
     * @param template A world template, from which it gets all properties.
     * @return A new generated world from the template.
     */
    public static World genWorld(World template) {
        WorldCreator creator = new WorldCreator(template.getName() + WorldUtils.getSalt());
        creator.copy(template);
        creator.forcedSpawnPosition(
                Position.block(0, 100, 0),
                0,
                0);
        return creator.createWorld();
    }

/**
     * Creates information about a world ready to send.
     *
     * @param world A world, that the information should be about.
     * @return A TextComponent ready to send with all information about the given world.
     */
    public static TextComponent info(World world) {
        TextComponent component = Component.text("Name: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(world.getName(), NamedTextColor.WHITE))
                .append(Component.text("\nPlayers: ", NamedTextColor.DARK_GREEN));

        List<Player> players = world.getPlayers();

        if (players.isEmpty()) {
            component = component.append(Component.text("None", NamedTextColor.WHITE));
        } else {
            for (Player p : players) {
                component = component.append(Component.text(p.getName(), NamedTextColor.GREEN))
                        .append(Component.text(", ", NamedTextColor.WHITE));
            }
            component = component.append(Component.text(players.size(), NamedTextColor.GREEN))
                    .append(Component.text(" total", NamedTextColor.WHITE));
        }

        return component.append(Component.text("\nSeed: ", NamedTextColor.DARK_GREEN))
                .append(Component.text(world.getSeed(), NamedTextColor.WHITE))
                .append(Component.text("\nBorder radius: ", NamedTextColor.DARK_GREEN))
                .append(Component.text((int) world.getWorldBorder().getSize(), NamedTextColor.WHITE));
    }

/**
     * Attempts to unload the world without saving and queues its directory for deletion.
     *
     * @param world the world to unload and queue for deletion
     */
    public static void delWorld(World world) {
        Bukkit.unloadWorld(world, false);
        FileUtils.safeDel(world.getWorldFolder(), FileUtils.DelSpecifier.USE_TASK_QUEUE);
    }

/**
     * Deletes a part of the world by its properties.
     *
     * @param world The world it should be deleting.
     * @param in Whether it should delete inside or outside the radius.
     * @param radius Radius it should delete to/from.
     * @param checkForBuilds Whether it should be checked for players-made builds.
     * @param center The center of the target.
     */
    public static void del(World world, boolean in, int radius, boolean checkForBuilds, Location center) {
        Chunk[] chunks = getAllChunks(world, radius, in, center);
        Map<Chunk, Boolean> chunksQueue = new HashMap<>();

        if (checkForBuilds) {
            World tempWorld = genWorld(world);

            for (Chunk chunk : chunks) {
                chunksQueue.put(chunk, compareChunks(chunk, tempWorld.getChunkAt(chunk.getX(), chunk.getZ())));
            }
        } else {
            for (Chunk chunk : chunks) {
                if (WorldUtils.isRegionSafeToDelete(radius, chunk.getX(), chunk.getZ(), in)) {
                    FileUtils.safeDel(FileManagement.getRegionFile(chunk.getX(), chunk.getZ(), world), FileUtils.DelSpecifier.USE_TASK_QUEUE);
                }
            }
        }

        FileManagement.deleteRegions(chunksQueue);
    }

/**
     * Deletes the entire world.
     *
     * @param world The target world.
     * @param checkForBuilds Whether it should check for builds.
     */
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

        FileManagement.deleteRegions(chunksQueue);
    }

/**
     * Deletes given sector.
     *
     * @param x X coordinate of the sector.
     * @param z Z coordinate of the sector.
     * @param isChunk Whether the sector is a chunk or a region.
     * @param checkForBuilds Whether it should be checked for players-made builds.
     * @param world The world the sector is located in.
     */
    public static void delSector(int x, int z, boolean isChunk, boolean checkForBuilds, World world) {
        if (checkForBuilds) {
            World newWorld = genWorld(world);

            if (isChunk) {
                Chunk chunk = world.getChunkAt(x, z);
                Chunk freshChunk = newWorld.getChunkAt(x, z);
                if (compareChunks(chunk, freshChunk)) {
                    FileManagement.makeFiles(x, z, world, true);
                }
            } else {
                List<Chunk> chunks = getGeneratedChunksInRegion(new Region(x, z, world));
                Map<Chunk, Boolean> safeToDelete = new HashMap<>();

                for (Chunk chunk : chunks) {
                    safeToDelete.put(
                            chunk,
                            compareChunks(chunk, newWorld.getChunkAt(chunk.getX(), chunk.getZ()))
                    );
                }
                FileManagement.makeFiles(safeToDelete);
            }
        } else {
            FileManagement.makeFiles(x, z, world, isChunk);
        }
    }

/**
     * Compares block data at every position across the first chunk's world height range.
     *
     * @param a the reference chunk
     * @param b the chunk to compare, which must support the same height range
     * @return {@code true} if block data matches at every compared position
     */
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

/**
     * Gets all chunks from a region.
     *
     * @param region Region it gets all chunks from
     * @return A {@code List<Chunk>} of all chunks from the given region.
     */
    public static List<Chunk> getGeneratedChunksInRegion(Region region) {
        File file = FileManagement.getRegionFile(region);
        int[][] chunks = Parser.parse(file);

        return coordsToChunk(chunks, region.getWorld());
    }

/**
     * Gets all chunk from given radius.
     *
     * @param world The world the chunks are located in.
     * @param radius The search radius.
     * @param in Whether it should be searched inside or outside the radius.
     * @param center The center of the radius.
     * @return An array of all chunks from the given radius.
     */
    private static Chunk[] getAllChunks(World world, int radius, boolean in, Location center) {
        List<Chunk> allChunks = new ArrayList<>();
        File[] files = getRegionFiles(world, radius, in, center);

        for (File file : files) {
            allChunks.addAll(new Region(file, world).getChunks());
        }

        return allChunks.toArray(Chunk[]::new);
    }

/**
     * Gets all chunks from given world.
     *
     * @param world The world the chunks are located in.
     * @return An array of all chunks in the given world.
     */
    private static Chunk[] getAllChunks(World world) {
        List<Chunk> chunks = new ArrayList<>();
        File[] files = getRegionFiles(world);

        for (File file : files) {
            Sector sector = new Sector(file);
            chunks.addAll(coordsToChunk(sector.getChunks(), world));
        }

        return chunks.toArray(Chunk[]::new);
    }

/**
     * Gets all region files in a world
     *
     * @param world The world it should take the region files from.
     * @return An array of all region files in the given world.
     */
    private static File[] getRegionFiles(World world) {
        File dir = new File(world.getWorldFolder(), "region");

        return dir.listFiles(file -> !file.isDirectory() && file.toPath().endsWith(".mca"));
    }

/**
     * Gets all region files from given radius.
     *
     * @param world The world it should take the region files from.
     * @param radius The search radius.
     * @param in Whether it should be searched inside or outside the radius.
     * @param center The center of the radius.
     * @return An array of all region files in the given radius.
     */
    private static File[] getRegionFiles(World world, int radius, boolean in, Location center) {
        File dir = new File(world.getWorldFolder(), "region");
        int regionRadius = WorldUtils.blockToRegion(radius);
        int regionX = WorldUtils.blockToRegion(center.getBlockX());
        int regionZ = WorldUtils.blockToRegion(center.getBlockZ());

        return dir.listFiles(file -> {
            if (file.isDirectory() || !file.getName().endsWith(".mca")) {
                return false;
            }

            String[] parts = file.getName().replace("r.", "").replace(".mca", "").split("\\.");

            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[1]);

            if (in) {
                return Math.abs(regionX - x) < regionRadius && Math.abs(regionZ - z) < regionRadius;
            } else {
                return Math.abs(regionX - x) > regionRadius && Math.abs(regionZ - z) > regionRadius;
            }
        });
    }

/**
     * Converts coordinates of chunks to chunks.
     *
     * @param coords Nested array of all chunks.
     * @param world World it should take the chunks from.
     * @return A {@code List<Chunk>} fetched from the nested array of coordinates.
     */
    private static List<Chunk> coordsToChunk(int[][] coords, World world) {
        List<Chunk> chunks = new ArrayList<>();

        for (int[] chunk : coords) {
            chunks.add(world.getChunkAt(chunk[0], chunk[1]));
        }

        return chunks;
    }

/**
     * Gets a block from given location asynchronously
     *
     * @param loc Location of the target block.
     * @return A {@code ComplateableFuture<String>} with a name of the block.
     */
    private static CompletableFuture<String> getBlockAsync(Location loc) {
        return loc.getWorld().getChunkAtAsync(loc)
                .thenApply(chunk -> loc.getBlock().getType().toString());
    }
}
