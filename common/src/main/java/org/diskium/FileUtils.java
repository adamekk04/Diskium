package org.diskium;

import java.io.File;

public class FileUtils {
    public static boolean del(File file, boolean useTaskQueue, File pluginFolder) {
        if (useTaskQueue) {
            TasksManagement.addTask(pluginFolder, new TaskObj(true, file, null));
            return true;
        } else {
            return file.delete(); // TODO: Provide more information, if something fails while deleting
        }
    }
}
