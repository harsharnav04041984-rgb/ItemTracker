package com.itemtrackerpro.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class InventoryChangeListener implements Listener {

    private final TrackingEngine trackingEngine;

    public InventoryChangeListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        ItemStack newItem = event.getNewItem();
        ItemStack oldItem = event.getOldItem();

        ItemStack relevant = (newItem != null && !newItem.getType().isAir()) ? newItem : oldItem;
        if (relevant == null || relevant.getType().isAir()) {
            return;
        }

        Location loc = event.getPlayer().getLocation();

        trackingEngine.track(TrackedEvent.now(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                ActionType.INVENTORY_CHANGE,
                relevant.getType(),
                relevant.getAmount(),
                loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}