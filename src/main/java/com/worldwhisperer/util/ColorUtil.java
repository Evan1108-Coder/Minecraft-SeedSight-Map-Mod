package com.worldwhisperer.util;

public final class ColorUtil {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFFAAAAAA;
    public static final int DARK_GRAY = 0xFF555555;
    public static final int YELLOW = 0xFFFFFF55;
    public static final int GREEN = 0xFF55FF55;
    public static final int RED = 0xFFFF5555;
    public static final int AQUA = 0xFF55FFFF;
    public static final int GOLD = 0xFFFFAA00;
    public static final int LIGHT_PURPLE = 0xFFFF55FF;

    public static final int MC_GREEN = 0xFF00AA00;
    public static final int MC_RED = 0xFFAA0000;
    public static final int MC_BLUE = 0xFF5555FF;
    public static final int MC_DARK_AQUA = 0xFF00AAAA;

    private ColorUtil() {}

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public static int lerp(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        return argb(
                (int) (a1 + (a2 - a1) * t),
                (int) (r1 + (r2 - r1) * t),
                (int) (g1 + (g2 - g1) * t),
                (int) (b1 + (b2 - b1) * t)
        );
    }

    public static int brighten(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return argb(a, r, g, b);
    }
}
