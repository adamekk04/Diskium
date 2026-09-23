package org.diskium.utils;

import org.diskium.MultiplatformLogger;
import org.diskium.objects.TaskObj;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {

    private static boolean TASKS_LOGS;
    private static boolean TASKS_PLUGINS;
    private static boolean TASKS_WORLD;

    public static void safeDel(File file, DelSpecifier type) {
        if ((type == DelSpecifier.LOGS && TASKS_LOGS)
        || (type == DelSpecifier.PLUGINS && TASKS_PLUGINS)
        || (type == DelSpecifier.WORLD && TASKS_WORLD)) {
            TasksUtils.add(new TaskObj(true, file, null, "N/A"));
        }

        else {
            forceDel(file);
        }
    }

    public static void forceDel(File file) {
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't delete file " + file.getName() + ", because it doesn't exist.");
        } catch (DirectoryNotEmptyException e) {
            delWithSubDirs(file);
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong." + e);
        }
    }

    public static void move(File origin, File goal) {
        try {
            Files.move(Path.of(origin.toURI()), Path.of(goal.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (FileAlreadyExistsException e) {
            MultiplatformLogger.error("Couldn't move file while completing tasks, because it already exists,");
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't move file while completing tasks, because it doesn't exist.");
        } catch (SecurityException e) {
            MultiplatformLogger.error("Couldn't move file while completing tasks, due to file move permissions.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while moving files in tasks completing.", e);
        }
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
        TASKS_LOGS = logs;
        TASKS_PLUGINS = plugins;
        TASKS_WORLD = world;
    }

    public enum DelSpecifier {
        LOGS,
        PLUGINS,
        WORLD
    }
}
