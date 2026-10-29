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

public class AntiDupeHolder implements ItpGuiHolder {

    private static final int SLOT_DUPLICATES = 11;
    private static final int SLOT_SPIKES = 15;
    private static final int SLOT_BACK = 18;
    private static final int SLOT_CLOSE = 26;

    private final GuiManager guiManager;
    private final Inventory inventory;

    public AntiDupeHolder(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Anti-Dupe Detection"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        boolean enabled = guiManager.getPlugin().getConfigManager().isAntiDupeEnabled();

        inventory.setItem(SLOT_DUPLICATES, GuiItemBuilder.of(Material.REPEATER)
                .name("&c&lDuplicate Hash Matches")
                .lore("&7Events with identical fingerprints",
                        "&7recorded more than once.",
                        "&7Statistical indicator — review",
                        "&7manually, not conclusive proof.",
                        "",
                        enabled ? "&eClick to view" : "&cAnti-dupe is disabled in config")
                .build());

        inventory.setItem(SLOT_SPIKES, GuiItemBuilder.of(Material.TNT)
                .name("&6&lQuantity Spikes")
                .lore("&7Unusually large quantities of",
                        "&7a single item obtained quickly.",
                        "&7Statistical indicator — review",
                        "&7manually, not conclusive proof.",
                        "",
                        enabled ? "&eClick to view" : "&cAnti-dupe is disabled in config")
                .build());

        inventory.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW).name("&e&lBack to Main Menu").build());
        inventory.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER).name("&c&lClose").build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        boolean enabled = guiManager.getPlugin().getConfigManager().isAntiDupeEnabled();

        if (slot == SLOT_DUPLICATES) {
            if (!enabled) {
                player.sendMessage("§cAnti-dupe detection is disabled in config.yml.");
                return;
            }
            AntiDupeDuplicatesHolder holder = new AntiDupeDuplicatesHolder(guiManager);
            player.openInventory(holder.getInventory());
        } else if (slot == SLOT_SPIKES) {
            if (!enabled) {
                player.sendMessage("§cAnti-dupe detection is disabled in config.yml.");
                return;
            }
            AntiDupeSpikesHolder holder = new AntiDupeSpikesHolder(guiManager);
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