package org.diskium.utils;

import org.diskium.objects.FlagObj;
import org.diskium.objects.DependingFlagObj;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlagsTest {

    @Test
    void getAllRejectsInvalidValuesAndIntegerOverflow() {
        assertAll(
                () -> {
                    Flags flags = new Flags(new FlagObj<Integer>("limit"));
                    flags.suggest("--limit=abc");
                    assertThrows(IllegalArgumentException.class, flags::getAll);
                },
                () -> {
                    Flags flags = new Flags(new FlagObj<Integer>("limit"));
                    flags.suggest("--limit=2147483648");
                    assertThrows(IllegalArgumentException.class, flags::getAll);
                },
                () -> {
                    Flags flags = new Flags(new FlagObj<Integer>("limit"));
                    flags.suggest("--limit=-2147483649");
                    assertThrows(IllegalArgumentException.class, flags::getAll);
                },
                () -> {
                    Flags flags = new Flags(new FlagObj<Boolean>("verbose"));
                    flags.suggest("--verbose=yes");
                    assertThrows(IllegalArgumentException.class, flags::getAll);
                }
        );
    }

    @Test
    void suggestDoesNotParsePartialDependentFlagAsCompletedFlag() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "safe"),
                new DependingFlagObj<String>(
                        "output", "mode", new Object[]{"fast"}, "console", "file"
                )
        );

        String[] suggestions = flags.suggest("--mode=fast --out");

        assertAll(
                () -> assertArrayEquals(new String[]{"--output"}, suggestions),
                () -> assertEquals(Map.of("mode", "fast"), flags.getAll())
        );
    }

    @Test
    void getAllUsesLastValueForDuplicateFlags() {
        Flags flags = new Flags(new FlagObj<Integer>("limit"));
        flags.suggest("--limit=1 --limit=2");

        assertEquals(Map.of("limit", 2), flags.getAll());
    }

    @Test
    void suggestResolvesDependencyNameIgnoringCase() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "safe"),
                new DependingFlagObj<String>(
                        "output", "mode", new Object[]{"fast"}, "console", "file"
                )
        );

        assertArrayEquals(new String[]{"--output"}, flags.suggest("--MODE=fast --"));
    }

    @Test
    void suggestListsAvailableFlagsForEmptyInput() {
        Flags flags = new Flags(
                new FlagObj<Boolean>("verbose"),
                new FlagObj<String>("mode", "fast", "safe"),
                new DependingFlagObj<String>(
                        "output", "mode", new Object[]{"fast"}, "console", "file"
                )
        );

        assertArrayEquals(new String[]{"--verbose", "--mode"}, flags.suggest(""));
    }

    @Test
    void getAllPreservesSpacesInsideStringValue() {
        Flags flags = new Flags(
                new FlagObj<String>("name"),
                new FlagObj<Boolean>("verbose")
        );
        flags.suggest("--name=My World --verbose");

        assertEquals(Map.of("name", "My World", "verbose", true), flags.getAll());
    }

    @Test
    void suggestReturnsEmptyWhenAllFlagsAreUsed() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "full"),
                new FlagObj<Boolean>("verbose")
        );

        assertArrayEquals(
                new String[]{},
                flags.suggest("--mode=fast --verbose --")
        );
    }

    @Test
    void getAllParsesIntegerBoundaries() {
        Flags flags = new Flags(new FlagObj<Integer>("limit"));

        flags.suggest("--limit=2147483647");
        assertEquals(Map.of("limit", Integer.MAX_VALUE), flags.getAll());

        flags.suggest("--limit=-2147483648");
        assertEquals(Map.of("limit", Integer.MIN_VALUE), flags.getAll());
    }

    @Test
    void suggestReturnsEmptyForUnknownFlagPrefix() {
        Flags flags = new Flags(
                new FlagObj<Boolean>("verbose"),
                new FlagObj<String>("mode", "fast", "full")
        );

        assertArrayEquals(new String[]{}, flags.suggest("--unknown"));
    }

    @Test
    void suggestUsesOptionsOfLastFlag() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "full"),
                new FlagObj<String>("output", "console", "file")
        );

        assertArrayEquals(
                new String[]{"file"},
                flags.suggest("--mode=fast --output=FI")
        );
    }

    @Test
    void suggestOffersDependentFlagForEnabledSwitch() {
        Flags flags = new Flags(
                new FlagObj<Boolean>("verbose"),
                new DependingFlagObj<String>(
                        "output", "verbose", new Object[]{true},
                        "console", "file"
                )
        );

        assertArrayEquals(
                new String[]{"--output"},
                flags.suggest("--verbose --")
        );
        assertArrayEquals(
                new String[]{},
                flags.suggest("--verbose=false --")
        );
    }

    @Test
    void suggestOffersDependentFlagForParsedInteger() {
        Flags flags = new Flags(
                new FlagObj<Integer>("level"),
                new DependingFlagObj<String>(
                        "output", "level", new Object[]{2},
                        "console", "file"
                )
        );

        assertArrayEquals(
                new String[]{"--output"},
                flags.suggest("--level=2 --")
        );
        assertArrayEquals(
                new String[]{},
                flags.suggest("--level=3 --")
        );
    }

    @Test
    void getAllReflectsOnlyLatestInput() {
        Flags flags = new Flags(
                new FlagObj<String>("mode"),
                new FlagObj<Boolean>("verbose")
        );

        flags.suggest("--mode=fast --verbose");
        assertEquals(Map.of("mode", "fast", "verbose", true), flags.getAll());

        flags.suggest("--mode=safe");
        assertEquals(Map.of("mode", "safe"), flags.getAll());
    }

    @Test
    void getAllPreservesEmptyStringValue() {
        Flags flags = new Flags(new FlagObj<String>("name"));

        flags.suggest("--name=");

        assertEquals(Map.of("name", ""), flags.getAll());
    }

    @Test
    void getAllParsesBooleanValuesIgnoringCase() {
        Flags flags = new Flags(new FlagObj<Boolean>("verbose"));

        flags.suggest("--verbose=TRUE");
        assertEquals(Map.of("verbose", true), flags.getAll());

        flags.suggest("--verbose=FaLsE");
        assertEquals(Map.of("verbose", false), flags.getAll());
    }

    @Test
    void getAllPreservesEqualsInsideStringValue() {
        Flags flags = new Flags(new FlagObj<String>("filter"));

        flags.suggest("--filter=name=value");

        assertEquals(Map.of("filter", "name=value"), flags.getAll());
    }

    @Test
    void getAllParsesDeclaredValueTypes() {
        Flags flags = new Flags(
                new FlagObj<String>("mode"),
                new FlagObj<Integer>("limit"),
                new FlagObj<Boolean>("verbose")
        );

        flags.suggest("--mode=fast --limit=10 --verbose=false");

        assertEquals(
                Map.of("mode", "fast", "limit", 10, "verbose", false),
                flags.getAll()
        );
    }

    @Test
    void suggestExcludesAlreadyEnteredDependentFlag() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "safe"),
                new DependingFlagObj<String>(
                        "output", "mode", new Object[]{"fast"},
                        "console", "file"
                ),
                new FlagObj<Boolean>("verbose")
        );

        assertArrayEquals(
                new String[]{"--verbose"},
                flags.suggest("--mode=fast --output=console --")
        );
    }

    @Test
    void suggestOffersDependentFlagOnlyForAllowedValue() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "safe"),
                new DependingFlagObj<String>(
                        "output", "mode", new Object[]{"fast"},
                        "console", "file"
                )
        );

        assertArrayEquals(new String[]{"--output"}, flags.suggest("--mode=fast --"));
        assertArrayEquals(new String[]{}, flags.suggest("--mode=safe --"));
        assertArrayEquals(new String[]{"--mode"}, flags.suggest("--"));
    }

    @Test
    void suggestMatchesFlagNamesIgnoringCase() {
        Flags flags = new Flags(
                new FlagObj<Boolean>("verbose"),
                new FlagObj<String>("mode", "fast", "full")
        );

        assertArrayEquals(new String[]{"--verbose"}, flags.suggest("--VER"));
        assertArrayEquals(new String[]{"--mode"}, flags.suggest("--Mo"));
    }

    @Test
    void suggestListsAllValuesAfterEquals() {
        Flags flags = new Flags(
                new FlagObj<String>("mode", "fast", "full", "safe")
        );

        assertArrayEquals(
                new String[]{"fast", "full", "safe"},
                flags.suggest("--mode=")
        );
    }

    @Test
    void suggestListsFlagsWhenOnlyPrefixIsEntered() {
        Flags flags = new Flags(
                new FlagObj<Boolean>("verbose"),
                new FlagObj<String>("mode", "fast", "full")
        );

        assertArrayEquals(
                new String[]{"--verbose", "--mode"},
                flags.suggest("--")
        );
    }
}
