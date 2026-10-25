package org.diskium.objects;

import java.util.ArrayList;
import java.util.List;

public class FlagObj <T> {
    private final String name;
    private final T[] options;

/**
     * Creates new flag.
     *
     * @param name Name of the flag.
     * @param options Values of the flag.
     */
    @SafeVarargs
    public FlagObj(String name, T... options) {
        this.name = name;
        this.options = options;
    }

/**
     * Creates a suggestion from already typed argument.
     *
     * @param typed Typed argument to be completed.
     * @return Array of suggestions.
     */
    public String[] suggest(String typed) {
        List<String> suggestions = new ArrayList<>();

        for (T suggestion : this.options) {
            if (suggestion.toString().toLowerCase().startsWith(typed.toLowerCase())) {
                suggestions.add(suggestion.toString());
            }
        }

        return suggestions.toArray(String[]::new);
    }

    public String getName() {
        return this.name;
    }

    public T[] getOptions() {
        return options;
    }

/**
     * Gets class of flag's values.
     *
     * @return Class of flag's values.
     */
    public Class<?> getValueClass() {
        return options.getClass().getComponentType();
    }
}
