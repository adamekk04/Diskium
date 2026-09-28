package org.diskium.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldUtilsTest {

    @Test
    void blockToChunkHandlesChunkBoundaries() {
        assertEquals(1, WorldUtils.blockToChunk(0));
        assertEquals(1, WorldUtils.blockToChunk(15));
        assertEquals(2, WorldUtils.blockToChunk(16));
        assertEquals(2, WorldUtils.blockToChunk(31));
        assertEquals(3, WorldUtils.blockToChunk(32));
    }

    @Test
    void chunkToRegionHandlesPositiveAndNegativeCoordinates() {
        assertArrayEquals(new int[]{0, 0}, WorldUtils.chunkToRegion(0, 31));
        assertArrayEquals(new int[]{1, 1}, WorldUtils.chunkToRegion(32, 63));
        assertArrayEquals(new int[]{-1, -1}, WorldUtils.chunkToRegion(-1, -32));
        assertArrayEquals(new int[]{-2, -2}, WorldUtils.chunkToRegion(-33, -64));
    }

    @Test
    void blockToRegionHandlesRegionBoundaries() {
        assertEquals(0, WorldUtils.blockToRegion(0));
        assertEquals(0, WorldUtils.blockToRegion(511));
        assertEquals(1, WorldUtils.blockToRegion(512));
        assertEquals(1, WorldUtils.blockToRegion(1023));
        assertEquals(2, WorldUtils.blockToRegion(1024));
    }

    @Test
    void getSaltUsesExpectedLengthAndCharacters() {
        String salt = WorldUtils.getSalt();

        assertEquals(10, salt.length());
        assertTrue(salt.matches("[a-z0-9]{10}"));
    }

    @Test
    void regionInsideRadiusIsSafeToDeleteWhenDeletingInside() {
        assertTrue(WorldUtils.isRegionSafeToDelete(1024, 0, 0, true));
        assertTrue(WorldUtils.isRegionSafeToDelete(1024, 63, 63, true));
        assertFalse(WorldUtils.isRegionSafeToDelete(1024, 64, 64, true));
    }

    @Test
    void regionOutsideRadiusIsSafeToDeleteWhenDeletingOutside() {
        assertFalse(WorldUtils.isRegionSafeToDelete(1024, 0, 0, false));
        assertFalse(WorldUtils.isRegionSafeToDelete(1024, 63, 63, false));
        assertTrue(WorldUtils.isRegionSafeToDelete(1024, 64, 64, false));
    }

    @Test
    void regionSafetyIsSymmetricForNegativeCoordinates() {
        assertTrue(WorldUtils.isRegionSafeToDelete(1024, -63, -63, true));
        assertFalse(WorldUtils.isRegionSafeToDelete(1024, -64, -64, true));
        assertTrue(WorldUtils.isRegionSafeToDelete(1024, -64, -64, false));
    }
}
