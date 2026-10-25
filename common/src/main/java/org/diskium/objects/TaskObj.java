package org.diskium.objects;

import java.io.File;

public class TaskObj {
    boolean delete;
    File file;
    File replacementFile;
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
