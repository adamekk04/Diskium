package org.diskium.mca;

import org.diskium.MultiplatformLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class Parser {
    public static int[][] parse(File file) {
        if (!isValid(file)) return null;

        Sector sector = new Sector(file);

        return sector.getChunks();
    }

    public static void removeChunk(int x, int z, File file) {
        int[] coords = getCoordsByFile(file);
        int index = getIndex(coords[0], coords[1], x, z);

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(index * 4L);

            int location = raf.readInt();
            int offset = (location >>> 8) & 0xFFFFFF;
            int length = location & 0xFF;

            raf.writeInt(0);
            raf.seek(index * 4L + 4096);
            raf.writeInt(0);

            for (int i = 0; i < 1024; i++) {
                raf.seek(i * 4L);

                location = raf.readInt();
                int otherOffset = (location >>> 8) & 0xFFFFFF;
                int otherLength = location & 0xFF;

                if (otherOffset == 0 || otherLength == 0) continue;

                if (otherOffset > offset) {
                    otherOffset -= length;

                    int newLocation = (otherOffset << 8) | otherLength;

                    raf.seek(i * 4L);
                    raf.writeInt(newLocation);
                }
            }
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while trying to read " + file.getName(), e);
        }
    }

    private static boolean isValid(File file) {
        try {
            long size = Files.size(file.toPath());

            return size % 4096 == 0;
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't find file " + file.getName());
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while trying to validate " + file.getName(), e);
        }
        return false;
    }

    private static int[] getCoordsByFile(File file) {
        String name = file.getName().replace("r.", "").replace(".mca", "");

        String[] parts = name.split("\\.");

        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private static int getIndex(int chunkX, int chunkZ, int regionX, int regionZ) {
        int localX = chunkX - regionX * 32;
        int localZ = chunkZ - regionZ * 32;

        return localX + localZ * 32;
    }
}
