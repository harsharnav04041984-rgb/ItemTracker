package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingListener implements Listener {

    private final TrackingEngine trackingEngine;

    public FishingListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!(event.getCaught() instanceof Item caughtItem)) {
            return;
        }

        ItemStack stack = caughtItem.getItemStack();
        Location loc = event.getPlayer().getLocation();

        trackingEngine.track(TrackedEvent.now(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                ActionType.FISHING,
                stack.getType(),
                stack.getAmount(),
                loc.getWorld() != null ? loc.getWorld().getName() : null,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}