package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainMenuHolder implements ItpGuiHolder {

    private static final int SLOT_CONSOLE_LOGS = 11;
    private static final int SLOT_ANTI_DUPE = 12;
    private static final int SLOT_PLAYER_TRACKER = 13;
    private static final int SLOT_ITEM_SEARCH = 14;
    private static final int SLOT_HAND_ITEM = 15;
    private static final int SLOT_CLOSE = 22;

    private final GuiManager guiManager;
    private final Inventory inventory;

    public MainMenuHolder(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Main Menu"));
        build();
    }

    private void build() {
        ItemStack filler = GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(SLOT_CONSOLE_LOGS, GuiItemBuilder.of(Material.WRITABLE_BOOK)
                .name("&b&lConsole / Live Activity Logs")
                .lore("&7View a live, searchable feed of",
                        "&7every tracked event on the server.",
                        "",
                        "&eClick to open")
                .build());

        inventory.setItem(SLOT_ANTI_DUPE, GuiItemBuilder.of(Material.REDSTONE)
                .name("&c&lAnti-Dupe Detection")
                .lore("&7Scans recorded events for",
                        "&7suspicious duplicate or spike patterns.",
                        "",
                        "&eClick to open")
                .build());

        inventory.setItem(SLOT_PLAYER_TRACKER, GuiItemBuilder.of(Material.PLAYER_HEAD)
                .name("&a&lPlayer Tracker")
                .lore("&7Search a player by name or UUID",
                        "&7and view their recorded activity.",
                        "",
                        "&eClick to open")
                .build());

        inventory.setItem(SLOT_ITEM_SEARCH, GuiItemBuilder.of(Material.COMPASS)
                .name("&6&lSearch by Item")
                .lore("&7Trace how, when, where, and",
                        "&7by whom an item was obtained.",
                        "",
                        "&eClick to open")
                .build());

        inventory.setItem(SLOT_HAND_ITEM, GuiItemBuilder.of(Material.ITEM_FRAME)
                .name("&d&lCheck Hand Item")
                .lore("&7Inspect the item currently",
                        "&7in your main hand.",
                        "",
                        "&eClick to open")
                .build());

        inventory.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&c&lClose")
                .build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        switch (slot) {
            case SLOT_CONSOLE_LOGS -> guiManager.openConsoleLogs(player);
            case SLOT_ANTI_DUPE -> guiManager.openAntiDupe(player);
            case SLOT_PLAYER_TRACKER -> guiManager.openPlayerTracker(player);
            case SLOT_ITEM_SEARCH -> guiManager.openItemSearch(player);
            case SLOT_HAND_ITEM -> guiManager.openHandItemMenu(player);
            case SLOT_CLOSE -> player.closeInventory();
            default -> {
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}