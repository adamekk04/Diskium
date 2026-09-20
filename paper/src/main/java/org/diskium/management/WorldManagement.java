package org.diskium.management;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.diskium.Diskium;
import org.diskium.mca.Parser;
import org.diskium.mca.Sector;
import org.diskium.objects.Region;
import org.diskium.utils.FileUtils;
import org.diskium.utils.WorldUtils;

import java.io.File;
import java.util.*;

public class WorldManagement {

    public static TextComponent getBlock(Location loc, boolean existing) {
        TextComponent component;

        if (existing) {
            component = Component.text(loc.getWorld().getName(), NamedTextColor.DARK_GREEN)
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(Component.text(loc.getBlock().getType().toString(), NamedTextColor.GREEN));
        } else {
            World newWorld = genWorld(loc.getWorld());
            component = Component.text(loc.getWorld().getName(), NamedTextColor.DARK_GREEN)
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(Component.text(loc.getBlock().getType().toString(), NamedTextColor.GREEN));
            delWorld(newWorld);
        }
        return component;
    }

    public static World genWorld(World template) {
        String salt = WorldUtils.getSalt();

        Bukkit.getScheduler().runTaskAsynchronously(Diskium.getInstance(), () -> {
            WorldCreator creator = new WorldCreator(template.getName() + salt);
            creator.copy(template);
            creator.createWorld();
        });

        return Bukkit.getWorld(template.getName() + salt);
    }

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

                for (Chunk chunk : chunks) {
                    if (compareChunks(chunk, newWorld.getChunkAt(chunk.getX(), chunk.getZ()))) {
                        FileManagement.makeFiles(x, z, world, false);
                    }
                }
            }
        } else {
            FileManagement.makeFiles(x, z, world, isChunk);
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
        File file = FileManagement.getRegionFile(ch.getX(), ch.getZ(), ch.getWorld());
        int[][] chunks = Parser.parse(file);

        return coordsToChunk(chunks, ch.getWorld());
    }

    public static List<Chunk> getGeneratedChunksInRegion(Region region) {
        File file = FileManagement.getRegionFile(region.getX(), region.getZ(), region.getWorld());
        int[][] chunks = Parser.parse(file);

        return coordsToChunk(chunks, region.getWorld());
    }

    private static Chunk[] getAllChunks(World world, int radius, boolean in) {
        List<Chunk> allChunks = new ArrayList<>();
        File[] files = getRegionFiles(world, radius, in);

        for (File file : files) {
            allChunks.addAll(RegionManagement.getRegion(file, world).getChunks());
        }

        return allChunks.toArray(Chunk[]::new);
    }

    private static Chunk[] getAllChunks(World world) {
        List<Chunk> chunks = new ArrayList<>();
        File[] files = getRegionFiles(world);

        for (File file : files) {
            Sector sector = new Sector(file);
            chunks.addAll(coordsToChunk(sector.getChunks(), world));
        }

        return chunks.toArray(Chunk[]::new);
    }

    private static File[] getRegionFiles(World world) {
        File dir = new File(world.getWorldFolder(), "region");

        return dir.listFiles(file -> !file.isDirectory() && file.toPath().endsWith(".mca"));
    }

    private static File[] getRegionFiles(World world, int radius, boolean in) {
        File dir = new File(world.getWorldFolder(), "region");
        return dir.listFiles(file -> {
            if (file.isDirectory() || !file.getName().endsWith(".mca")) {
                return false;
            }

            String[] parts = file.getName().replace("r.", "").replace(".mca", "").split("\\.");

            int x = Integer.parseInt(parts[0]);
            int z = Integer.parseInt(parts[1]);

            if (in) {
                return Math.max(x, z) < WorldUtils.blockToRegion(radius);
            } else {
                return Math.max(x, z) > WorldUtils.blockToRegion(radius);
            }
        });
    }

    private static List<Chunk> coordsToChunk(int[][] coords, World world) {
        List<Chunk> chunks = new ArrayList<>();

        for (int[] chunk : coords) {
            chunks.add(world.getChunkAt(chunk[0], chunk[1]));
        }

        return chunks;
    }
}
