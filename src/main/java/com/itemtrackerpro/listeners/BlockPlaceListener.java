package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final TrackingEngine trackingEngine;

    public BlockPlaceListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlockPlaced().getLocation();

        trackingEngine.track(TrackedEvent.now(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                ActionType.BLOCK_PLACE,
                event.getBlockPlaced().getType(),
                1,
                loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}