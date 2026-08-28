package org.diskium;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class TasksUtils { // TODO: Merge tasks and backups

    public static boolean fileExists(File folder, boolean task) {
        if (!folder.exists()) return false;
        File file;
        if (task) file = new File(folder, "backups.txt");
        else file = new File(folder, "tasks.txt");
        return file.exists();
    }

    public static TaskObj[] getTasks(File folder) {
        List<TaskObj> tasks = new ArrayList<>();
        File taskFile = new File(folder, "tasks.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(taskFile))) {
            String pathLine;

            while ((pathLine = br.readLine()) != null) {
                File path = new File(pathLine);
                String mode = br.readLine();

                if (mode.equals("1")) {
                    tasks.add(new TaskObj(true, path, null, getType(path)));
                } else {
                    tasks.add(new TaskObj(false, path, new File(mode), getType(path)));
                }
            }

            return tasks.toArray(new TaskObj[0]);
        } catch (Exception e) { // TODO: Provide more info
            return null;
        }
    }

    public static BackupObj[] getBackups(File folder) {
        List<BackupObj> backups = new ArrayList<>();
        File backupFile = new File(folder, "backups.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
            String fileLine;
            String itselfLine;

            while (((fileLine = br.readLine()) != null) && ((itselfLine = br.readLine()) != null)) {
                File path = new File(fileLine);
                File itself = new File(itselfLine);

                backups.add(new BackupObj(path, itself));
            }

            return backups.toArray(new BackupObj[0]);
        } catch (Exception e) { // TODO: Provide more info
            return null;
        }
    }

    public static boolean add(File pluginFolder, TaskObj task) {
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

    public static void complete(TaskObj[] tasks) {
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

    public static void remove(TaskObj task) { // TODO: Finish this

    }

    public static void remove(BackupObj backup) { // TODO: Finish this

    }

    public static String getType(File file) { // TODO: Prevent edge cases by not using contains
        if (file.getPath().contains("plugins")) return "Plugin";
        if (file.getPath().contains(".mca")) return "World";
        if (file.getPath().contains(".log")) return "Log";
        else return "N/A";
    }
}
