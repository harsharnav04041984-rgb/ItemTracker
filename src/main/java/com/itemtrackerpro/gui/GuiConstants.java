package com.itemtrackerpro.gui;

public final class GuiConstants {

    private GuiConstants() {
    }

    public static final int SIZE_3_ROWS = 27;
    public static final int SIZE_6_ROWS = 54;

    public static final String TITLE_PREFIX = "ItemTracker » ";

    // Standard 54-slot paginated layout: slots 0-44 are content (45 per
    // page), slots 45-53 are the fixed control row shared by every
    // paginated/listing GUI in the plugin.
    public static final int CONTENT_SLOTS = 45;

    public static final int SLOT_BACK = 45;
    public static final int SLOT_FILTER = 46;
    public static final int SLOT_PREV_PAGE = 48;
    public static final int SLOT_PAGE_INFO = 49;
    public static final int SLOT_NEXT_PAGE = 50;
    public static final int SLOT_HOME = 52;
    public static final int SLOT_CLOSE = 53;
}