package org.diskium.utils;

import org.diskium.objects.TaskObj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        FileUtils.setUseTasks(true, true, false);
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

        FileUtils.move(origin.toFile(), goal.toFile());

        assertFalse(Files.exists(origin));
        assertEquals("test content", Files.readString(goal));
    }

    @Test
    void moveReplacesExistingFile() throws IOException {
        Path origin = tempDir.resolve("origin.txt");
        Path goal = tempDir.resolve("goal.txt");

        Files.writeString(origin, "new content");
        Files.writeString(goal, "old content");

        FileUtils.move(origin.toFile(), goal.toFile());

        assertFalse(Files.exists(origin));
        assertEquals("new content", Files.readString(goal));
    }

    @Test
    void safeDelDeletesEnabledTypesAndQueuesDisabledType() throws IOException {
        Path log = Files.createFile(tempDir.resolve("delete-me.log"));
        Path plugin = Files.createFile(tempDir.resolve("delete-me.jar"));
        Path world = Files.createFile(tempDir.resolve("delete-me.mca"));

        assertTrue(FileUtils.safeDel(log.toFile(), FileUtils.DelSpecifier.LOGS));
        assertTrue(FileUtils.safeDel(plugin.toFile(), FileUtils.DelSpecifier.PLUGINS));
        assertTrue(FileUtils.safeDel(world.toFile(), FileUtils.DelSpecifier.WORLD));

        assertFalse(Files.exists(log));
        assertFalse(Files.exists(plugin));
        assertTrue(Files.exists(world));

        TaskObj[] tasks = TasksUtils.getTasks();

        assertEquals(1, tasks.length);
        assertTrue(tasks[0].getDelete());
        assertEquals(world.toFile(), tasks[0].getFile());
        assertEquals(TaskObj.Types.WORLD, tasks[0].getType());
    }

    @Test
    void getDeleteWhileRunningWorld() {
        assertFalse(FileUtils.getDeleteWhileRunningWorld());
    }
}
