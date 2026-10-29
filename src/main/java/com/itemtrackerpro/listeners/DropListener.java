package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.engine.TransferCorrelator;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class DropListener implements Listener {

    private final TrackingEngine trackingEngine;
    private final TransferCorrelator transferCorrelator;

    public DropListener(TrackingEngine trackingEngine, TransferCorrelator transferCorrelator) {
        this.trackingEngine = trackingEngine;
        this.transferCorrelator = transferCorrelator;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemDrop().getItemStack();
        Location loc = event.getItemDrop().getLocation();

        transferCorrelator.registerDrop(event.getItemDrop().getUniqueId(), player.getUniqueId(), player.getName());

        trackingEngine.track(TrackedEvent.now(
                player.getUniqueId(),
                player.getName(),
                ActionType.DROP,
                stack.getType(),
                stack.getAmount(),
                loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}