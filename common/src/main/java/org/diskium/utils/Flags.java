package org.diskium.utils;

import org.diskium.MultiplatformLogger;
import org.diskium.objects.DependingFlagObj;
import org.diskium.objects.FlagObj;

import java.util.*;
import java.util.stream.Collectors;

public class Flags {
    private final FlagObj[] base;
    private final String[] baseFlags;

    private String now;
    private FlagObj flagNow;
    private Map<String, Object> parsed;
    private String[] parsedFlags;

/**
     * Creates flags from {@code FlagObj}
     *
     * @param base Varargs of {@code FlagObj}
     */
    @SafeVarargs
    public Flags(FlagObj... base) {
        this.base = base;
        this.baseFlags = Arrays.stream(base).map(FlagObj::getName).toArray(String[]::new);
    }

    /**
     * Suggests unused flag names or values matching the current input.
     * Prefix matching ignores case. Stores the input for subsequent calls to {@link #getAll()}.
     *
     * @param typed the current command argument containing flag expressions
     * @return matching flag names or value suggestions
     */
    public String[] suggest(String typed) {
        this.now = typed;
        parseNames();

        int flagStart = typed.lastIndexOf("--");
        boolean completedOption = false;

        if (flagStart >= 0 && typed.endsWith(" ")) {
            String[] parts = typed.substring(flagStart + 2).stripTrailing().split("=", 2);

            if (parts.length == 2) {
                completedOption = getFlagByName(parts[0])
                        .map(flag -> Arrays.stream(flag.getOptions())
                                .anyMatch(option -> option.toString().equalsIgnoreCase(parts[1])))
                        .orElse(false);
            }
        }

        boolean isFlag = typed.isBlank() || flagStart > typed.lastIndexOf('=') || completedOption;

        if (isFlag) {
            String prefix = completedOption || flagStart < 0 ? "" : typed.substring(flagStart + 2).strip().toLowerCase();
            boolean entered = Arrays.stream(this.base).anyMatch(flag -> flag.getName().equalsIgnoreCase(prefix));

            return Arrays.stream(this.base)
                    .filter(flag -> Arrays.stream(this.parsedFlags)
                            .map(value -> value.split("=", 2)[0])
                            .noneMatch(name -> name.equalsIgnoreCase(flag.getName())))
                    .filter(flag -> entered || flag.getName().toLowerCase().startsWith(prefix))
                    .filter(flag -> {
                        if (flag instanceof DependingFlagObj dependingFlag) {
                            return dependingFlag.isFlagDepending(getAll());
                        }

                        return true;
                    } )
                    .map(flag -> {
                        Class<?> type = flag.getValueClass();
                        boolean isBoolean = type == boolean.class || type == Boolean.class;

                        return "--" + flag.getName() + (isBoolean ? "" : "=");
                    })
                    .toArray(String[]::new);
        }

        String current = typed.substring(typed.lastIndexOf("--") + 2);
        String[] parts = current.split("=", 2);
        this.flagNow = getFlagByName(parts[0]).get();
        return flagNow.suggest(parts.length == 2 ? parts[1] : "");
    }

/**
     * Parses the input stored by the most recent call to {@link #suggest(String)}.
     * Unknown flags are ignored, flags without values become {@code true},
     * and the last value wins for repeated flags.
     *
     * @return values keyed by the registered flag names
     * @throws IllegalArgumentException if a value cannot be converted to its declared type
     */
    public Map<String, Object> getAll() {
        parseNames();

        this.parsed = Arrays.stream(this.parsedFlags)
                .map(flag -> flag.split("=", 2))
                .filter(parts -> getFlagByName(parts[0]).isPresent())
                .collect(Collectors.toMap(
                        parts -> getFlagByName(parts[0]).orElseThrow().getName(),
                        parts -> parts.length == 1 ? Boolean.TRUE : parseValue(parts[0], parts[1]),
                        (previous, latest) -> latest
                ));

        return this.parsed;
    }

    /**
     * Splits the current input on {@code --}, trims each part,
     * and stores the non-empty flag expressions.
     */
    private void parseNames() {
        this.parsedFlags = Arrays.stream(now.split("--"))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

/**
     * Converts a value to the registered flag's declared type.
     *
     * @param name the registered flag name, matched without regard to case
     * @param value the text to convert
     * @return the converted string, integer, or boolean
     * @throws IllegalArgumentException if the value is invalid or the type is unsupported
     */
    private Object parseValue(String name, String value) {
        Class<?> type = Arrays.stream(this.base).filter(flag -> flag.getName().equalsIgnoreCase(name))
                .toList()
                .getFirst()
                .getValueClass();

        try {
            if (type == String.class) {
                return value;
            }
            if (type == Integer.class || type == int.class) {
                return Integer.parseInt(value);
            }
            if (type == Boolean.class || type == boolean.class) {
                if (value.equalsIgnoreCase("true")) return true;
                if (value.equalsIgnoreCase("false")) return false;

                MultiplatformLogger.error("Couldn't parse boolean: " + value);
            }
        } catch (NumberFormatException e) {
            MultiplatformLogger.error("Couldn't parse integer: " + value);
        }

        throw new IllegalArgumentException(
                "Invalid value '" + value + "' for flag --" + name
        );
    }

/**
     * Gets flag by its name.
     *
     * @param name Name of the flag.
     * @return Return {@code Optional<FlagObj>} of the flag.
     */
    private Optional<FlagObj> getFlagByName(String name) {
        return Arrays.stream(this.base)
                .filter(flag -> flag.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
