package org.diskium.utils;

import org.diskium.objects.BackupObj;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.TaskObj;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TasksUtils {

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
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to tasks.txt");
        }
        return null;
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
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to tasks.txt");
        }
        return null;
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
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to tasks.txt");
        }
        return false;
    }

    public static boolean add(File pluginFolder, BackupObj backup) {
        File backupFile = new File(pluginFolder, "backup.txt");

        try (FileWriter fw = new FileWriter(backupFile, true)) {
            fw.write(backup.getFile().toString());
            fw.write(backup.getItself().toString());
            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to tasks.txt");
        }
        return false;
    }

    public static void complete(TaskObj[] tasks) {
        for (TaskObj task : tasks) {
            if (task.getDelete()) {
                FileUtils.del(task.getFile());
            } else {
                FileUtils.move(task.getFile(), task.getReplacementFile());
            }
        }
    }

    public static void complete(BackupObj[] backups) { // TODO: Make methods for those long try catch blocks
        for (BackupObj backup : backups) {
            FileUtils.move(backup.getItself(), backup.getFile());
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

    public static void create(File dir, boolean task) {
        if (task) new File(dir, "tasks.txt");
        else new File(dir, "backups.txt");
    }
}
