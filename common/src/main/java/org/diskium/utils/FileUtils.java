package org.diskium.utils;

import org.diskium.MultiplatformLogger;
import org.diskium.objects.TaskObj;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileUtils {

    public static void safeDel(File file, boolean useTaskQueue, File pluginFolder, String type) {
        if (useTaskQueue) {
            TasksUtils.add(pluginFolder, new TaskObj(true, file, null, type));
        } else {
            try {
                Files.delete(file.toPath());
            } catch (NoSuchFileException e) {
                MultiplatformLogger.error("Couldn't delete file " + file.getName() + ", because it doesn't exist");
            } catch (IOException e) {
                MultiplatformLogger.error("Something went wrong." + e);
            }
        }
    }

    public static void del(File file) {
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't delete file " + file.getName() + ", because it doesn't exist.");
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
}
