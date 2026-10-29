package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.DuplicateHashGroup;
import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.model.LogFilter;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import com.itemtrackerpro.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class AntiDupeDuplicatesHolder implements ItpGuiHolder {

    private final GuiManager guiManager;
    private final Inventory inventory;
    private final List<DuplicateHashGroup> allGroups;
    private int page = 0;
    private static final int PER_PAGE = GuiConstants.CONTENT_SLOTS;

    public AntiDupeDuplicatesHolder(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_6_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Anti-Dupe: Duplicates"));

        ConfigManager cfg = guiManager.getPlugin().getConfigManager();
        long sinceTs = System.currentTimeMillis() - (cfg.getAntiDupeLookbackHours() * 3600_000L);
        this.allGroups = guiManager.getPlugin().getTrackedEventDAO()
                .findDuplicateHashes(sinceTs, cfg.getAntiDupeHashScanLimit());

        render();
    }

    private void render() {
        for (int i = 0; i < GuiConstants.CONTENT_SLOTS; i++) {
            inventory.setItem(i, null);
        }

        int start = page * PER_PAGE;
        int end = Math.min(allGroups.size(), start + PER_PAGE);

        for (int i = start; i < end; i++) {
            DuplicateHashGroup group = allGroups.get(i);
            Material icon;
            try {
                icon = Material.valueOf(group.material());
            } catch (IllegalArgumentException ex) {
                icon = Material.PAPER;
            }

            inventory.setItem(i - start, GuiItemBuilder.of(icon)
                    .name("&c&lDuplicate x" + group.count())
                    .lore("&7Material: &f" + group.material(),
                            "&7Action: &f" + group.actionType(),
                            "&7Player: &f" + group.playerName(),
                            "&7Hash: &f" + shorten(group.eventHash()),
                            "",
                            "&eClick to view matching events")
                    .build());
        }

        buildControlRow();
    }

    private String shorten(String hash) {
        return hash == null ? "N/A" : hash.substring(0, Math.min(12, hash.length())) + "...";
    }

    private void buildControlRow() {
        for (int i = GuiConstants.CONTENT_SLOTS; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        inventory.setItem(GuiConstants.SLOT_BACK, GuiItemBuilder.of(Material.ARROW).name("&e&lBack").build());

        int totalPages = Math.max(1, (int) Math.ceil(allGroups.size() / (double) PER_PAGE));
        if (page > 0) {
            inventory.setItem(GuiConstants.SLOT_PREV_PAGE, GuiItemBuilder.of(Material.ARROW)
                    .name("&e&lPrevious Page").build());
        }
        inventory.setItem(GuiConstants.SLOT_PAGE_INFO, GuiItemBuilder.of(Material.BOOK)
                .name("&f&lPage " + (page + 1) + " / " + totalPages).build());
        if (page < totalPages - 1) {
            inventory.setItem(GuiConstants.SLOT_NEXT_PAGE, GuiItemBuilder.of(Material.ARROW)
                    .name("&e&lNext Page").build());
        }

        inventory.setItem(GuiConstants.SLOT_HOME, GuiItemBuilder.of(Material.NETHER_STAR)
                .name("&d&lMain Menu").build());
        inventory.setItem(GuiConstants.SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&c&lClose").build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot < GuiConstants.CONTENT_SLOTS) {
            int index = page * PER_PAGE + slot;
            if (index < allGroups.size()) {
                DuplicateHashGroup group = allGroups.get(index);
                LogFilter filter = LogFilter.empty().withEventHash(group.eventHash());
                ConsoleLogsHolder holder = new ConsoleLogsHolder(guiManager, player, filter, "Duplicate Matches");
                player.openInventory(holder.getInventory());
            }
            return;
        }

        if (slot == GuiConstants.SLOT_BACK) {
            AntiDupeHolder holder = new AntiDupeHolder(guiManager);
            player.openInventory(holder.getInventory());
        } else if (slot == GuiConstants.SLOT_HOME) {
            guiManager.openMainMenu(player);
        } else if (slot == GuiConstants.SLOT_PREV_PAGE && page > 0) {
            page--;
            render();
        } else if (slot == GuiConstants.SLOT_NEXT_PAGE) {
            int totalPages = Math.max(1, (int) Math.ceil(allGroups.size() / (double) PER_PAGE));
            if (page < totalPages - 1) {
                page++;
                render();
            }
        } else if (slot == GuiConstants.SLOT_CLOSE) {
            player.closeInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}