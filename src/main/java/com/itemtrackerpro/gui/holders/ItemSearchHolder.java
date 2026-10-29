package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.ActionBreakdown;
import com.itemtrackerpro.database.TrackedEventDAO;
import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.model.LogFilter;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class ItemSearchHolder implements ItpGuiHolder {

    private static final int SLOT_SEARCH = 13;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_CLOSE = 26;

    private final GuiManager guiManager;
    private final Inventory inventory;

    public ItemSearchHolder(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Search by Item"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        inventory.setItem(SLOT_SEARCH, GuiItemBuilder.of(Material.COMPASS)
                .name("&6&lSearch Item")
                .lore("&7Enter a material name to see",
                        "&7how, when, where, and by whom",
                        "&7it was obtained, transferred,",
                        "&7or stored.",
                        "",
                        "&eClick to search")
                .build());

        inventory.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW).name("&e&lBack to Main Menu").build());
        inventory.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER).name("&c&lClose").build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == SLOT_SEARCH) {
            promptSearch(player);
        } else if (slot == SLOT_BACK) {
            guiManager.openMainMenu(player);
        } else if (slot == SLOT_CLOSE) {
            player.closeInventory();
        }
    }

    private void promptSearch(Player player) {
        player.closeInventory();
        player.sendMessage("§e[ItemTracker] §fEnter a material name (e.g. DIAMOND, OAK_LOG).");

        guiManager.getPlugin().getChatInputManager().awaitInput(player, input -> {
            if (input == null) {
                player.openInventory(this.inventory);
                return;
            }
            resolveAndOpen(player, input.trim());
        });
    }

    private void resolveAndOpen(Player player, String input) {
        Material material;
        try {
            material = Material.valueOf(input.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            player.sendMessage("§cUnknown material: " + input);
            player.openInventory(this.inventory);
            return;
        }

        TrackedEventDAO dao = guiManager.getPlugin().getTrackedEventDAO();
        int total = dao.countByMaterialExact(material.name());

        if (total == 0) {
            player.sendMessage("§cNo recorded events found for " + material.name() + ".");
            player.openInventory(this.inventory);
            return;
        }

        List<ActionBreakdown> breakdown = dao.getActionBreakdownForMaterial(material.name());
        player.sendMessage("§6[ItemTracker] §fRecorded history for §f" + material.name() +
                " §7(" + total + " total events):");
        for (ActionBreakdown entry : breakdown) {
            player.sendMessage("§7 - " + entry.actionType() + ": §f" + entry.count());
        }

        LogFilter filter = LogFilter.empty().withMaterialExact(material.name());
        ConsoleLogsHolder holder = new ConsoleLogsHolder(guiManager, player, filter, "Item: " + material.name());
        player.openInventory(holder.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}