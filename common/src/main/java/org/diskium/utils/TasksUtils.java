package org.diskium.utils;

import org.diskium.objects.BackupObj;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.TaskObj;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TasksUtils {

    private static File serverRoot;
    private static File pluginFile;

    public static boolean mkFiles() {
        boolean buffer = false;

        if (!pluginFile.exists()) {
            if (!pluginFile.mkdirs()) {
                MultiplatformLogger.error("Could not create plugin directory");
                return false;
            }
        }

        File backups = new File(pluginFile, "backups.txt");
        File tasks = new File(pluginFile, "tasks.txt");

        try {
            if (!backups.exists()) {
                backups.createNewFile();
                buffer = true;
            }

            if (!tasks.exists()) {
                tasks.createNewFile();
                buffer = true;
            }
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while creating new files", e);
            return false;
        }

        return buffer;
    }

    public static TaskObj[] getTasks() {
        List<TaskObj> tasks = new ArrayList<>();
        File taskFile = new File(pluginFile, "tasks.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(taskFile))) {
            String pathLine;

            while ((pathLine = br.readLine()) != null) {
                File path = new File(pathLine);
                String mode = br.readLine();

                if (mode == null) {
                    return tasks.toArray(TaskObj[]::new);
                } else if (mode.equals("1")) {
                    tasks.add(new TaskObj(true, path, null, getType(path)));
                } else {
                    tasks.add(new TaskObj(false, path, new File(mode), getType(path)));
                }
            }

            return tasks.toArray(TaskObj[]::new);
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File tasks.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while getting tasks from tasks.txt", e);
        }
        return new TaskObj[0];
    }

    public static BackupObj[] getBackups() {
        List<BackupObj> backups = new ArrayList<>();
        File backupFile = new File(pluginFile, "backups.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
            String fileLine;
            String itselfLine;

            while (((fileLine = br.readLine()) != null) && ((itselfLine = br.readLine()) != null)) {
                File path = new File(fileLine);
                File itself = new File(itselfLine);

                backups.add(new BackupObj(path, itself));
            }

            return backups.toArray(BackupObj[]::new);
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access backups.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File backups.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while getting backups from backups.txt", e);
        }
        return new BackupObj[0];
    }

    public static boolean add(TaskObj task) {
        File taskFile = new File(pluginFile, "tasks.txt");

        try (FileWriter fw = new FileWriter(taskFile, true)) {
            fw.write(task.getFile().toString());
            if (task.getDelete()) {
                fw.write("1");
            } else {
                fw.write(task.getReplacementFile().toString());
            }
            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access backups.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File tasks.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding backups to backups.txt", e);
        }
        return false;
    }

    public static boolean add(BackupObj backup) {
        File backupFile = new File(pluginFile, "backup.txt");

        try (FileWriter fw = new FileWriter(backupFile, true)) {
            fw.write(backup.getFile().toString());
            fw.write(backup.getItself().toString());
            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File backups.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to tasks.txt", e);
        }
        return false;
    }

    public static void complete(TaskObj[] tasks) {
        for (TaskObj task : tasks) {
            if (task.getDelete()) {
                FileUtils.forceDel(task.getFile());
            } else {
                FileUtils.move(task.getFile(), task.getReplacementFile());
            }
        }
    }

    public static void complete(BackupObj[] backups) {
        for (BackupObj backup : backups) {
            FileUtils.move(backup.getItself(), backup.getFile());
        }
    }

    public static void remove(TaskObj task) {
        Path taskFile = new File(pluginFile, "tasks.txt").toPath();

        try {
            List<String> lines = Files.readAllLines(taskFile);

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals(task.getFile().toString())) {
                    lines.remove(i);
                    if (i < lines.size()) {
                        lines.remove(i);
                    }
                    break;
                }
            }

            Files.write(taskFile, lines);
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot remove task from tasks.txt, due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File tasks.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while removing task from tasks.txt.", e);
        }
    }

    public static void remove(BackupObj backup) {
        Path backupFile = new File(pluginFile, "backups.txt").toPath();

        try {
            List<String> lines = Files.readAllLines(backupFile);

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals(backup.getFile().toString())) {
                    lines.remove(i);
                    if (i < lines.size()) {
                        lines.remove(i);
                    }
                    break;
                }
            }

            Files.write(backupFile, lines);
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot remove backup from backups.txt, due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File backups.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while removing backup from backups.txt.", e);
        }
    }

    public static TaskObj.Types getType(File file) {
        if (file.getPath().startsWith(new File(serverRoot, "plugins").toString())) return TaskObj.Types.PLUGINS;
        if (file.getPath().endsWith(".mca")) return TaskObj.Types.WORLD;
        if (file.getPath().startsWith(new File(serverRoot, "logs").toString())) return TaskObj.Types.LOGS;
        else return TaskObj.Types.OTHER;
    }

    public static int nonNegative(int x) {
        return Math.max(x, 0);
    }

    public static void setFiles(File serverDir, File pluginDir) {
        serverRoot = serverDir;
        pluginFile = pluginDir;
    }
}
