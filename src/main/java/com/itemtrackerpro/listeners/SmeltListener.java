package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class SmeltListener implements Listener {

    private final TrackingEngine trackingEngine;

    public SmeltListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (event.getItemType().isAir() || event.getItemAmount() <= 0) {
            return;
        }

        trackingEngine.track(TrackedEvent.now(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                ActionType.SMELT,
                event.getItemType(),
                event.getItemAmount(),
                event.getBlock().getWorld().getName(),
                event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ()
        ));
    }
}