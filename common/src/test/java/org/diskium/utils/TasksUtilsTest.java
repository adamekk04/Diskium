package org.diskium.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TasksUtilsTest {

    private File pluginDir;
    private File serverDir;

    @TempDir Path tempDir;

    @BeforeEach
    void setFiles() {
        serverDir = tempDir.resolve("server").toFile();
        pluginDir = tempDir.resolve("plugin").toFile();

        TasksUtils.setFiles(serverDir, pluginDir);
    }

    @Test
    void mkFiles() {
        assertTrue(TasksUtils.mkFiles());
        assertTrue(pluginDir.isDirectory());
        assertTrue(pluginDir.toPath().resolve("tasks.txt").toFile().isFile());
        assertTrue(pluginDir.toPath().resolve("backups.txt").toFile().isFile());
    }

    @Test
    void mkFilesWithExistingFiles() {
        assertTrue(TasksUtils.mkFiles());
        assertFalse(TasksUtils.mkFiles());
    }

    @Test
    void mkFilesWithContent() throws IOException {
        assertTrue(TasksUtils.mkFiles());

        Path tasksFile = pluginDir.toPath().resolve("tasks.txt");
        String content = "Test Content";
        Files.writeString(tasksFile, content);

        assertFalse(TasksUtils.mkFiles());

        String newContent = Files.readString(tasksFile);

        assertEquals(content, newContent);
    }

    @Test
    void getTasks() throws IOException {
        TasksUtils.mkFiles();

        Path original1 = tempDir.resolve("server/plugins/old.jar");
        Path original2 = tempDir.resolve("server/world/region/r.0.0.mca");
        Path replacement = tempDir.resolve("backup/r.0.0.mca");
        Files.createDirectories(original1.getParent());
        Files.createDirectories(original2.getParent());
        Files.createDirectories(replacement.getParent());
        Files.writeString(original1, "delete me");
        Files.writeString(original2, "source");
        Files.writeString(replacement, "replacement");

        Files.write(
                pluginDir.toPath().resolve("tasks.txt"),
                List.of(
                        original1.toString(), FileUtils.getSha(original1.toFile()), "1", "1",
                        original2.toString(), FileUtils.getSha(original2.toFile()), replacement.toString(),
                        FileUtils.getSha(replacement.toFile())));

        TaskObj[] tasks = TasksUtils.getTasks();

        assertEquals(2, tasks.length);

        assertAll(
                () -> assertTrue(tasks[0].getDelete()),
                () -> assertEquals(original1.toFile(), tasks[0].getFile()),
                () -> assertNull(tasks[0].getReplacementFile()),
                () -> assertEquals(TaskObj.Types.PLUGINS, tasks[0].getType()));

        assertAll(
                () -> assertFalse(tasks[1].getDelete()),
                () -> assertEquals(original2.toFile(), tasks[1].getFile()),
                () -> assertEquals(replacement.toFile(), tasks[1].getReplacementFile()),
                () -> assertEquals(TaskObj.Types.WORLD, tasks[1].getType()));
    }

    @Test
    void getTasksIgnoresIncompleteRecord() throws IOException {
        TasksUtils.mkFiles();

        Files.write(pluginDir.toPath().resolve("tasks.txt"), List.of("/some/file/without/mode"));

        assertEquals(0, TasksUtils.getTasks().length);
    }

    @Test
    void getBackupsReadsStoredBackups() throws IOException {
        TasksUtils.mkFiles();

        Path original = tempDir.resolve("server/world/region/r.0.0.mca");
        Path backup = tempDir.resolve("backup/r.0.0.mca");
        Files.createDirectories(original.getParent());
        Files.createDirectories(backup.getParent());
        Files.writeString(original, "backup content");
        Files.writeString(backup, "backup content");

        Files.write(
                pluginDir.toPath().resolve("backups.txt"),
                List.of(original.toString(), FileUtils.getSha(backup.toFile()), backup.toString()));

        BackupObj[] backups = TasksUtils.getBackups();

        assertEquals(1, backups.length);
        assertEquals(original.toFile(), backups[0].getFile());
        assertEquals(backup.toFile(), backups[0].getItself());
        assertEquals(TaskObj.Types.WORLD, backups[0].getType());
    }

    @Test
    void addStoresDeleteTask() throws IOException {
        TasksUtils.mkFiles();

        File file = tempDir.resolve("server/plugins/example.jar").toFile();
        Files.createDirectories(file.toPath().getParent());
        Files.writeString(file.toPath(), "delete me");
        TaskObj task = new TaskObj(true, file, null, TaskObj.Types.PLUGINS);

        assertTrue(TasksUtils.add(task));

        assertEquals(
                List.of(file.toString(), task.getFileHash(), "1", "1"),
                Files.readAllLines(pluginDir.toPath().resolve("tasks.txt")));
    }

    @Test
    void addStoresReplacementTask() throws IOException {
        TasksUtils.mkFiles();

        File original = tempDir.resolve("server/world/region/r.0.0.mca").toFile();
        File replacement = tempDir.resolve("backup/r.0.0.mca").toFile();
        Files.createDirectories(original.toPath().getParent());
        Files.createDirectories(replacement.toPath().getParent());
        Files.writeString(original.toPath(), "source");
        Files.writeString(replacement.toPath(), "replacement");

        TaskObj task = new TaskObj(false, original, replacement, TaskObj.Types.WORLD);

        assertTrue(TasksUtils.add(task));

        assertEquals(
                List.of(
                        original.toString(), task.getFileHash(), replacement.toString(),
                        task.getReplacementFileHash()),
                Files.readAllLines(pluginDir.toPath().resolve("tasks.txt")));
    }

    @Test
    void addStoresBackup() throws IOException {
        TasksUtils.mkFiles();

        File original = tempDir.resolve("server/world/region/r.0.0.mca").toFile();
        File backup = tempDir.resolve("backup/r.0.0.mca").toFile();

        Files.createDirectories(original.toPath().getParent());
        Files.createDirectories(backup.toPath().getParent());
        Files.writeString(original.toPath(), "region data");

        assertTrue(TasksUtils.add(new BackupObj(original, backup)));
        assertEquals("region data", Files.readString(backup.toPath()));
        assertEquals("region data", Files.readString(original.toPath()));

        assertEquals(
                List.of(original.toString(), FileUtils.getSha(original), backup.toString()),
                Files.readAllLines(pluginDir.toPath().resolve("backups.txt")));
    }

    @Test
    void completeMovesReplacementTask() throws IOException {
        Path original = tempDir.resolve("server/world/region/r.0.0.mca");
        Path replacement = tempDir.resolve("backup/r.0.0.mca");
        Files.createDirectories(original.getParent());
        Files.createDirectories(replacement.getParent());
        Files.writeString(original, "region data");

        TaskObj task = new TaskObj(false, original.toFile(), replacement.toFile(), TaskObj.Types.WORLD);

        TasksUtils.complete(new TaskObj[] {task});

        assertFalse(Files.exists(original));
        assertEquals("region data", Files.readString(replacement));
    }

    @Test
    void completeDeletesTaskFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("delete-me.txt"));
        TaskObj task = new TaskObj(true, file.toFile(), null, TaskObj.Types.OTHER);

        TasksUtils.complete(new TaskObj[] {task});

        assertFalse(Files.exists(file));
    }

    @Test
    void completeRestoresBackup() throws IOException {
        TasksUtils.mkFiles();

        Path original = tempDir.resolve("server/world/region/r.0.0.mca");
        Path backup = tempDir.resolve("backup/r.0.0.mca");
        Files.createDirectories(original.getParent());
        Files.createDirectories(backup.getParent());
        Files.writeString(original, "backup data");
        Files.writeString(backup, "backup data");
        String backupHash = FileUtils.getSha(backup.toFile());
        BackupObj backupObj = new BackupObj(original.toFile(), backup.toFile(), backupHash);
        Files.writeString(original, "old data");
        Files.write(
                pluginDir.toPath().resolve("backups.txt"),
                List.of(original.toString(), backupHash, backup.toString()));

        TasksUtils.restore(backupObj);
        TaskObj[] tasks = TasksUtils.getTasks();
        assertEquals(1, tasks.length);
        TasksUtils.complete(tasks);

        assertFalse(Files.exists(backup));
        assertEquals("backup data", Files.readString(original));
        assertEquals(3, Files.readAllLines(pluginDir.toPath().resolve("backups.txt")).size());
    }

    @Test
    void completeRejectsBackupModifiedAfterRestoreWasQueued() throws IOException {
        TasksUtils.mkFiles();

        Path original = tempDir.resolve("server/world/region/r.0.0.mca");
        Path backup = tempDir.resolve("backup/r.0.0.mca");
        Files.createDirectories(original.getParent());
        Files.createDirectories(backup.getParent());
        Files.writeString(original, "valid backup");
        Files.writeString(backup, "valid backup");
        String backupHash = FileUtils.getSha(backup.toFile());
        BackupObj backupObj = new BackupObj(original.toFile(), backup.toFile(), backupHash);
        Files.writeString(original, "good destination");
        Path registry = pluginDir.toPath().resolve("backups.txt");
        Files.write(registry, List.of(original.toString(), backupHash, backup.toString()));

        TasksUtils.restore(backupObj);
        Files.writeString(backup, "corrupt backup");
        TaskObj[] tasks = TasksUtils.getTasks();

        assertEquals(0, tasks.length);
        assertEquals("good destination", Files.readString(original));
        assertEquals("corrupt backup", Files.readString(backup));
        assertEquals(4, Files.readAllLines(pluginDir.toPath().resolve("tasks.txt")).size());
    }

    @Test
    void removeDeletesOnlySelectedTask() throws IOException {
        TasksUtils.mkFiles();

        File first = tempDir.resolve("first.jar").toFile();
        File removed = tempDir.resolve("removed.jar").toFile();
        File last = tempDir.resolve("last.jar").toFile();
        Files.writeString(first.toPath(), "first");
        Files.writeString(removed.toPath(), "removed");
        Files.writeString(last.toPath(), "last");
        TaskObj removeTask = new TaskObj(true, removed, null, TaskObj.Types.OTHER);
        Path tasksFile = pluginDir.toPath().resolve("tasks.txt");
        Files.write(
                tasksFile,
                List.of(
                        first.toString(), FileUtils.getSha(first), "1", "1",
                        removed.toString(), removeTask.getFileHash(), "1", "1",
                        last.toString(), FileUtils.getSha(last), "1", "1"));

        TasksUtils.remove(removeTask);

        assertEquals(
                List.of(first.toString(), FileUtils.getSha(first), "1", "1", last.toString(),
                        FileUtils.getSha(last), "1", "1"),
                Files.readAllLines(tasksFile));
    }

    @Test
    void removeDeletesOnlySelectedBackup() throws IOException {
        TasksUtils.mkFiles();

        File original = tempDir.resolve("original.mca").toFile();
        File oldBackup = tempDir.resolve("old.backup").toFile();
        File newBackup = tempDir.resolve("new.backup").toFile();
        Files.writeString(original.toPath(), "same original");
        Files.writeString(oldBackup.toPath(), "old version");
        Files.writeString(newBackup.toPath(), "new version");
        String oldHash = FileUtils.getSha(oldBackup);
        String newHash = FileUtils.getSha(newBackup);
        Path backupsFile = pluginDir.toPath().resolve("backups.txt");
        Files.write(
                backupsFile,
                List.of(
                        original.toString(), oldHash, oldBackup.toString(),
                        original.toString(), newHash, newBackup.toString()));

        TasksUtils.remove(new BackupObj(original, newBackup, newHash));

        assertEquals(
                List.of(original.toString(), oldHash, oldBackup.toString()),
                Files.readAllLines(backupsFile));
    }

    @Test
    void getTypeRecognizesAllSupportedTypes() {
        assertAll(
                () ->
                        assertEquals(
                                TaskObj.Types.PLUGINS,
                                TasksUtils.getType(tempDir.resolve("server/plugins/example.jar").toFile())),
                () ->
                        assertEquals(
                                TaskObj.Types.WORLD,
                                TasksUtils.getType(tempDir.resolve("server/world/region/r.0.0.mca").toFile())),
                () ->
                        assertEquals(
                                TaskObj.Types.LOGS,
                                TasksUtils.getType(tempDir.resolve("server/logs/latest.log").toFile())),
                () ->
                        assertEquals(
                                TaskObj.Types.OTHER,
                                TasksUtils.getType(tempDir.resolve("server/server.properties").toFile())));
    }

    @Test
    void setFilesChangesServerAndPluginDirectories() {
        File otherServerDir = tempDir.resolve("other-server").toFile();
        File otherPluginDir = tempDir.resolve("other-plugin").toFile();

        TasksUtils.setFiles(otherServerDir, otherPluginDir);

        assertTrue(TasksUtils.mkFiles());
        assertTrue(otherPluginDir.toPath().resolve("tasks.txt").toFile().isFile());
        assertTrue(otherPluginDir.toPath().resolve("backups.txt").toFile().isFile());
        assertEquals(
                TaskObj.Types.PLUGINS,
                TasksUtils.getType(otherServerDir.toPath().resolve("plugins/example.jar").toFile()));
    }
}
