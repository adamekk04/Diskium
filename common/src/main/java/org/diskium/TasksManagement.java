package org.diskium;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class TasksManagement {

    public static boolean taskFileExists(File folder) {
        if (!folder.exists()) return false;
        File file = new File(folder, "tasks.txt");
        if (!file.exists()) return false;
        return true;
    }

    public static TaskObj[] getTasks(File folder) {
        List<TaskObj> tasks = new ArrayList<>();
        File taskFile = new File(folder, "tasks.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(taskFile))) {
            while (true) {
                File path = new File(br.readLine());
                String mode = br.readLine();

                if (mode.equals("1")) {
                    tasks.add(new TaskObj(true, path, null));
                } else {
                    tasks.add(new TaskObj(false, path, new File(mode)));
                }

                return tasks.toArray(new TaskObj[0]);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean addTask(File pluginFolder, TaskObj task) {
        File taskFile = new File(pluginFolder, "tasks.txt");

        try (FileWriter fw = new FileWriter(taskFile, true)) {
            fw.write(task.getFile().toString());
            if (task.getDelete()) {
                fw.write("1");
            } else {
                fw.write(task.getReplacementFile().toString());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void doTasks(TaskObj[] tasks) {
        for (TaskObj task : tasks) {
            if (task.getDelete()) {
                task.getFile().delete(); // TODO: Provide more information, if something fails while deleting
            } else {
                try {
                    Files.move(Path.of(task.getFile().toURI()), Path.of(task.getReplacementFile().toURI()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    // TODO: Return something, if moving fails
                }
            }
        }
    }
}
