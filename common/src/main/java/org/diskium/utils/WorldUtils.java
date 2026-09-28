package org.diskium.utils;

import java.util.Random;

public class WorldUtils {

    public static int blockToChunk(int radius) {
        return ( radius / 16) + 1;
    }

    public static int[] chunkToRegion(int x, int z) {
        return new int[]{
                Math.floorDiv(x, 32),
                Math.floorDiv(z, 32)
        };
    }

    public static int blockToRegion(int radius) {
        return radius / 512;
    }

    public static String getSalt() {
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

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
