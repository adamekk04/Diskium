package org.diskium.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FlagObjTest {

    @Test
    void suggestMatchesOnlyBeginningOfOption() {
        FlagObj<String> flag = new FlagObj<>("mode", "fast", "breakfast");

        assertArrayEquals(new String[]{"fast"}, flag.suggest("FA"));
        assertArrayEquals(new String[]{}, flag.suggest("AST"));
        assertArrayEquals(new String[]{"breakfast"}, flag.suggest("BRE"));
    }

    @Test
    void suggestRejectsInputLongerThanOption() {
        FlagObj<String> flag = new FlagObj<>("mode", "fast", "full");

        assertArrayEquals(new String[]{}, flag.suggest("FASTER"));
        assertArrayEquals(new String[]{}, flag.suggest("FULLER"));
        assertArrayEquals(new String[]{"fast"}, flag.suggest("FAST"));
    }

    @Test
    void suggestMatchesPrefixIgnoringCase() {
        FlagObj<String> flag = new FlagObj<>("mode", "fast", "Fast", "FULL");

        assertArrayEquals(new String[]{"fast", "Fast", "FULL"}, flag.suggest("f"));
        assertArrayEquals(new String[]{"fast", "Fast", "FULL"}, flag.suggest("F"));
        assertArrayEquals(new String[]{"fast", "Fast"}, flag.suggest("FAST"));
        assertArrayEquals(new String[]{"FULL"}, flag.suggest("fu"));
    }

    @Test
    void getValueClassPreservesDeclaredTypeWithoutOptions() {
        FlagObj<String> textFlag = new FlagObj<>("name");
        FlagObj<Integer> numberFlag = new FlagObj<>("limit");
        FlagObj<Boolean> booleanFlag = new FlagObj<>("verbose");

        assertEquals(String.class, textFlag.getValueClass());
        assertEquals(Integer.class, numberFlag.getValueClass());
        assertEquals(Boolean.class, booleanFlag.getValueClass());
    }

    @Test
    void suggestReturnsEmptyWhenNoOptionsExist() {
        FlagObj<String> flag = new FlagObj<>("verbose");

        assertArrayEquals(new String[]{}, flag.suggest(""));
        assertArrayEquals(new String[]{}, flag.suggest("true"));
    }

    @Test
    void suggestFiltersNumericOptionsByTextPrefix() {
        FlagObj<Integer> flag = new FlagObj<>("limit", 1, 10, 20);

        assertArrayEquals(new String[]{"1", "10"}, flag.suggest("1"));
        assertArrayEquals(new String[]{"20"}, flag.suggest("2"));
        assertArrayEquals(new String[]{}, flag.suggest("3"));
        assertArrayEquals(new String[]{"1", "10", "20"}, flag.suggest(""));
    }
}
