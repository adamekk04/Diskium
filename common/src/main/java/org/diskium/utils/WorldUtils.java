package org.diskium.utils;

import java.util.Random;

public class WorldUtils {

/**
     * Calculates a chunk radius as {@code radius / 16 + 1} using integer division.
     *
     * @param radius the radius in blocks
     * @return the quotient plus one, including for exact multiples of 16
     */
    public static int blockToChunk(int radius) {
        return ( radius / 16) + 1;
    }

/**
     * Finds the region containing the given chunk, rounding down for negative coordinates.
     *
     * @param x the global chunk X coordinate
     * @param z the global chunk Z coordinate
     * @return the region coordinates as {@code {x, z}}
     */
    public static int[] chunkToRegion(int x, int z) {
        return new int[]{
                Math.floorDiv(x, 32),
                Math.floorDiv(z, 32)
        };
    }

/**
     * Converts block to region.
     *
     * @param radius Radius of blocks.
     * @return Radius of regions.
     */
    public static int blockToRegion(int radius) {
        return radius / 512;
    }

/**
     * Generates random salt with all lowercase letters and numbers. It's 10 characters long.
     *
     * @return The generated salt.
     */
    public static String getSalt() {
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

/**
     * Tests the region-distance bucket derived from chunk coordinates against a block radius.
     * Uses the larger absolute coordinate relative to the world origin;
     * equality with the converted radius counts as outside.
     *
     * @param radius the radius in blocks, converted using integer division by 512
     * @param x the global chunk X coordinate
     * @param z the global chunk Z coordinate
     * @param in {@code true} to select inside the radius; {@code false} to select outside
     * @return whether the calculated bucket matches the requested side of the boundary
     */
    public static boolean isRegionSafeToDelete(int radius, int x, int z, boolean in) {
        long furthestChunk = Math.max(
                Math.abs((long) x),
                Math.abs((long) z)
        );

        long regionDistance = furthestChunk / 32;
        long radiusInRegions = blockToRegion(radius);
        boolean isInside = regionDistance < radiusInRegions;

        return in == isInside;
    }
}
