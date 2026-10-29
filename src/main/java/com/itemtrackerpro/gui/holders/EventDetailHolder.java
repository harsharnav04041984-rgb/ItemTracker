package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.EventRecord;
import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import com.itemtrackerpro.util.TimestampUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class EventDetailHolder implements ItpGuiHolder {

    private static final int SLOT_INFO = 13;
    private static final int SLOT_TELEPORT = 21;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_CLOSE = 26;

    private final GuiManager guiManager;
    private final Inventory inventory;
    private final EventRecord record;
    private final Runnable onBack;

    public EventDetailHolder(GuiManager guiManager, EventRecord record, Runnable onBack) {
        this.guiManager = guiManager;
        this.record = record;
        this.onBack = onBack;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Event #" + record.id()));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        Material icon;
        try {
            icon = Material.valueOf(record.material());
        } catch (IllegalArgumentException ex) {
            icon = Material.PAPER;
        }

        boolean hasLocation = record.world() != null && record.x() != null
                && record.y() != null && record.z() != null;

        inventory.setItem(SLOT_INFO, GuiItemBuilder.of(icon)
                .name("&b&lEvent Details")
                .lore(
                        "&7Event ID: &f#" + record.id(),
                        "&7Action: &f" + record.actionType(),
                        "&7Material: &f" + record.material(),
                        "&7Quantity: &f" + record.quantity(),
                        "&7Player: &f" + record.playerName(),
                        "&7UUID: &f" + record.playerUuid(),
                        "&7World: &f" + (record.world() != null ? record.world() : "N/A"),
                        hasLocation
                                ? "&7Location: &fX:" + record.x() + " Y:" + record.y() + " Z:" + record.z()
                                : "&7Location: &fNot recorded for this action",
                        "&7Time: &f" + TimestampUtil.format(record.timestamp())
                )
                .build());

        if (hasLocation) {
            inventory.setItem(SLOT_TELEPORT, GuiItemBuilder.of(Material.ENDER_PEARL)
                    .name("&a&lTeleport to Location")
                    .lore("&7Requires confirmation.",
                            "",
                            "&eClick to continue")
                    .build());
        } else {
            inventory.setItem(SLOT_TELEPORT, GuiItemBuilder.of(Material.BARRIER)
                    .name("&7Teleport Unavailable")
                    .lore("&7This event has no recorded coordinates.")
                    .build());
        }

        inventory.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW)
                .name("&e&lBack")
                .build());

        inventory.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&c&lClose")
                .build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == SLOT_TELEPORT && record.world() != null && record.x() != null
                && record.y() != null && record.z() != null) {
            TeleportConfirmHolder confirmHolder = new TeleportConfirmHolder(
                    guiManager, record.world(), record.x(), record.y(), record.z(),
                    () -> player.openInventory(this.inventory));
            player.openInventory(confirmHolder.getInventory());
        } else if (slot == SLOT_BACK) {
            if (onBack != null) {
                onBack.run();
            } else {
                player.closeInventory();
            }
        } else if (slot == SLOT_CLOSE) {
            player.closeInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}