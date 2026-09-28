package org.diskium.objects;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class TaskObjTest {

    @Test
    void storesReplacementTask() {
        File original = new File("world/region/r.0.0.mca");
        File replacement = new File("backups/r.0.0.mca");

        TaskObj task = new TaskObj(
                false,
                original,
                replacement,
                TaskObj.Types.WORLD
        );

        assertFalse(task.getDelete());
        assertSame(original, task.getFile());
        assertSame(replacement, task.getReplacementFile());
        assertEquals(TaskObj.Types.WORLD, task.getType());
    }

    @Test
    void storesDeleteTask() {
        File file = new File("plugins/example.jar");

        TaskObj task = new TaskObj(
                true,
                file,
                null,
                TaskObj.Types.PLUGINS
        );

        assertTrue(task.getDelete());
        assertSame(file, task.getFile());
        assertNull(task.getReplacementFile());
        assertEquals(TaskObj.Types.PLUGINS, task.getType());
    }
}