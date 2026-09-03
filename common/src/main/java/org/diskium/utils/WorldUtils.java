package org.diskium.utils;

import java.util.Arrays;
import java.util.Random;

public class WorldUtils {
    public static int blockToChunk(int radius) {
        return (radius / 16) + 1;
    }

    public static int blockToChunk(double radius) {
        return ((int) radius / 16) + 1;
    }

    public static int[] chunkToRegion(int x, int z) {
        return new int[]{x / 32 + 1, z / 32 + 1};
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
        int max = Arrays.stream(chunkToRegion(x, z)).max().getAsInt();

        if (in) {
            return max < blockToRegion(radius);
        } else {
            return max >= blockToRegion(radius);
        }
    }
}
