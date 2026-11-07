package org.diskium.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.diskium.MultiplatformLogger;
import org.diskium.mca.Parser;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;

public class TasksUtils {

    /*
    tasks.txt has this format:
    file
    sha of file
    replacementFile or '1', if its deletion task
    sha of replacementFile or anything, if its deletion task

    backups.txt has this format:
    file
    sha of file
    destination
     */

    private static File serverRoot;
    private static File pluginFile;

    /**
      * Creates the plugin directory and any missing {@code backups.txt} or {@code tasks.txt} files.
      *
      * @return {@code true} if at least one registry file was created and no I/O error occurred;
      *     {@code false} if both files already existed or creation failed
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
                buffer = backups.createNewFile();
            }

            if (!tasks.exists()) {
                buffer = tasks.createNewFile();
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
                String sha = br.readLine();
                String mode = br.readLine();
                String sha2 = br.readLine();

                if (sha == null || mode == null || sha2 == null) {
                    MultiplatformLogger.error("tasks.txt may be corrupted.");
                }

                if (mode == null) {
                    return tasks.toArray(TaskObj[]::new);
                } else if (mode.equals("1")) {
                    TaskObj task = new TaskObj(true, path, null, sha, getType(path));

                    if (Objects.equals(task.getFileHash(), FileUtils.getSha(path))
                            || Files.exists(deletionQuarantine(task))) {
                        tasks.add(task);
                    } else {
                        MultiplatformLogger.warn("Invalid sha of task " + task);
                    }
                } else {
                    TaskObj task = new TaskObj(false, path, new File(mode), getType(path));

                    if (Objects.equals(task.getFileHash(), sha)
                            && Objects.equals(task.getReplacementFileHash(), sha2)) {
                        tasks.add(task);
                    } else {
                        MultiplatformLogger.warn("Invalid sha of task " + task);
                    }
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
            String fileLineHash;
            String itselfLine;

            while ((fileLine = br.readLine()) != null
                    && (fileLineHash = br.readLine()) != null
                    && (itselfLine = br.readLine()) != null) {
                File path = new File(fileLine);
                File itself = new File(itselfLine);
                BackupObj backup = new BackupObj(path, itself, fileLineHash);

                if (Objects.equals(FileUtils.getSha(backup.getItself()), fileLineHash)) {
                    backups.add(backup);
                } else {
                    MultiplatformLogger.warn("Invalid sha of backup " + backup);
                }
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
        if (task.getFile().toString().contains("\n") || (task.getReplacementFile() != null && task.getReplacementFile().toString().contains("\n"))) {
            MultiplatformLogger.error("Cannot add new task, because file contains line break.");
            return false;
        }

        File taskFile = new File(pluginFile, "tasks.txt");

        try (FileWriter fw = new FileWriter(taskFile, true)) {
            fw.write(task.getFile().toString());
            fw.write(System.lineSeparator());
            fw.write(task.getFileHash());
            fw.write(System.lineSeparator());

            if (task.getDelete()) {
                fw.write("1");
                fw.write(System.lineSeparator());
                fw.write("1");
            } else {
                fw.write(task.getReplacementFile().toString());
                fw.write(System.lineSeparator());
                fw.write(task.getReplacementFileHash());
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
      * Adds a backup into {@code backups.txt} and creates a backup of that file.
      *
      * @param backup Backup you want to add.
      * @return Whether the action was successful
      */
    public static boolean add(BackupObj backup) {
        if (backup.getFile().toString().contains("\n") || backup.getItself().toString().contains("\n")) {
            MultiplatformLogger.error("Cannot add new backup, because file contains line break.");
            return false;
        }

        File backupFile = new File(pluginFile, "backups.txt");

        if (!FileUtils.copy(backup.getFile(), backup.getItself())) {
            return false;
        }
        if (!Objects.equals(backup.getFileHash(), FileUtils.getSha(backup.getItself()))) {
            FileUtils.forceDel(backup.getItself());
            MultiplatformLogger.warn("Corrupted backup got deleted");
            return false;
        }

        try (FileWriter fw = new FileWriter(backupFile, true)) {
            fw.write(backup.getFile().toString());
            fw.write(System.lineSeparator());
            fw.write(backup.getFileHash());
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
            if (task.getDelete()) {
                Path quarantine = deletionQuarantine(task);
                try {
                    if (!Files.exists(quarantine)) {
                        if (!Objects.equals(task.getFileHash(), FileUtils.getSha(task.getFile()))) {
                            continue;
                        }
                        Files.move(task.getFile().toPath(), quarantine);
                    }

                    if (remove(task)) {
                        if (!FileUtils.forceDel(quarantine.toFile())) {
                            MultiplatformLogger.warn("Couldn't delete quarantined task file: " + quarantine);
                        }
                    } else {
                        MultiplatformLogger.error(
                                "File moved to deletion quarantine, but task remains registered: " + task);
                    }
                } catch (IOException | SecurityException e) {
                    MultiplatformLogger.error("Couldn't move task file to deletion quarantine: " + task, e);
                }
                continue;
            }

            if (Objects.equals(task.getFileHash(), FileUtils.getSha(task.getFile()))
            && Objects.equals(task.getReplacementFileHash(), FileUtils.getSha(task.getReplacementFile()))) {
                boolean success;

                File modifiedRegion = task.getFile();
                File region = task.getReplacementFile();

                if (!Parser.areModified(region, modifiedRegion)) {
                    success = FileUtils.move(task.getFile(), task.getReplacementFile());
                } else {
                    success = false;
                }

                if (success && !remove(task)) {
                    MultiplatformLogger.error("Task completed, but couldn't remove it from tasks.txt: " + task);
                }
            }
        }
    }

    /**
      * Removes a task from {@code tasks.txt}.
      *
      * @param task The task you want to remove.
      */
    public static boolean remove(TaskObj task) {
        Path taskFile = new File(pluginFile, "tasks.txt").toPath();

        try {
            List<String> lines = Files.readAllLines(taskFile);
            String mode = task.getDelete() ? "1" : task.getReplacementFile().toString();
            String replacementHash = task.getDelete() ? "1" : task.getReplacementFileHash();

            for (int i = 0; i + 3 < lines.size(); i += 4) {
                if (Objects.equals(lines.get(i), task.getFile().toString())
                        && Objects.equals(lines.get(i + 1), task.getFileHash())
                        && Objects.equals(lines.get(i + 2), mode)
                        && Objects.equals(lines.get(i + 3), replacementHash)) {
                    lines.subList(i, i + 4).clear();
                    Path temporaryFile = Files.createTempFile(taskFile.getParent(), ".tasks-", ".tmp");
                    try {
                        Files.write(temporaryFile, lines);
                        try {
                            Files.move(
                                    temporaryFile,
                                    taskFile,
                                    StandardCopyOption.ATOMIC_MOVE,
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (AtomicMoveNotSupportedException e) {
                            Files.move(temporaryFile, taskFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } finally {
                        Files.deleteIfExists(temporaryFile);
                    }
                    return true;
                }
            }

            return false;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot remove task from tasks.txt, due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File tasks.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while removing task from tasks.txt.", e);
        }
        return false;
    }

    static Path deletionQuarantine(TaskObj task) {
        String id = UUID.nameUUIDFromBytes(
                (task.getFile().getAbsolutePath() + "\n" + task.getFileHash())
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .toString();
        Path file = task.getFile().toPath();
        return file.resolveSibling(file.getFileName() + ".diskium-delete-" + id);
    }

    /**
      * Moves each verified backup to its original path and removes its registry record.
      *
      * @param backups the backups to restore
      */
    public static void complete(BackupObj[] backups) {
        for (BackupObj backup : backups) {
            if (Objects.equals(backup.getFileHash(), FileUtils.getSha(backup.getItself()))
                    && FileUtils.move(backup.getItself(), backup.getFile())) {
                remove(backup);
            } else {
                MultiplatformLogger.warn("Backup " + backup + " skipped, because it has invalid sha.");
            }
        }
    }

    /**
      * Removes a backup from {@code backups.txt}.
      *
      * @param backup The backup you want to remove.
      */
    public static boolean remove(BackupObj backup) {
        Path backupFile = new File(pluginFile, "backups.txt").toPath();

        try {
            List<String> lines = Files.readAllLines(backupFile);

            for (int i = 0; i + 2 < lines.size(); i += 3) {
                if (Objects.equals(lines.get(i), backup.getFile().toString())
                        && Objects.equals(lines.get(i + 1), backup.getFileHash())
                        && Objects.equals(lines.get(i + 2), backup.getItself().toString())) {
                    lines.subList(i, i + 3).clear();
                    break;
                }
            }

            Files.write(backupFile, lines);
            return true;
        } catch (SecurityException e) {
            MultiplatformLogger.error("Cannot remove backup from backups.txt, due to security reasons.");
        } catch (FileNotFoundException e) {
            MultiplatformLogger.warn("File backups.txt do not exist, creating a new one");
            mkFiles();
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while removing backup from backups.txt.", e);
        }

        return false;
    }

    /**
     * Restores a backup. It checks, if hash of the backup is correct.
     *
     * @param backup A backup you want to restore.
     */
    public static void restore(BackupObj backup) {
        if (!backup.getFileHash().equals(FileUtils.getSha(backup.getItself()))) {
            MultiplatformLogger.warn("Hash of " + backup + " is wrong.");
            return;
        }

        TaskObj task = new TaskObj(
                false,
                backup.getItself(),
                backup.getFile(),
                backup.getFileHash(),
                TaskObj.Types.BACKUP
        );

        if (remove(backup)) {
            add(task);
        }
    }

    /**
      * Classifies a path by its location or filename suffix.
      *
      * @param file the path to classify
      * @return {@code PLUGINS} for paths starting with the plugins directory, {@code WORLD} for the
      *     {@code .mca} suffix, {@code LOGS} for paths starting with the logs directory, or {@code
      *     OTHER}
      */
    public static TaskObj.Types getType(File file) {
        if (file.getPath().startsWith(new File(serverRoot, "plugins").toString()))
            return TaskObj.Types.PLUGINS;
        if (file.getPath().endsWith(".mca")) return TaskObj.Types.WORLD;
        if (file.getPath().startsWith(new File(serverRoot, "logs").toString()))
            return TaskObj.Types.LOGS;
        else return TaskObj.Types.OTHER;
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
