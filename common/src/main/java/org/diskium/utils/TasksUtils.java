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

    /**
     * Creates the plugin directory and any missing {@code backups.txt} or {@code tasks.txt} files.
     *
     * @return {@code true} if at least one registry file was created and no I/O error occurred;
     * {@code false} if both files already existed or creation failed
     */
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

    /**
     * Fetches all tasks from {@code tasks.txt}
     *
     * @return Array of {@code TaskObj}
     */
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

    /**
     * Fetches all backups from {@code backups.txt}
     *
     * @return Array of {@code BackupObj}
     */
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

    /**
     * Adds a task into {@code tasks.txt}.
     *
     * @param task Task you want to add.
     * @return Whether the action was successful
     */
    public static boolean add(TaskObj task) {
        File taskFile = new File(pluginFile, "tasks.txt");

        try (FileWriter fw = new FileWriter(taskFile, true)) {
            fw.write(task.getFile().toString());
            fw.write(System.lineSeparator());

            if (task.getDelete()) {
                fw.write("1");
            } else {
                fw.write(task.getReplacementFile().toString());
            }

            fw.write(System.lineSeparator());
            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access tasks.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File tasks.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding backups to tasks.txt", e);
        }
        return false;
    }

    /**
     * Adds a backup into {@code backups.txt} and creates
     * a backup of that file.
     *
     * @param backup Backup you want to add.
     * @return Whether the action was successful
     */
    public static boolean add(BackupObj backup) {
        File backupFile = new File(pluginFile, "backups.txt");

        if (!FileUtils.copy(backup.getFile(), backup.getItself())) {
            return false;
        }

        try (FileWriter fw = new FileWriter(backupFile, true)) {
            fw.write(backup.getFile().toString());
            fw.write(System.lineSeparator());
            fw.write(backup.getItself().toString());
            fw.write(System.lineSeparator());

            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot access backups.txt due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File backups.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while adding tasks to backups.txt", e);
        }
        return false;
    }

    /**
     * Executes each deletion or move and then removes its task record.
     *
     * @param tasks the tasks to execute
     */
    public static void complete(TaskObj[] tasks) {
        for (TaskObj task : tasks) {
            boolean success;
            if (task.getDelete()) {
                success = FileUtils.forceDel(task.getFile());
            } else {
                success = FileUtils.move(task.getFile(), task.getReplacementFile());
            }
            if (success) {
                remove(task);
            }
        }
    }

    /**
     * Moves each backup to its original path, replacing an existing destination,
     * and removes its registry record.
     *
     * @param backups the backups to restore
     */
    public static void complete(BackupObj[] backups) {
        for (BackupObj backup : backups) {
            if (FileUtils.move(backup.getItself(), backup.getFile())) {
                remove(backup);
            }
        }
    }

    /**
     * Removes a task from {@code tasks.txt}.
     *
     * @param task The task you want to remove.
     */
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

    /**
     * Removes a backup from {@code backups.txt}.
     *
     * @param backup The backup you want to remove.
     */
    public static void remove(BackupObj backup) {
        FileUtils.forceDel(backup.getItself());
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

    /**
     * Classifies a path by its location or filename suffix.
     *
     * @param file the path to classify
     * @return {@code PLUGINS} for paths starting with the plugins directory,
     * {@code WORLD} for the {@code .mca} suffix,
     * {@code LOGS} for paths starting with the logs directory,
     * or {@code OTHER}
     */
    public static TaskObj.Types getType(File file) {
        if (file.getPath().startsWith(new File(serverRoot, "plugins").toString())) return TaskObj.Types.PLUGINS;
        if (file.getPath().endsWith(".mca")) return TaskObj.Types.WORLD;
        if (file.getPath().startsWith(new File(serverRoot, "logs").toString())) return TaskObj.Types.LOGS;
        else return TaskObj.Types.OTHER;
    }

    /**
     * Gives you a maximum of 0 and {@code x}
     *
     * @param x Number, you want ot get.
     * @return {@code x}, but 0, if it's negative.
     */
    public static int nonNegative(int x) {
        return Math.max(x, 0);
    }

    /**
     * Sets server directory and plugin directory.
     *
     * @param serverDir Server directory.
     * @param pluginDir Plugin directory.
     */
    public static void setFiles(File serverDir, File pluginDir) {
        serverRoot = serverDir;
        pluginFile = pluginDir;
    }
}
