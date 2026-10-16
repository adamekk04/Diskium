package org.diskium.utils;

import org.diskium.MultiplatformLogger;

import java.util.*;
import java.util.stream.Collectors;

public class Flags {
    private final Map<String, Class<?>> base;
    private final String[] baseFlags;
    private String now;
    private Map<String, Object> parsed;
    private String[] parsedFlags;

    public Flags(Map<String, Class<?>> base) {
        this.base = base;
        List<String> baseFlags = new ArrayList<>();

        for (Map.Entry<String, Class<?>> entry : base.entrySet()) {
            baseFlags.add(entry.getKey());
        }

        this.baseFlags = baseFlags.toArray(String[]::new);
    }

    public String[] suggest(String typed) {
        this.now = typed;
        parseNames();

        return Arrays.stream(this.baseFlags)
                .filter(item -> !Arrays.stream(this.parsedFlags).toList().contains(item))
                .toArray(String[]::new);
    }

    public Map<String, Object> getAll() {
        parseAll();

        return this.parsed;
    }

    private void parseAll() {
        parseNames();

        this.parsed = Arrays.stream(this.parsedFlags)
                .map(flag -> flag.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts.length == 1 ? Boolean.TRUE : parseValue(parts[0], parts[1])
                ));
    }

    private void parseNames() {
        this.parsedFlags = Arrays.stream(now.replace(" ", "").split("--"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private Object parseValue(String name, String value) {
        Class<?> type = this.base.get(name);
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

    private boolean canParse(Object object, Class<?> type) {
        if (type == String.class) {
            return true;
        }
        if (type == Integer.class) {
            try {
                Integer.parseInt(object.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (type == Boolean.class || type == boolean.class) {
            return (object.toString().equalsIgnoreCase("true") || object.toString().equalsIgnoreCase("false"));
        }

        return false;
    }
}
