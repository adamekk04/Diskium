package org.diskium.objects;

import org.diskium.utils.TasksUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BackupObjTest {

    @TempDir
    Path tempDir;

    private File serverRoot;

    @BeforeEach
    void setUp() {
        serverRoot = tempDir.toFile();
        File pluginDirectory = tempDir.resolve("diskium").toFile();

        TasksUtils.setFiles(serverRoot, pluginDirectory);
    }

    @Test
    void constructorStoresFiles() {
        File original = tempDir.resolve("world/region/r.0.0.mca").toFile();
        File backup = tempDir.resolve("backups/r.0.0.mca").toFile();

        BackupObj backupObj = new BackupObj(original, backup);

        assertSame(original, backupObj.getFile());
        assertSame(backup, backupObj.getItself());
    }

    @Test
    void getTypeReturnsWorldForRegionFile() {
        File original = tempDir.resolve("world/region/r.0.0.mca").toFile();
        File backup = tempDir.resolve("backups/r.0.0.mca").toFile();

        BackupObj backupObj = new BackupObj(original, backup);

        assertEquals(TaskObj.Types.WORLD, backupObj.getType());
    }

    @Test
    void getTypeReturnsPluginsForPluginFile() {
        File original = tempDir.resolve("plugins/example.jar").toFile();
        File backup = tempDir.resolve("backups/example.jar").toFile();

        BackupObj backupObj = new BackupObj(original, backup);

        assertEquals(TaskObj.Types.PLUGINS, backupObj.getType());
    }

    @Test
    void getTypeReturnsLogsForLogFile() {
        File original = tempDir.resolve("logs/latest.log").toFile();
        File backup = tempDir.resolve("backups/latest.log").toFile();

        BackupObj backupObj = new BackupObj(original, backup);

        assertEquals(TaskObj.Types.LOGS, backupObj.getType());
    }

    @Test
    void getTypeReturnsOtherForUnknownFile() {
        File original = tempDir.resolve("server.properties").toFile();
        File backup = tempDir.resolve("backups/server.properties").toFile();

        BackupObj backupObj = new BackupObj(original, backup);

        assertEquals(TaskObj.Types.OTHER, backupObj.getType());
    }
}
