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
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CheckHandItemHolder implements ItpGuiHolder {

    private static final int SLOT_INFO = 13;
    private static final int SLOT_TRACK_MINE = 11;
    private static final int SLOT_TRACK_GLOBAL = 15;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_CLOSE = 26;

    private final GuiManager guiManager;
    private final Inventory inventory;
    private final ItemStack snapshot;

    public CheckHandItemHolder(GuiManager guiManager, Player player) {
        this.guiManager = guiManager;
        ItemStack hand = player.getInventory().getItemInMainHand();
        this.snapshot = (hand == null || hand.getType().isAir()) ? null : hand.clone();
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Check Hand Item"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        if (snapshot == null) {
            inventory.setItem(SLOT_INFO, GuiItemBuilder.of(Material.BARRIER)
                    .name("&c&lNo Item in Hand")
                    .lore("&7You are not holding any item.",
                            "&7Hold an item and reopen this menu.")
                    .build());
        } else {
            inventory.setItem(SLOT_INFO, GuiItemBuilder.of(snapshot)
                    .name("&d&l" + snapshot.getType().name())
                    .lore("&7Amount: &f" + snapshot.getAmount(),
                            "&7Material: &f" + snapshot.getType().name())
                    .build());

            inventory.setItem(SLOT_TRACK_MINE, GuiItemBuilder.of(Material.PLAYER_HEAD)
                    .name("&a&lYour Recorded History")
                    .lore("&7Shows your own recorded",
                            "&7acquisition/transfer events",
                            "&7for this item type.",
                            "",
                            "&7Note: Minecraft does not track",
                            "&7individual item identity — this",
                            "&7is not a guaranteed chain of",
                            "&7custody for this exact stack.",
                            "",
                            "&eClick to view")
                    .build());

            inventory.setItem(SLOT_TRACK_GLOBAL, GuiItemBuilder.of(Material.COMPASS)
                    .name("&6&lGlobal Tracking")
                    .lore("&7Shows all recorded events",
                            "&7for this item type across",
                            "&7every player on the server.",
                            "",
                            "&eClick to view")
                    .build());
        }

        inventory.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW).name("&e&lBack to Main Menu").build());
        inventory.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER).name("&c&lClose").build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (snapshot != null && slot == SLOT_TRACK_MINE) {
            player.sendMessage("§e[ItemTracker] §fShowing your own recorded events for §f" +
                    snapshot.getType().name() + "§f. This reflects recorded activity only — " +
                    "§citem identity cannot be traced exactly in Minecraft§f.");

            LogFilter filter = LogFilter.empty()
                    .withPlayerUuid(player.getUniqueId().toString())
                    .withMaterialExact(snapshot.getType().name());
            ConsoleLogsHolder holder = new ConsoleLogsHolder(guiManager, player, filter,
                    "Your History: " + snapshot.getType().name());
            player.openInventory(holder.getInventory());
        } else if (snapshot != null && slot == SLOT_TRACK_GLOBAL) {
            TrackedEventDAO dao = guiManager.getPlugin().getTrackedEventDAO();
            List<ActionBreakdown> breakdown = dao.getActionBreakdownForMaterial(snapshot.getType().name());
            int total = dao.countByMaterialExact(snapshot.getType().name());

            player.sendMessage("§6[ItemTracker] §fGlobal history for §f" + snapshot.getType().name() +
                    " §7(" + total + " total events):");
            for (ActionBreakdown entry : breakdown) {
                player.sendMessage("§7 - " + entry.actionType() + ": §f" + entry.count());
            }

            LogFilter filter = LogFilter.empty().withMaterialExact(snapshot.getType().name());
            ConsoleLogsHolder holder = new ConsoleLogsHolder(guiManager, player, filter,
                    "Global: " + snapshot.getType().name());
            player.openInventory(holder.getInventory());
        } else if (slot == SLOT_BACK) {
            guiManager.openMainMenu(player);
        } else if (slot == SLOT_CLOSE) {
            player.closeInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}