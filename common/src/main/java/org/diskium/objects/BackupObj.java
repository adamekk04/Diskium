package org.diskium.objects;

import java.io.File;

import org.diskium.utils.FileUtils;
import org.diskium.utils.TasksUtils;

public class BackupObj {
    File file;
    String fileHash;
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
        this.fileHash = FileUtils.getSha(file);
    }

    public File getFile() {
        return file;
    }

    public String getFileHash() {
        return fileHash;
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
