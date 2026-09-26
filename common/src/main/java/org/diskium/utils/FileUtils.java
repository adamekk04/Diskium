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
    private static boolean DELETE_WHILE_RUNNING_WORLD;

    public static boolean safeDel(File file, DelSpecifier type) {
        if ((type == DelSpecifier.LOGS && DELETE_WHILE_RUNNING_LOGS)
        || (type == DelSpecifier.PLUGINS && DELETE_WHILE_RUNNING_PLUGINS)
        || (type == DelSpecifier.WORLD && DELETE_WHILE_RUNNING_WORLD)) {
            return TasksUtils.add(new TaskObj(true, file, null, TasksUtils.getType(file)));
        } else {
            return forceDel(file);
        }
    }

    public static boolean forceDel(File file) {
        try {
            Files.delete(file.toPath());
            return true;
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't delete file " + file.getName() + ", because it doesn't exist.");
        } catch (DirectoryNotEmptyException e) {
            delWithSubDirs(file);
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong." + e);
        }
        return false;
    }

    public static void move(File origin, File goal) {
        try {
            Files.move(Path.of(origin.toURI()), Path.of(goal.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (FileAlreadyExistsException e) {
            MultiplatformLogger.error("Couldn't move file, because it already exists,");
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't move file, because it doesn't exist.");
        } catch (SecurityException e) {
            MultiplatformLogger.error("Couldn't move file, due to file move permissions.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while moving a file.", e);
        }
    }

    public static boolean getDeleteWhileRunningWorld() {
        return DELETE_WHILE_RUNNING_WORLD;
    }

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


    public static void setUseTasks(boolean logs, boolean plugins, boolean world) {
        DELETE_WHILE_RUNNING_LOGS = logs;
        DELETE_WHILE_RUNNING_PLUGINS = plugins;
        DELETE_WHILE_RUNNING_WORLD = world;
    }

    public enum DelSpecifier {
        LOGS,
        PLUGINS,
        WORLD
    }
}
