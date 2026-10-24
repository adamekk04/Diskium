package org.diskium.objects;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependingFlagObjTest {

    @Test
    void isFlagDependingRejectsAllValuesWhenNoneAreAllowed() {
        DependingFlagObj<String> flag = new DependingFlagObj<>(
                "output",
                "mode",
                new Object[]{},
                "console", "file"
        );

        assertFalse(flag.isFlagDepending(Map.of("mode", "fast")));
        assertFalse(flag.isFlagDepending(Map.of("mode", "full")));
        assertFalse(flag.isFlagDepending(Map.of()));
    }

    @Test
    void isFlagDependingDistinguishesValueTypes() {
        DependingFlagObj<String> flag = new DependingFlagObj<>(
                "output",
                "level",
                new Object[]{1},
                "console", "file"
        );

        assertTrue(flag.isFlagDepending(Map.of("level", 1)));
        assertFalse(flag.isFlagDepending(Map.of("level", "1")));
        assertFalse(flag.isFlagDepending(Map.of("level", 2)));
    }

    @Test
    void isFlagDependingRequiresAllowedValue() {
        DependingFlagObj<String> flag = new DependingFlagObj<>(
                "output",
                "mode",
                new Object[]{"fast", "full"},
                "console", "file"
        );

        assertTrue(flag.isFlagDepending(Map.of("mode", "fast")));
        assertTrue(flag.isFlagDepending(Map.of("mode", "full")));

        assertFalse(flag.isFlagDepending(Map.of("mode", "safe")));
        assertFalse(flag.isFlagDepending(Map.of()));

        Map<String, Object> flagsWithNull = new HashMap<>();
        flagsWithNull.put("mode", null);
        assertFalse(flag.isFlagDepending(flagsWithNull));
    }
}
