package org.diskium.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {

    @Test
    void isValidDateValidatesRangeAndFormat() {
        assertTrue(DateUtils.isValidDate("2026-09-28", "2026-09-28"));
        assertTrue(DateUtils.isValidDate("2026-09-27", "2026-09-28"));
        assertFalse(DateUtils.isValidDate("2026-09-29", "2026-09-28"));

        assertTrue(DateUtils.isValidDate("2026-09-28", null));
        assertTrue(DateUtils.isValidDate(null, "2026-09-28"));
        assertTrue(DateUtils.isValidDate(null, null));

        assertFalse(DateUtils.isValidDate("invalid", "2026-09-28"));
        assertFalse(DateUtils.isValidDate("2026-09-28", "invalid"));
        assertFalse(DateUtils.isValidDate("invalid", null));
        assertFalse(DateUtils.isValidDate(null, "invalid"));
    }

    @Test
    void isValidStringChecksDatePatternCharacters() {
        assertTrue(DateUtils.isValidString("yyyy/mm/dd"));
        assertTrue(DateUtils.isValidString("dd-mm-yyyy"));
        assertTrue(DateUtils.isValidString("mm.dd.yyyy"));

        assertFalse(DateUtils.isValidString("yyy/mm/dd"));
        assertFalse(DateUtils.isValidString("yyyy/m/dd"));
        assertFalse(DateUtils.isValidString("yyyy/mm/d"));
        assertFalse(DateUtils.isValidString("yyyy/mm/ddd"));
        assertFalse(DateUtils.isValidString("2026-09-28"));
    }
}
