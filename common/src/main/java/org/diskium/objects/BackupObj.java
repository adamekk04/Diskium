package org.diskium.objects;

import org.diskium.utils.TasksUtils;

import java.io.File;

public class BackupObj {
    File file;
    File itself;

/**
     * Creates new backup.
     *
     * @param file File being backed up.
     * @param itself Backup location itself.
     */
    public BackupObj(File file, File itself) {
        this.file = file;
        this.itself = itself;
    }

    public File getFile() {
        return file;
    }

    public File getItself() {
        return itself;
    }

/**
     * Gets type of backup.
     *
     * @return Type of backup.
     */
    public TaskObj.Types getType() {
        return TasksUtils.getType(file);
    }
}
