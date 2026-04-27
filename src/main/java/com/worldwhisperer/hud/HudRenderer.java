package com.worldwhisperer.hud;

import com.worldwhisperer.WorldWhispererClient;
import com.worldwhisperer.config.WorldWhispererConfig;
import com.worldwhisperer.gui.TabManager;
import com.worldwhisperer.gui.TabManager.Tab;
import com.worldwhisperer.util.ColorUtil;
import com.worldwhisperer.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HudRenderer {
    private final WorldWhispererClient mod;
    private final MinimapRenderer minimapRenderer;
    private final PanelRenderer panelRenderer;
    private boolean visible = true;

    private static final int TAB_W = 24;
    private static final int TAB_H = 20;
    private static final int TAB_GAP = 2;
    private static final int BORDER = 3;

    public HudRenderer(WorldWhispererClient mod) {
        this.mod = mod;
        this.minimapRenderer = new MinimapRenderer(mod);
        this.panelRenderer = new PanelRenderer(mod);
        this.visible = mod.getConfig().hudVisible;
    }

    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!visible) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        if (client.options.hudHidden) return;

        WorldWhispererConfig cfg = mod.getConfig();
        TextRenderer font = client.textRenderer;

        int screenW = client.getWindow().getScaledWidth();
        int mapSize = cfg.mapSize;
        int panelH = cfg.panelHeight;
        int contentW = mapSize;
        int contentH;

        TabManager tm = mod.getTabManager();
        boolean fullView = tm.isInFullView();

        if (fullView) {
            contentH = mapSize + panelH;
        } else {
            contentH = mapSize + BORDER + panelH;
        }

        int totalW = TAB_W + BORDER + contentW + BORDER * 2;
        int totalH = contentH + BORDER * 2;

        int baseX = cfg.hudX >= 0 ? cfg.hudX : screenW - totalW - 5;
        int baseY = cfg.hudY;

        float alpha = cfg.hudOpacity;

        // Draw outer container border
        RenderUtil.drawMcPanel(ctx, baseX, baseY, totalW, totalH,
                ColorUtil.withAlpha(0x181818, (int) (alpha * 255)));

        // Tab bar area (left side inside border)
        int tabX = baseX + BORDER;
        int tabY = baseY + BORDER;
        drawTabBar(ctx, font, tabX, tabY, totalH - BORDER * 2);

        // Content area
        int contentX = tabX + TAB_W + BORDER;
        int contentY = baseY + BORDER;

        if (fullView) {
            drawFullTab(ctx, font, contentX, contentY, contentW, contentH, tm.getActiveFullTab());
        } else {
            // Top section: map
            drawTopSection(ctx, font, contentX, contentY, contentW, mapSize, tm.getActiveTopTab());

            // Divider
            int divY = contentY + mapSize;
            RenderUtil.drawHorizontalDivider(ctx, contentX, divY, contentW);

            // Bottom section: stats/perf
            int bottomY = divY + BORDER;
            int bottomH = panelH;
            drawBottomSection(ctx, font, contentX, bottomY, contentW, bottomH, tm.getActiveBottomTab());
        }
    }

    private void drawTabBar(DrawContext ctx, TextRenderer font, int x, int y, int height) {
        TabManager tm = mod.getTabManager();
        Tab[] tabs = Tab.values();

        for (int i = 0; i < tabs.length; i++) {
            int tabY = y + i * (TAB_H + TAB_GAP);
            boolean active = tm.isTabActive(tabs[i]);
            boolean hovered = false;

            int bg = active ? 0xFF4A4A4A : 0xFF2A2A2A;
            ctx.fill(x, tabY, x + TAB_W, tabY + TAB_H, bg);

            if (active) {
                ctx.fill(x, tabY, x + 2, tabY + TAB_H, 0xFF55FF55);
            }

            // Tab icon as first letter
            String icon = tabs[i].label.substring(0, 1);
            int textX = x + (TAB_W - font.getWidth(icon)) / 2;
            int textY = tabY + (TAB_H - 8) / 2;
            int textColor = active ? ColorUtil.WHITE : ColorUtil.GRAY;
            ctx.drawText(font, icon, textX, textY, textColor, true);
        }
    }

    private void drawTopSection(DrawContext ctx, TextRenderer font,
                                 int x, int y, int w, int h, Tab tab) {
        if (tab == Tab.MINIMAP) {
            minimapRenderer.render(ctx, x, y, w, h);
        }
    }

    private void drawBottomSection(DrawContext ctx, TextRenderer font,
                                    int x, int y, int w, int h, Tab tab) {
        switch (tab) {
            case STATS -> panelRenderer.renderStats(ctx, font, x, y, w, h);
            case PERF_STATS -> panelRenderer.renderPerfStats(ctx, font, x, y, w, h);
            default -> {}
        }
    }

    private void drawFullTab(DrawContext ctx, TextRenderer font,
                              int x, int y, int w, int h, Tab tab) {
        switch (tab) {
            case WAYPOINT -> panelRenderer.renderWaypoints(ctx, font, x, y, w, h);
            case CALCULATOR -> panelRenderer.renderCalculator(ctx, font, x, y, w, h);
            case SETTINGS -> panelRenderer.renderSettings(ctx, font, x, y, w, h);
            default -> {}
        }
    }

    public void toggleVisible() {
        visible = !visible;
        mod.getConfig().hudVisible = visible;
        mod.getConfig().save();
    }

    public void toggleExpanded() {
        // Could implement fullscreen map mode here
    }

    public boolean isVisible() { return visible; }

    public int getTabIndexAt(double mouseX, double mouseY, int baseX, int baseY) {
        int tabX = baseX + BORDER;
        int tabY = baseY + BORDER;
        Tab[] tabs = Tab.values();

        for (int i = 0; i < tabs.length; i++) {
            int ty = tabY + i * (TAB_H + TAB_GAP);
            if (mouseX >= tabX && mouseX < tabX + TAB_W
                    && mouseY >= ty && mouseY < ty + TAB_H) {
                return i;
            }
        }
        return -1;
    }
}
