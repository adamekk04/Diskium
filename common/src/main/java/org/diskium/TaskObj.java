package org.diskium;

import java.io.File;

public class TaskObj {
    boolean delete;
    File file;
    File replacementFile;

    public TaskObj(boolean delete, File file, File replacementFile) {
        this.delete = delete;
        this.file = file;
        this.replacementFile = replacementFile;
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
}
