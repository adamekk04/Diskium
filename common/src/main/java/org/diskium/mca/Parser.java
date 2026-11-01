package org.diskium.mca;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import org.diskium.MultiplatformLogger;

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
     * Checks, if both files have different chunks. Deleted chunks doesn't count.
     * 
     * @param r1 First region chunk.
     * @param r2 Second region chunk.
     * @return Whether both files are different.
     */
    public static boolean areModified(File r1, File r2) {
        for (File f : new File[]{ r1, r2 }) {
            if (!isValid(f)) {
                MultiplatformLogger.error("Cannot read file " + f.getAbsolutePath());
                MultiplatformLogger.error("Size: " + f.length());
                return false;
            }
        }

        try (RandomAccessFile raf1 = new RandomAccessFile(r1, "rw")) {
            try (RandomAccessFile raf2 = new RandomAccessFile(r2, "rw")) {
                for (int i = 0; i < 1024; i++) {
                    raf1.seek(i * 4L);
                    raf2.seek(i * 4L);

                    int location1 = raf1.readInt();
                    int location2 = raf2.readInt();
                    int offset1 = (location1 >>> 8) & 0xFFFFFF; // offset of the chunk in sectors
                    int offset2 = (location2 >>> 8) & 0xFFFFFF;
                    int length1 = location1 & 0xFF; // length of the chunk in sectors
                    int length2 = location2 & 0xFF;

                    if (!(length1 == 0 || length1 == length2 || length2 == 0)) {
                        continue;
                    }

                    byte[] tail1 = new byte[length1 * 4096];
                    byte[] tail2 = new byte[length2 * 4096];

                    raf1.seek(offset1 * 4096L);
                    raf1.readFully(tail1);
                    raf2.seek(offset2 * 4096L);
                    raf2.readFully(tail2);

                    if (tail1 != tail2) {
                        return false;
                    }
                }

                return true;
            }
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while reading region files.", e);
        }

        return false;
    }

    /**
      * Checks the file size without validating the region contents.
      *
      * @param file the region file
      * @return {@code true} if the size is at least 8192 bytes and a multiple of 4096; {@code false}
      *     if the size is invalid or cannot be read
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
            MultiplatformLogger.error(
                    "Something went wrong while trying to validate " + file.getName(), e);
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
