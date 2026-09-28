package org.diskium.mca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    public static final int[][] CHUNK_COORDINATES = {
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
    void parse() {
        File missingFile = tempDir.resolve("r.1.2.mca").toFile();
        File regionFile = getTestRegionFile();

        assertEquals(0, Parser.parse(missingFile).length);
        assertArrayEquals(CHUNK_COORDINATES, Parser.parse(regionFile));
    }

    @Test
    void removeChunk() {
        File missingFile = tempDir.resolve("r.1.2.mca").toFile();
        File regionFile = getTestRegionFile();

        assertFalse(Parser.removeChunk(0, -24, missingFile));
        assertFalse(Parser.removeChunk(0, 0, regionFile));
        assertTrue(Parser.removeChunk(0, -24, regionFile));
    }

    private File getTestRegionFile() {
        Path destination = tempDir.resolve("r.0.-1.mca");

        try (InputStream source = Objects.requireNonNull(getClass().getResourceAsStream("/r.0.-1.mca"), "Missing test resource /r.0.-1.mca")) {
            Files.copy(source, destination);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return destination.toFile();
    }
}
