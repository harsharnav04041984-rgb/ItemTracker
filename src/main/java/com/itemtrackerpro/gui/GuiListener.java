package com.itemtrackerpro.gui;

import com.itemtrackerpro.ItemTrackerPro;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    private final ItemTrackerPro plugin;

    public GuiListener(ItemTrackerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (!(topHolder instanceof ItpGuiHolder guiHolder)) {
            return;
        }

        // Unconditionally cancelled: blocks extraction, insertion,
        // shift-clicks, hotbar-swap duplication, and number-key exploits
        // regardless of which slot was clicked.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Re-verified on every click in case OP status changed mid-session.
        if (!player.isOp() && !player.hasPermission("itemtrackerpro.admin")) {
            player.closeInventory();
            player.sendMessage("§cYou no longer have permission to use ItemTracker Pro.");
            return;
        }

        // Ignore clicks in the player's own inventory while a GUI is open above it.
        if (event.getClickedInventory() == null ||
                !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        guiHolder.handleClick(player, event.getRawSlot(), event.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (topHolder instanceof ItpGuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ItpGuiHolder guiHolder && event.getPlayer() instanceof Player player) {
            guiHolder.onClose(player);
        }
    }
}