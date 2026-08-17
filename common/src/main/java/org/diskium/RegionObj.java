package org.diskium;

public class RegionObj {
    int x;
    int z;

    public RegionObj(int[] coords) { // TODO: Don't use RegionObj and use lists
        this.x = coords[0];
        this.z = coords[1];
    }

    public RegionObj(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
