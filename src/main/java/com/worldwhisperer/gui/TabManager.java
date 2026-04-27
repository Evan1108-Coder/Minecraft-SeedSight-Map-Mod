package com.worldwhisperer.gui;

public class TabManager {

    public enum Tab {
        MINIMAP("MiniMap", TabType.PARTIAL_TOP),
        STATS("Statistics", TabType.PARTIAL_BOTTOM),
        PERF_STATS("Perf Stats", TabType.PARTIAL_BOTTOM),
        WAYPOINT("Waypoints", TabType.FULL),
        CALCULATOR("Calculator", TabType.FULL),
        SETTINGS("Settings", TabType.FULL);

        public final String label;
        public final TabType type;

        Tab(String label, TabType type) {
            this.label = label;
            this.type = type;
        }
    }

    public enum TabType {
        PARTIAL_TOP,
        PARTIAL_BOTTOM,
        FULL
    }

    private Tab activeTopTab = Tab.MINIMAP;
    private Tab activeBottomTab = Tab.STATS;
    private Tab activeFullTab = null;
    private boolean inFullView = false;

    private Tab rememberedTop = Tab.MINIMAP;
    private Tab rememberedBottom = Tab.STATS;

    public void selectTab(Tab tab) {
        switch (tab.type) {
            case PARTIAL_TOP -> {
                if (inFullView) {
                    inFullView = false;
                    activeTopTab = tab;
                    activeBottomTab = rememberedBottom;
                } else {
                    activeTopTab = tab;
                }
                rememberedTop = activeTopTab;
            }
            case PARTIAL_BOTTOM -> {
                if (inFullView) {
                    inFullView = false;
                    activeTopTab = rememberedTop;
                    activeBottomTab = tab;
                } else {
                    activeBottomTab = tab;
                }
                rememberedBottom = activeBottomTab;
            }
            case FULL -> {
                if (!inFullView) {
                    rememberedTop = activeTopTab;
                    rememberedBottom = activeBottomTab;
                }
                activeFullTab = tab;
                inFullView = true;
            }
        }
    }

    public void cycleTab() {
        Tab[] tabs = Tab.values();
        Tab current = getCurrentPrimaryTab();
        int idx = current.ordinal();
        Tab next = tabs[(idx + 1) % tabs.length];
        selectTab(next);
    }

    public Tab getCurrentPrimaryTab() {
        if (inFullView) return activeFullTab;
        return activeTopTab;
    }

    public Tab getActiveTopTab() { return activeTopTab; }
    public Tab getActiveBottomTab() { return activeBottomTab; }
    public Tab getActiveFullTab() { return activeFullTab; }
    public boolean isInFullView() { return inFullView; }

    public boolean isTabActive(Tab tab) {
        if (inFullView) return tab == activeFullTab;
        return tab == activeTopTab || tab == activeBottomTab;
    }
}
