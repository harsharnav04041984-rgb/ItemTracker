package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class HopperTransferListener implements Listener {

    // Automated item movement has no player actor. Attributed to a fixed
    // system UUID/name instead of leaving the NOT NULL player columns empty.
    public static final UUID SYSTEM_UUID = new UUID(0L, 0L);
    public static final String SYSTEM_NAME = "SYSTEM";

    private final TrackingEngine trackingEngine;

    public HopperTransferListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemMove(InventoryMoveItemEvent event) {
        ItemStack stack = event.getItem();
        if (stack.getType().isAir()) {
            return;
        }

        Location loc = event.getDestination().getLocation();

        trackingEngine.track(TrackedEvent.now(
                SYSTEM_UUID,
                SYSTEM_NAME,
                ActionType.CONTAINER_TRANSFER,
                stack.getType(),
                stack.getAmount(),
                loc != null && loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc != null ? loc.getBlockX() : null,
                loc != null ? loc.getBlockY() : null,
                loc != null ? loc.getBlockZ() : null
        ));
    }
}