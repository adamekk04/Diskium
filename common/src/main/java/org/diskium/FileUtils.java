package org.diskium;

import java.io.File;

public class FileUtils {
    public static boolean del(File file, boolean useTaskQueue, File pluginFolder, String type) {
        if (useTaskQueue) {
            TasksUtils.add(pluginFolder, new TaskObj(true, file, null, type));
            return true;
        } else {
            return file.delete(); // TODO: Provide more information, if something fails while deleting
        }
    }
}
