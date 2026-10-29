package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

public class TradeListener implements Listener {

    private static final int MERCHANT_RESULT_SLOT = 2;

    private final TrackingEngine trackingEngine;

    public TradeListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrade(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getRawSlot() != MERCHANT_RESULT_SLOT) {
            return;
        }
        if (event.getAction() == InventoryAction.NOTHING) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) {
            return;
        }

        trackingEngine.track(TrackedEvent.now(
                player.getUniqueId(),
                player.getName(),
                ActionType.TRADE,
                result.getType(),
                result.getAmount(),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
        ));
    }
}