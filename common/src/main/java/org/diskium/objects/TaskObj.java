package org.diskium.objects;

import java.io.File;

public class TaskObj {
    boolean delete;
    File file;
    File replacementFile;
    Types type;

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

    public static enum Types {
        BACKUP,
        LOGS,
        PLUGINS,
        WORLD,
        OTHER
    }
}
