package org.diskium;

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
        return new int[] {x / 32 + 1, z / 32 + 1};
    }

    public static int blockToRegion(int radius) {
        return (radius / 1024) + 1;
    }

    public static String getSalt() {
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        String toReturn = "";
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            toReturn = toReturn + chars.charAt(random.nextInt(chars.length())); // TODO: Use StringBuilder
        }
        return toReturn;
    }

    public static boolean isRegionSafeToDelete(int radius, int x, int z, boolean in) { // TODO: Simplify all ifs and returns
        if (in) {
            // TODO: Fix edge-case, when radius is same as region border
            if (Arrays.stream(chunkToRegion(x, z)).max().getAsInt() >= blockToRegion(radius)) {
                return false;
            }
            return true;
        } else {
            if (Arrays.stream(chunkToRegion(x, z)).max().getAsInt() <= blockToRegion(radius)) {
                return false;
            }
            return true;
        }
    }
}
