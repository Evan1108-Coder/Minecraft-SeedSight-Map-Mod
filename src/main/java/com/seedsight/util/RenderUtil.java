package com.seedsight.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public final class RenderUtil {
    private static final int BORDER_OUTER = 0xFF000000;
    private static final int BORDER_HIGHLIGHT = 0xFFFFFFFF;
    private static final int BORDER_SHADOW = 0xFF373737;
    private static final int BORDER_INNER = 0xFF8B8B8B;
    private static final int BG_DARK = 0xD9181818;

    private RenderUtil() {}

    public static void drawMcBorder(DrawContext ctx, int x, int y, int w, int h) {
        // Outer black border
        ctx.fill(x, y, x + w, y + 1, BORDER_OUTER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
        ctx.fill(x, y, x + 1, y + h, BORDER_OUTER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

        // White highlight (top-left inner)
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_HIGHLIGHT);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_HIGHLIGHT);

        // Dark shadow (bottom-right inner)
        ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_SHADOW);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_SHADOW);

        // Gray fill (inside)
        ctx.fill(x + 2, y + 2, x + w - 2, y + h - 2, BORDER_INNER);
    }

    public static void drawMcPanel(DrawContext ctx, int x, int y, int w, int h) {
        drawMcPanel(ctx, x, y, w, h, BG_DARK);
    }

    public static void drawMcPanel(DrawContext ctx, int x, int y, int w, int h, int bgColor) {
        // Background fill
        ctx.fill(x + 2, y + 2, x + w - 2, y + h - 2, bgColor);

        // Outer black border
        ctx.fill(x, y, x + w, y + 1, BORDER_OUTER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
        ctx.fill(x, y, x + 1, y + h, BORDER_OUTER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

        // White top + left highlight
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_HIGHLIGHT);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_HIGHLIGHT);

        // Shadow bottom + right
        ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_SHADOW);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_SHADOW);
    }

    public static void drawMcButton(DrawContext ctx, int x, int y, int w, int h,
                                     boolean hovered, boolean pressed) {
        int bg;
        if (pressed) {
            bg = 0xFF555555;
        } else if (hovered) {
            bg = 0xFF8B8B8B;
        } else {
            bg = 0xFF6C6C6C;
        }

        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        ctx.fill(x, y, x + w, y + 1, BORDER_OUTER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
        ctx.fill(x, y, x + 1, y + h, BORDER_OUTER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

        if (!pressed) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_HIGHLIGHT);
            ctx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_HIGHLIGHT);
            ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_SHADOW);
            ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_SHADOW);
        }
    }

    public static void drawShadowedText(DrawContext ctx, TextRenderer font,
                                          String text, int x, int y, int color) {
        ctx.drawText(font, text, x + 1, y + 1, 0xFF3F3F3F, false);
        ctx.drawText(font, text, x, y, color, false);
    }

    public static void drawRightAlignedText(DrawContext ctx, TextRenderer font,
                                              String text, int x, int y, int color) {
        int w = font.getWidth(text);
        drawShadowedText(ctx, font, text, x - w, y, color);
    }

    public static void drawHorizontalDivider(DrawContext ctx, int x, int y, int w) {
        ctx.fill(x, y, x + w, y + 1, BORDER_SHADOW);
        ctx.fill(x, y + 1, x + w, y + 2, BORDER_HIGHLIGHT);
    }

    public static void drawVerticalDivider(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + 1, y + h, BORDER_SHADOW);
        ctx.fill(x + 1, y, x + 2, y + h, BORDER_HIGHLIGHT);
    }

    public static void playClickSound() {
        MinecraftClient.getInstance().getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
}
