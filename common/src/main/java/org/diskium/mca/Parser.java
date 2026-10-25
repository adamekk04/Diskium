package org.diskium.mca;

import org.diskium.MultiplatformLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class Parser {

/**
     * Reads global coordinates of chunks listed in a region file's location table.
     *
     * @param file the region file
     * @return coordinate pairs {@code {x, z}}, or an empty array if the initial file check fails
     */
    public static int[][] parse(File file) {
        if (!isValid(file)) return new int[0][0];

        Sector sector = new Sector(file);

        return sector.getChunks();
    }

/**
     * Removes chunk from region file.
     *
     * @param x X coordinate of the chunk.
     * @param z Z coordinate of the chunk.
     * @param file Region file.
     * @return Status of the action.
     */
    public static boolean removeChunk(int x, int z, File file) {
        if (!isValid(file)) {
            MultiplatformLogger.error("Cannot read file " + file.getAbsolutePath());
            MultiplatformLogger.error("Size: " + file.length());
            return false;
        }

        int index = getIndex(x, z);

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(index * 4L);

            int location = raf.readInt();
            int offset = (location >>> 8) & 0xFFFFFF; // offset of the chunk in sectors
            int length = location & 0xFF; // length of the chunk in sectors

            if (offset == 0 || length == 0) {
                return false;
            }

            raf.seek(index * 4L);
            raf.writeInt(0); // remove from the first table
            raf.seek(index * 4L + 4096);
            raf.writeInt(0); // remove from the second table

            for (int i = 0; i < 1024; i++) {
                raf.seek(i * 4L);

                location = raf.readInt();
                int otherOffset = (location >>> 8) & 0xFFFFFF;
                int otherLength = location & 0xFF;

                if (otherOffset == 0 || otherLength == 0) {
                    continue;
                }

                if (otherOffset > offset) {
                    otherOffset -= length;

                    int newLocation = (otherOffset << 8) | otherLength;

                    raf.seek(i * 4L);
                    raf.writeInt(newLocation);
                }
            }

            long source = (offset + length) * 4096L; // chunks behind the deleted one
            long target = offset * 4096L; // target location to move chunks to
            long originalSize = raf.length(); // original size of the file

            byte[] tail = new byte[Math.toIntExact(originalSize - source)];
            raf.seek(source);
            raf.readFully(tail);

            raf.seek(target);
            raf.write(tail);
            raf.setLength(originalSize - length * 4096L);

            return true;
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while trying to read " + file.getName(), e);
            return false;
        }
    }

/**
     * Checks the file size without validating the region contents.
     *
     * @param file the region file
     * @return {@code true} if the size is at least 8192 bytes and a multiple of 4096;
     *         {@code false} if the size is invalid or cannot be read
     */
    private static boolean isValid(File file) {
        try {
            long size = Files.size(file.toPath());

            return size % 4096 == 0 && size >= 8192;
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't find file " + file.getName());
        } catch (NullPointerException e) {
            MultiplatformLogger.error("File to validate is null");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while trying to validate " + file.getName(), e);
        }
        return false;
    }

/**
     * Gets index of chunk in region file.
     *
     * @param chunkX X coordinate of the chunk.
     * @param chunkZ Z coordinate of the chunk.
     * @return Index of the chunk in region file.
     */
    private static int getIndex(int chunkX, int chunkZ) {
        int localX = Math.floorMod(chunkX, 32);
        int localZ = Math.floorMod(chunkZ, 32);

        return localX + localZ * 32;
    }
}
