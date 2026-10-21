package org.diskium.objects;

import java.util.ArrayList;
import java.util.List;

public class FlagObj <T> {
    private final String name;
    private final T[] options;

    @SafeVarargs
    public FlagObj(String name, T... options) {
        this.name = name;
        this.options = options;
    }

    public String[] suggest(String typed) {
        List<String> suggestions = new ArrayList<>();

        for (T suggestion : this.options) {
            if (suggestion.toString().startsWith(typed)) {
                suggestions.add(suggestion.toString());
            }
        }

        return suggestions.toArray(String[]::new);
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getValueClass() {
        return options.getClass().getComponentType();
    }
}
