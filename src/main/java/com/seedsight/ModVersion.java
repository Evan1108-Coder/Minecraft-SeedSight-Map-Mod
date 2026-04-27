package com.seedsight;

public final class ModVersion {
    public static final String MC_VERSION = "1.21.1";
    public static final int MC_MINOR = 1;

    public static boolean hasPaleGarden() {
        return MC_MINOR >= 4;
    }

    public static boolean hasCreaking() {
        return MC_MINOR >= 4;
    }

    private ModVersion() {}
}
