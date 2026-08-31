package org.diskium;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class FileUtils {

    public static void del(File file, boolean useTaskQueue, File pluginFolder, String type) {
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
}
