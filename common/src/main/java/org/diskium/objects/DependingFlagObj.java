package org.diskium.objects;

import java.util.Map;
import java.util.Objects;

public class DependingFlagObj <T> extends FlagObj<T> {
    private final String dependingName;
    private final Object[] dependingValue;

/**
     * Creates new depending flag
     *
     * @param name Name of the flag.
     * @param dependingName Name of the flag it depends on.
     * @param dependingValue Values it depends on.
     * @param options Values the flag has.
     */
    @SafeVarargs
    public DependingFlagObj(String name, String dependingName, Object[] dependingValue, T... options) {
        super(name, options);
        this.dependingName = dependingName;
        this.dependingValue = dependingValue;
    }

    /**
     * Checks whether the required flag is present and matches an allowed value.
     *
     * @param map parsed flag names and values
     * @return {@code true} if the dependency is satisfied
     */
    public boolean isFlagDepending(Map<String, Object> map) {
        Object value = map.get(this.dependingName);
        if (value == null) {
            return false;
        }

        for (Object allowed : this.dependingValue) {
            if (Objects.equals(value, allowed)) {
                return true;
            }
        }

        return false;
    }
}
