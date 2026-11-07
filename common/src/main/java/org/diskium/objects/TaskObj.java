package org.diskium.objects;

import org.diskium.utils.FileUtils;

import java.io.File;

public class TaskObj {
    boolean delete;
    File file;
    File replacementFile;
    String fileHash;
    String replacementFileHash;
    Types type;

    /**
      * Creates a file deletion or move.
      *
      * @param delete {@code true} to delete the target; {@code false} to move it
      * @param file the file or directory to delete or move
      * @param replacementFile the move destination, or {@code null} for deletion
      * @param type the task category
      */
    public TaskObj(boolean delete, File file, File replacementFile, Types type) {
        this.delete = delete;
        this.file = file;
        this.replacementFile = replacementFile;
        this.type = type;
        this.fileHash = FileUtils.getSha(file);
        this.replacementFileHash = replacementFile == null ? null : FileUtils.getSha(replacementFile);
    }

    public TaskObj(boolean delete, File file, File replacementFile, String hash, Types type) {
        this(delete, file, replacementFile, type);
        this.fileHash = hash;
    }

    public boolean getDelete() {
        return delete;
    }

    public File getFile() {
        return file;
    }

    public File getReplacementFile() {
        return replacementFile;
    }

    public String getFileHash() {
        return fileHash;
    }

    public String getReplacementFileHash() {
        return replacementFileHash;
    }

    public Types getType() {
        return type;
    }

    public enum Types {
        BACKUP,
        LOGS,
        PLUGINS,
        WORLD,
        OTHER
    }
}
