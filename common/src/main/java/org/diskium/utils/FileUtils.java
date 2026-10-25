package org.diskium.utils;

import org.diskium.MultiplatformLogger;
import org.diskium.objects.TaskObj;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {

    private static boolean DELETE_WHILE_RUNNING_LOGS;
    private static boolean DELETE_WHILE_RUNNING_PLUGINS;

/**
     * Deletes a file immediately when allowed by the deletion policy;
     * otherwise queues a deletion task.
     *
     * @param file the file or directory to delete
     * @param type the policy category; {@code USE_TASK_QUEUE} always queues deletion
     * @return {@code true} if immediate deletion reports success or the task was saved;
     *         {@code false} otherwise
     */
    public static boolean safeDel(File file, DelSpecifier type) {
        if ((type == DelSpecifier.LOGS && DELETE_WHILE_RUNNING_LOGS)
        || (type == DelSpecifier.PLUGINS && DELETE_WHILE_RUNNING_PLUGINS)) {
            return forceDel(file);
        } else {
            return TasksUtils.add(new TaskObj(true, file, null, TasksUtils.getType(file)));
        }
    }

/**
     * Attempts immediate deletion, recursively deleting non-empty directories.
     * Failures are logged.
     *
     * @param file the file or directory to delete
     * @return {@code true} if deletion succeeds or recursive deletion was attempted;
     *         recursive deletion failures are not reflected in this result
     */
    public static boolean forceDel(File file) {
        try {
            Files.delete(file.toPath());
            return true;
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't delete file " + file.getName() + ", because it doesn't exist.");
        } catch (DirectoryNotEmptyException e) {
            delWithSubDirs(file);
            return true;
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong." + e);
        }
        return false;
    }

/**
     * Moves the source to the destination, replacing an existing destination.
     * Failures are logged.
     *
     * @param origin the source path
     * @param goal the destination path
     * @return {@code true} if the move succeeds; {@code false} if it fails
     */
    public static boolean move(File origin, File goal) {
        try {
            Files.move(Path.of(origin.toURI()), Path.of(goal.toURI()), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (FileAlreadyExistsException e) {
            MultiplatformLogger.error("Couldn't move file, because it already exists,");
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't move file, because it doesn't exist.");
        } catch (SecurityException e) {
            MultiplatformLogger.error("Couldn't move file, due to file move permissions.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while moving a file.", e);
        }

        return false;
    }

/**
     * Deletes a folder with its subdirectories.
     *
     * @param file Folder to delete.
     */
    private static void delWithSubDirs(File file) {
        try (Stream<Path> paths = Files.walk(file.toPath())) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        forceDel(path.toFile());
                    });
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong.", e);
        }
    }

/**
     * Configures whether log and plugin files may be deleted immediately.
     *
     * @param logs {@code true} to delete logs immediately; {@code false} to queue deletion
     * @param plugins {@code true} to delete plugin files immediately; {@code false} to queue deletion
     */
    public static void setUseTasks(boolean logs, boolean plugins) {
        DELETE_WHILE_RUNNING_LOGS = logs;
        DELETE_WHILE_RUNNING_PLUGINS = plugins;
    }

    public enum DelSpecifier {
        LOGS,
        PLUGINS,
        USE_TASK_QUEUE
    }
}
