package org.diskium.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;
import org.diskium.MultiplatformLogger;
import org.diskium.objects.BackupObj;
import org.diskium.objects.TaskObj;

public class FileUtils {

    private static boolean DELETE_WHILE_RUNNING_LOGS;
    private static boolean DELETE_WHILE_RUNNING_PLUGINS;
    private static boolean CREATE_BACKUPS;
    private static File PLUGIN_DIR;

    /**
      * Deletes a file immediately when allowed by the deletion policy; otherwise queues a deletion
      * task.
      *
      * @param file the file or directory to delete
      * @param type the policy category; {@code USE_TASK_QUEUE} always queues deletion
      * @return {@code true} if immediate deletion reports success or the task was saved; {@code false}
      *     otherwise
      */
    public static boolean safeDel(File file, DelSpecifier type) {
        if ((type == DelSpecifier.LOGS && DELETE_WHILE_RUNNING_LOGS)
                || (type == DelSpecifier.PLUGINS && DELETE_WHILE_RUNNING_PLUGINS)) {
            if (createBackup(file)) {
                return forceDel(file);
            }
        } else {
            if (createBackup(file)) {
                return TasksUtils.add(new TaskObj(true, file, null, TasksUtils.getType(file)));
            }
        }

        return false;
    }

    /**
      * Attempts immediate deletion, recursively deleting non-empty directories. Failures are logged.
      *
      * @param file the file or directory to delete
      * @return {@code true} if deletion succeeds or recursive deletion was attempted; recursive
      *     deletion failures are not reflected in this result
      */
    public static boolean forceDel(File file) {
        try {
            Files.delete(file.toPath());
            return true;
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error(
                    "Couldn't delete file " + file.getName() + ", because it doesn't exist.");
        } catch (DirectoryNotEmptyException e) {
            return delWithSubDirs(file);
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong." + e);
        }
        return false;
    }

    /**
      * Moves the source to the destination, replacing an existing destination. Failures are logged.
      *
      * @param origin the source path
      * @param goal the destination path
      * @return {@code true} if the move succeeds; {@code false} if it fails
      */
    public static boolean move(File origin, File goal) {
        try {
            Files.move(
                    Path.of(origin.toURI()), Path.of(goal.toURI()), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (FileAlreadyExistsException e) {
            MultiplatformLogger.error("Couldn't move file, because it already exists,");
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't move file, because it doesn't exist.");
        } catch (SecurityException e) {
            MultiplatformLogger.error("Couldn't move file, due to file move permissions.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while moving a file.", e);
        }

        return false;
    }

    /**
      * Copies the source to the destination, replacing an existing destination. Failures are logged.
      *
      * @param origin the source path
      * @param goal the destination path
      * @return {@code true} if the copy succeeds; {@code false} if it fails
      */
    public static boolean copy(File origin, File goal) {
        try {
            Files.copy(Path.of(origin.toURI()), Path.of(goal.toURI()));
            return true;
        } catch (FileAlreadyExistsException e) {
            MultiplatformLogger.error("Couldn't move file, because it already exists,");
        } catch (NoSuchFileException e) {
            MultiplatformLogger.error("Couldn't move file, because it doesn't exist.");
        } catch (SecurityException e) {
            MultiplatformLogger.error("Couldn't move file, due to file move permissions.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong while moving a file.", e);
        }

        return false;
    }

    /**
      * Deletes a folder with its subdirectories.
      *
      * @param file Folder to delete.
      */
    private static boolean delWithSubDirs(File file) {
        try (Stream<Path> paths = Files.walk(file.toPath())) {
            Stream<Path> sorted = paths.sorted(Comparator.reverseOrder());

            for (Path p : sorted.toList()) {
                if (!forceDel(p.toFile())) {
                    return false;
                }
            }
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong.", e);
            return false;
        }

        return true;
    }

    /**
      * Creates a backup, if config allows it.
      *
      * @param file File, you want to create a backup of.
      * @return Whether the action succeeded.
      */
    public static boolean createBackup(File file) {
        if (!file.isFile()) {
            return false;
        }

        if (CREATE_BACKUPS) {
            Path backupDir = PLUGIN_DIR.toPath().resolve("backups").resolve(UUID.randomUUID().toString());

            try {
                Path destination = Files.createDirectories(backupDir);
                return TasksUtils.add(new BackupObj(file, new File(destination.toFile(), file.getName())));
            } catch (IOException e) {
                MultiplatformLogger.error("Couldn't create backup directory.", e);
                return false;
            }
        }

        return true;
    }

    /**
     * Computes the SHA256 hash of {@code file}.
     * @param file File you want to compute the hash from.
     * @return Hash of the file or null, if exception occurs.
     */
    public static String getSha(File file) {
        if (file == null) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }

                return HexFormat.of().formatHex(digest.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            MultiplatformLogger.error("Cannot find SHA-256 algorithm.");
        } catch (IOException e) {
            MultiplatformLogger.error("Something went wrong.", e);
        }

        return null;
    }

    /**
      * Configures whether log and plugin files may be deleted immediately.
      *
      * @param logs {@code true} to delete logs immediately; {@code false} to queue deletion
      * @param plugins {@code true} to delete plugin files immediately; {@code false} to queue deletion
      */
    public static void setUseTasks(boolean logs, boolean plugins, boolean backups, File pluginDir) {
        DELETE_WHILE_RUNNING_LOGS = logs;
        DELETE_WHILE_RUNNING_PLUGINS = plugins;
        CREATE_BACKUPS = backups;
        PLUGIN_DIR = pluginDir;
    }

    public enum DelSpecifier {
        LOGS,
        PLUGINS,
        USE_TASK_QUEUE
    }
}
