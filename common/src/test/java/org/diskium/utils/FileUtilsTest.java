package org.diskium.utils;

import org.diskium.objects.TaskObj;
import org.diskium.objects.BackupObj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setDelWhileRunning() {
        File serverDir = tempDir.resolve("server").toFile();
        File pluginDir = tempDir.resolve("plugin").toFile();

        TasksUtils.setFiles(serverDir, pluginDir);
        TasksUtils.mkFiles();
        FileUtils.setUseTasks(true, true, false, pluginDir);
    }

    @Test
    void forceDelDeletesFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("delete-me.txt"));

        assertTrue(FileUtils.forceDel(file.toFile()));
        assertFalse(Files.exists(file));
        assertFalse(FileUtils.forceDel(file.toFile()));
    }

    @Test
    void forceDelDeletesDirectoryWithContent() throws IOException {
        Path directory = tempDir.resolve("directory");
        Path nestedDirectory = directory.resolve("nested");
        Path nestedFile = nestedDirectory.resolve("file.txt");

        Files.createDirectories(nestedDirectory);
        Files.writeString(nestedFile, "test content");

        assertTrue(FileUtils.forceDel(directory.toFile()));
        assertFalse(Files.exists(directory));
    }

    @Test
    void moveMovesFileAndPreservesContent() throws IOException {
        Path origin = tempDir.resolve("folder/origin.txt");
        Path goal = tempDir.resolve("goal.txt");

        Files.createDirectories(origin.getParent());
        Files.writeString(origin, "test content");

        assertTrue(FileUtils.move(origin.toFile(), goal.toFile()));

        assertFalse(Files.exists(origin));
        assertEquals("test content", Files.readString(goal));
    }

    @Test
    void moveReplacesExistingFile() throws IOException {
        Path origin = tempDir.resolve("origin.txt");
        Path goal = tempDir.resolve("goal.txt");

        Files.writeString(origin, "new content");
        Files.writeString(goal, "old content");

        assertTrue(FileUtils.move(origin.toFile(), goal.toFile()));

        assertFalse(Files.exists(origin));
        assertEquals("new content", Files.readString(goal));
    }

    @Test
    void safeDelDeletesEnabledTypesAndQueuesExplicitTaskQueueType() throws IOException {
        Path log = Files.createFile(tempDir.resolve("delete-me.log"));
        Path plugin = Files.createFile(tempDir.resolve("delete-me.jar"));
        Path world = Files.createFile(tempDir.resolve("delete-me.mca"));

        assertTrue(FileUtils.safeDel(log.toFile(), FileUtils.DelSpecifier.LOGS));
        assertTrue(FileUtils.safeDel(plugin.toFile(), FileUtils.DelSpecifier.PLUGINS));
        assertTrue(FileUtils.safeDel(world.toFile(), FileUtils.DelSpecifier.USE_TASK_QUEUE));

        assertFalse(Files.exists(log));
        assertFalse(Files.exists(plugin));
        assertTrue(Files.exists(world));

        TaskObj[] tasks = TasksUtils.getTasks();

        assertEquals(1, tasks.length);
        assertTrue(tasks[0].getDelete());
        assertEquals(world.toFile(), tasks[0].getFile());
        assertEquals(TaskObj.Types.WORLD, tasks[0].getType());
        assertEquals(0, TasksUtils.getBackups().length);
    }

    @ParameterizedTest
    @CsvSource({
            "true, false, false, LOGS, true",
            "true, false, true, LOGS, true",
            "false, true, false, LOGS, false",
            "false, true, true, LOGS, false",
            "false, true, false, PLUGINS, true",
            "false, true, true, PLUGINS, true",
            "true, false, false, PLUGINS, false",
            "true, false, true, PLUGINS, false",
            "true, true, false, USE_TASK_QUEUE, false",
            "true, true, true, USE_TASK_QUEUE, false"
    })
    void safeDelAppliesPolicyAndRegistersBackups(boolean logs, boolean plugins, boolean backups,
                                                FileUtils.DelSpecifier type, boolean immediate) throws IOException {
        File pluginDir = tempDir.resolve("plugin").toFile();
        FileUtils.setUseTasks(logs, plugins, backups, pluginDir);
        Path file = tempDir.resolve("server/world/region/r.0.0.mca");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "region data");

        assertTrue(FileUtils.safeDel(file.toFile(), type));

        assertEquals(!immediate, Files.exists(file));
        TaskObj[] tasks = TasksUtils.getTasks();
        assertEquals(immediate ? 0 : 1, tasks.length);
        if (!immediate) {
            assertEquals("region data", Files.readString(file));
            assertTrue(tasks[0].getDelete());
            assertEquals(file.toFile(), tasks[0].getFile());
            assertEquals(TaskObj.Types.WORLD, tasks[0].getType());
            TasksUtils.complete(tasks);
            assertFalse(Files.exists(file));
            assertEquals(0, TasksUtils.getTasks().length);
        }

        BackupObj[] registeredBackups = TasksUtils.getBackups();
        assertEquals(backups ? 1 : 0, registeredBackups.length);
        if (backups) {
            assertEquals(file.toFile(), registeredBackups[0].getFile());
            Path backup = registeredBackups[0].getItself().toPath();
            assertEquals(file.getFileName(), backup.getFileName());
            assertEquals(pluginDir.toPath().resolve("backups"), backup.getParent().getParent());
            java.util.UUID.fromString(backup.getParent().getFileName().toString());
            assertEquals("region data", Files.readString(backup));
            assertEquals(TaskObj.Types.WORLD, registeredBackups[0].getType());
            TasksUtils.complete(registeredBackups);
            assertEquals("region data", Files.readString(file));
            assertFalse(Files.exists(backup));
            assertEquals(0, TasksUtils.getBackups().length);
        }
    }

    @Test
    void safeDelKeepsSameNamedBackupsSeparate() throws IOException {
        FileUtils.setUseTasks(true, true, true, tempDir.resolve("plugin").toFile());
        for (String world : new String[]{"world", "nether"}) {
            Path file = tempDir.resolve(world).resolve("r.0.0.mca");
            Files.createDirectories(file.getParent());
            Files.writeString(file, world);
            assertTrue(FileUtils.safeDel(file.toFile(), FileUtils.DelSpecifier.LOGS));
        }

        BackupObj[] backups = TasksUtils.getBackups();
        assertEquals(2, backups.length);
        assertNotEquals(backups[0].getItself(), backups[1].getItself());
        assertEquals("world", Files.readString(backups[0].getItself().toPath()));
        assertEquals("nether", Files.readString(backups[1].getItself().toPath()));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(FileUtils.DelSpecifier.class)
    void safeDelDoesNotDeleteOrQueueWhenBackupDirectoryCannotBeCreated(FileUtils.DelSpecifier type)
            throws IOException {
        FileUtils.setUseTasks(true, true, true, tempDir.resolve("plugin").toFile());
        Files.writeString(tempDir.resolve("plugin/backups"), "blocking file");
        Path file = tempDir.resolve("keep.txt");
        Files.writeString(file, "original data");

        assertFalse(FileUtils.safeDel(file.toFile(), type));
        assertEquals("original data", Files.readString(file));
        assertEquals(0, TasksUtils.getTasks().length);
        assertEquals(0, TasksUtils.getBackups().length);
    }

}
