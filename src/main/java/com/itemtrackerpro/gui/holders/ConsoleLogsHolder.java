package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.EventRecord;
import com.itemtrackerpro.database.TrackedEventDAO;
import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.model.LogFilter;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.util.TimestampUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class ConsoleLogsHolder implements ItpGuiHolder {

    private final GuiManager guiManager;
    private final Inventory inventory;
    private final Player viewer;

    private LogFilter filter;
    private int page = 0;
    private int totalPages = 1;
    private BukkitTask refreshTask;
    private boolean inputDialogOpen = false;

    public ConsoleLogsHolder(GuiManager guiManager, Player viewer, LogFilter filter) {
        this(guiManager, viewer, filter, "Console Logs");
    }

    public ConsoleLogsHolder(GuiManager guiManager, Player viewer, LogFilter filter, String titleSuffix) {
        this.guiManager = guiManager;
        this.viewer = viewer;
        this.filter = filter;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_6_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + titleSuffix));
        refresh();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        long intervalTicks = 20L * guiManager.getPlugin().getConfig()
                .getLong("gui.refresh-interval-seconds", 3);

        refreshTask = Bukkit.getScheduler().runTaskTimer(guiManager.getPlugin(), () -> {
            if (inputDialogOpen) {
                return;
            }
            if (!viewer.isOnline() || !viewer.getOpenInventory().getTopInventory().equals(inventory)) {
                refreshTask.cancel();
                return;
            }
            refresh();
        }, intervalTicks, intervalTicks);
    }

    private void refresh() {
        TrackedEventDAO dao = guiManager.getPlugin().getTrackedEventDAO();
        int pageSize = GuiConstants.CONTENT_SLOTS;

        int totalCount = dao.countFiltered(filter);
        totalPages = Math.max(1, (int) Math.ceil(totalCount / (double) pageSize));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        List<EventRecord> records = dao.findFiltered(filter, page, pageSize);

        for (int i = 0; i < GuiConstants.CONTENT_SLOTS; i++) {
            inventory.setItem(i, null);
        }

        for (int i = 0; i < records.size(); i++) {
            inventory.setItem(i, buildEventItem(records.get(i)));
        }

        buildControlRow();
    }

    private ItemStack buildEventItem(EventRecord record) {
        Material icon;
        try {
            icon = Material.valueOf(record.material());
        } catch (IllegalArgumentException ex) {
            icon = Material.PAPER;
        }

        return GuiItemBuilder.of(icon)
                .name("&b" + record.actionType() + " &7#" + record.id())
                .lore(
                        "&7Player: &f" + record.playerName(),
                        "&7Item: &f" + record.material() + " x" + record.quantity(),
                        "&7World: &f" + (record.world() != null ? record.world() : "N/A"),
                        "&7Time: &f" + TimestampUtil.format(record.timestamp()),
                        "",
                        "&eClick for full details"
                )
                .build();
    }

    private void buildControlRow() {
        for (int i = GuiConstants.CONTENT_SLOTS; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        inventory.setItem(GuiConstants.SLOT_BACK, GuiItemBuilder.of(Material.ARROW)
                .name("&e&lBack to Main Menu").build());

        boolean hasFilter = !filter.isEmpty();
        List<String> filterLore = new ArrayList<>();
        filterLore.add("&7Player: &f" + display(filter.playerName()));
        if (filter.playerUuid() != null) {
            filterLore.add("&7UUID: &f" + filter.playerUuid());
        }
        filterLore.add("&7Item: &f" + display(filter.material()));
        filterLore.add("&7Action: &f" + display(filter.actionType()));
        filterLore.add("&7World: &f" + display(filter.world()));
        if (filter.eventHash() != null) {
            filterLore.add("&7Hash Filter: &fActive");
        }
        filterLore.add("");
        filterLore.add("&eClick to edit filters");
        if (hasFilter) {
            filterLore.add("&cShift-click to clear");
        }

        inventory.setItem(GuiConstants.SLOT_FILTER, GuiItemBuilder.of(
                        hasFilter ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(hasFilter ? "&a&lFilters Active" : "&7&lNo Filters")
                .loreList(filterLore)
                .build());

        if (page > 0) {
            inventory.setItem(GuiConstants.SLOT_PREV_PAGE, GuiItemBuilder.of(Material.ARROW)
                    .name("&e&lPrevious Page").build());
        }

        inventory.setItem(GuiConstants.SLOT_PAGE_INFO, GuiItemBuilder.of(Material.BOOK)
                .name("&f&lPage " + (page + 1) + " / " + totalPages)
                .build());

        if (page < totalPages - 1) {
            inventory.setItem(GuiConstants.SLOT_NEXT_PAGE, GuiItemBuilder.of(Material.ARROW)
                    .name("&e&lNext Page").build());
        }

        inventory.setItem(GuiConstants.SLOT_HOME, GuiItemBuilder.of(Material.NETHER_STAR)
                .name("&d&lMain Menu").build());

        inventory.setItem(GuiConstants.SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&c&lClose").build());
    }

    private String display(String value) {
        return value == null ? "Any" : value;
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot < GuiConstants.CONTENT_SLOTS) {
            TrackedEventDAO dao = guiManager.getPlugin().getTrackedEventDAO();
            List<EventRecord> records = dao.findFiltered(filter, page, GuiConstants.CONTENT_SLOTS);
            if (slot < records.size()) {
                EventRecord record = records.get(slot);
                EventDetailHolder detail = new EventDetailHolder(guiManager, record,
                        () -> player.openInventory(this.inventory));
                player.openInventory(detail.getInventory());
            }
            return;
        }

        if (slot == GuiConstants.SLOT_BACK || slot == GuiConstants.SLOT_HOME) {
            guiManager.openMainMenu(player);
        } else if (slot == GuiConstants.SLOT_FILTER) {
            if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                filter = LogFilter.empty();
                refresh();
            } else {
                openFilterPrompt(player);
            }
        } else if (slot == GuiConstants.SLOT_PREV_PAGE && page > 0) {
            page--;
            refresh();
        } else if (slot == GuiConstants.SLOT_NEXT_PAGE && page < totalPages - 1) {
            page++;
            refresh();
        } else if (slot == GuiConstants.SLOT_CLOSE) {
            player.closeInventory();
        }
    }

    private void openFilterPrompt(Player player) {
        inputDialogOpen = true;
        player.closeInventory();
        player.sendMessage("§e[ItemTracker] §fFilter format: §fplayer=<name> uuid=<uuid> item=<material> " +
                "action=<type> world=<world>");
        player.sendMessage("§7Example: §fplayer=Steve item=DIAMOND action=BLOCK_BREAK");
        player.sendMessage("§7Leave any field out to not filter by it. Type §ccancel§7 to abort.");

        guiManager.getPlugin().getChatInputManager().awaitInput(player, input -> {
            inputDialogOpen = false;
            if (input != null) {
                filter = parseFilterInput(input);
                page = 0;
            }
            refresh();
            player.openInventory(this.inventory);
        });
    }

    private LogFilter parseFilterInput(String input) {
        LogFilter result = LogFilter.empty();
        String[] tokens = input.split("\\s+");

        for (String token : tokens) {
            String[] parts = token.split("=", 2);
            if (parts.length != 2) continue;

            String key = parts[0].toLowerCase();
            String value = parts[1];

            result = switch (key) {
                case "player" -> result.withPlayerName(value);
                case "uuid" -> result.withPlayerUuid(value);
                case "item" -> result.withMaterial(value);
                case "world" -> result.withWorld(value);
                case "action" -> {
                    try {
                        yield result.withActionType(ActionType.valueOf(value.toUpperCase()).name());
                    } catch (IllegalArgumentException ex) {
                        viewer.sendMessage("§cUnknown action type: " + value);
                        yield result;
                    }
                }
                default -> result;
            };
        }
        return result;
    }

    @Override
    public void onClose(Player player) {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}