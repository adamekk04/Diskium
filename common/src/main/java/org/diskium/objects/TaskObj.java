package org.diskium.objects;

import java.io.File;

public class TaskObj {
    boolean delete;
    File file;
    File replacementFile;
    String type;

    public TaskObj(boolean delete, File file, File replacementFile, String type) {
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

    public String getType() {
        return type;
    }
}
