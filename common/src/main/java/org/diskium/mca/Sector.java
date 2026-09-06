package org.diskium.mca;

import org.diskium.MultiplatformLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sector {
    byte[] data = new byte[4096];
    int x;
    int z;

    public Sector(File file) {
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] data = new byte[4096];

            int bytesRead = input.read(data);

            if (bytesRead != 4096) MultiplatformLogger.error(file.getName() + " is too small, cannot get world data.");

            this.data = data;

            String fileName = file.getName();
            fileName = fileName.replace("r.", "");
            fileName = fileName.replace(".mca", "");

            String[] regionCoords = fileName.split("\\.");

            this.x = Integer.parseInt(regionCoords[0]);
            this.z = Integer.parseInt(regionCoords[1]);
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while trying to read " + file.getName(), e);
        }
    }

    public int[][] getChunks() {
        List<int[]> buffer = new ArrayList<>();

        for (int i = 0; i < 1024; i++) {
            byte b0 = this.data[i];
            byte b1 = this.data[i + 1];
            byte b2 = this.data[i + 2];
            byte b3 = this.data[i + 3];

            if (!(b0 == 0 && b1 == 0 && b2 == 0 && b3 == 0)) {
                buffer.add(new int[] {this.x * 32 + i % 32, this.z * 32 + i / 32});
            }
        }

        return buffer.toArray(int[][]::new);
    }
}
