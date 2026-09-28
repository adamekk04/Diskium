package org.diskium.mca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SectorTest {

    private static final int[][] CHUNK_COORDINATES = {
            {0, -24},
            {1, -24},
            {2, -24},
            {3, -24},
            {4, -24},
            {5, -24},
            {6, -24},
            {7, -24},
            {8, -24},
            {9, -24},
    };

    @TempDir
    Path tempDir;

    @Test
    void constructorReadsHeaderAndRegionCoordinates() throws IOException {
        File region = copyTestRegion("r.0.-1.mca");
        byte[] expectedHeader;

        try (InputStream input = Files.newInputStream(region.toPath())) {
            expectedHeader = input.readNBytes(4096);
        }

        Sector sector = new Sector(region);

        assertEquals(0, sector.x);
        assertEquals(-1, sector.z);
        assertEquals(4096, sector.data.length);
        assertArrayEquals(expectedHeader, sector.data);
    }

    @Test
    void constructorHandlesInvalidFileName() {
        File region = copyTestRegion("invalid.mca");

        Sector sector = new Sector(region);

        assertEquals(0, sector.x);
        assertEquals(0, sector.z);
    }

    @Test
    void getChunksReturnsCoordinatesFromRegionHeader() {
        Sector sector = new Sector(copyTestRegion("r.0.-1.mca"));

        assertArrayEquals(CHUNK_COORDINATES, sector.getChunks());
    }

    private File copyTestRegion(String fileName) {
        Path destination = tempDir.resolve(fileName);

        try (InputStream source = Objects.requireNonNull(
                getClass().getResourceAsStream("/r.0.-1.mca"),
                "Missing test resource /r.0.-1.mca"
        )) {
            Files.copy(source, destination);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return destination.toFile();
    }
}
