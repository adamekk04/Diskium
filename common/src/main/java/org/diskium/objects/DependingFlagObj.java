package org.diskium.objects;

import java.util.Map;
import java.util.Objects;

public class DependingFlagObj <T> extends FlagObj<T> {
    private final String dependingName;
    private final Object[] dependingValue;

    @SafeVarargs
    public DependingFlagObj(String name, String dependingName, Object[] dependingValue, T... options) {
        super(name, options);
        this.dependingName = dependingName;
        this.dependingValue = dependingValue;
    }

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
