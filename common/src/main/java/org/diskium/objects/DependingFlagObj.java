package org.diskium.objects;

public class DependingFlagObj <T> extends FlagObj {
    private final String dependingName;
    private final Object[] dependingValue;

    @SafeVarargs
    public DependingFlagObj(String name, String dependingName, Object[] dependingValue, T... options) {
        super(name, options);
        this.dependingName = dependingName;
        this.dependingValue = dependingValue;
    }
}
