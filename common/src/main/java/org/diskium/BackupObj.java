package org.diskium;

import java.io.File;

public class BackupObj {
    File file;
    File itself;

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

    public String getType() {
        return TasksUtils.getType(file);
    }
}
