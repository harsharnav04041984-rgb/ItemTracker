package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.PlayerSummary;
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
import java.util.UUID;

public class PlayerTrackerHolder implements ItpGuiHolder {

    private static final int SLOT_SEARCH = 13;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_CLOSE = 26;

    private final GuiManager guiManager;
    private final Inventory inventory;

    public PlayerTrackerHolder(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Player Tracker"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        inventory.setItem(SLOT_SEARCH, GuiItemBuilder.of(Material.PLAYER_HEAD)
                .name("&a&lSearch Player")
                .lore("&7Search by exact player name",
                        "&7or UUID to view their",
                        "&7recorded activity.",
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
        player.sendMessage("§e[ItemTracker] §fEnter a player name or UUID to search.");

        guiManager.getPlugin().getChatInputManager().awaitInput(player, input -> {
            if (input == null) {
                player.openInventory(this.inventory);
                return;
            }
            resolveAndOpen(player, input.trim());
        });
    }

    private void resolveAndOpen(Player player, String query) {
        TrackedEventDAO dao = guiManager.getPlugin().getTrackedEventDAO();

        UUID uuid = tryParseUuid(query);
        if (uuid != null) {
            PlayerSummary summary = dao.findPlayerSummaryByUuid(uuid.toString());
            if (summary == null) {
                player.sendMessage("§cNo recorded activity found for UUID: " + uuid);
                player.openInventory(this.inventory);
                return;
            }
            openPlayerActivity(player, summary);
            return;
        }

        List<PlayerSummary> matches = dao.findDistinctPlayersByNameLike(query, 25);
        if (matches.isEmpty()) {
            player.sendMessage("§cNo recorded activity found for player name: " + query);
            player.openInventory(this.inventory);
        } else if (matches.size() == 1) {
            openPlayerActivity(player, matches.get(0));
        } else {
            PlayerSelectionHolder selection = new PlayerSelectionHolder(guiManager, matches,
                    p -> p.openInventory(this.inventory),
                    this::openPlayerActivity);
            player.openInventory(selection.getInventory());
        }
    }

    private void openPlayerActivity(Player player, PlayerSummary summary) {
        player.sendMessage("§a[ItemTracker] §fShowing §f" + summary.eventCount() +
                "§f recorded events for §f" + summary.name() + " §7(" + summary.uuid() + ")");

        LogFilter filter = LogFilter.empty().withPlayerUuid(summary.uuid());
        ConsoleLogsHolder holder = new ConsoleLogsHolder(guiManager, player, filter, "Player: " + summary.name());
        player.openInventory(holder.getInventory());
    }

    private UUID tryParseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}