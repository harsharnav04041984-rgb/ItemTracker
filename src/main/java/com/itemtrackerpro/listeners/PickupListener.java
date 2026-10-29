package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.engine.TransferCorrelator;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PickupListener implements Listener {

    private final TrackingEngine trackingEngine;
    private final TransferCorrelator transferCorrelator;

    public PickupListener(TrackingEngine trackingEngine, TransferCorrelator transferCorrelator) {
        this.trackingEngine = trackingEngine;
        this.transferCorrelator = transferCorrelator;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItem().getItemStack();
        Location loc = player.getLocation();

        Optional<TransferCorrelator.DropInfo> transfer =
                transferCorrelator.consumeIfTransfer(event.getItem().getUniqueId(), player.getUniqueId());

        ActionType actionType = transfer.isPresent() ? ActionType.PLAYER_TRANSFER : ActionType.PICKUP;

        trackingEngine.track(TrackedEvent.now(
                player.getUniqueId(),
                player.getName(),
                actionType,
                stack.getType(),
                stack.getAmount(),
                loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}