package org.diskium.mca;

import org.diskium.MultiplatformLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class Parser {
    public static int[][] parse(File file) {
        if (!isValid(file)) return null;

        Sector sector = new Sector(file);

        return sector.getChunks();
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
}
