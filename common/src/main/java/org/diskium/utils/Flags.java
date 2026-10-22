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

    @SafeVarargs
    public Flags(FlagObj... base) {
        this.base = base;
        this.baseFlags = Arrays.stream(base).map(FlagObj::getName).toArray(String[]::new);
    }

    public String[] suggest(String typed) {
        this.now = typed;
        parseNames();

        boolean isFlag = typed.lastIndexOf("--") > typed.lastIndexOf('=');

        if (isFlag) {
            String prefix = typed.substring(typed.lastIndexOf("--") + 2).trim().toLowerCase();
            boolean entered = Arrays.stream(this.base).anyMatch(flag -> flag.getName().equalsIgnoreCase(prefix));

            return Arrays.stream(this.base)
                    .filter(flag -> Arrays.stream(this.parsedFlags)
                            .map(value -> value.split("=", 2)[0])
                            .noneMatch(name -> name.equalsIgnoreCase(flag.getName())))
                    .filter(flag -> entered || flag.getName().toLowerCase(Locale.ROOT).startsWith(prefix))
                    .filter(flag -> {
                        if (flag instanceof DependingFlagObj dependingFlag) {
                            return dependingFlag.isFlagDepending(getAll());
                        }

                        return true;
                    } )
                    .map(flag -> "--" + flag.getName())
                    .toArray(String[]::new);
        }

        String current = typed.substring(typed.lastIndexOf("--") + 2);
        String[] parts = current.split("=", 2);
        this.flagNow = getFlagByName(parts[0]);
        return flagNow.suggest(parts.length == 2 ? parts[1] : "");
    }

    public Map<String, Object> getAll() {
        parseNames();

        this.parsed = Arrays.stream(this.parsedFlags)
                .map(flag -> flag.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts.length == 1 ? Boolean.TRUE : parseValue(parts[0], parts[1])
                ));

        return this.parsed;
    }

    private void parseNames() {
        this.parsedFlags = Arrays.stream(now.replace(" ", "").split("--"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

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

        return null;
    }

    private FlagObj getFlagByName(String name) {
        return Arrays.stream(this.base).filter(flag -> flag.getName().equalsIgnoreCase(name))
                .toList()
                .getFirst();
    }
}
