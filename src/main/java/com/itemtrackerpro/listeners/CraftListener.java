package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftListener implements Listener {

    private final TrackingEngine trackingEngine;

    public CraftListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        if (result.getType().isAir()) {
            return;
        }

        // Shift-click crafting can produce multiple batches per click;
        // CraftItemEvent fires once regardless, so this logs the per-click
        // yield. Exact bulk-craft totals via inventory diffing are planned
        // for Phase 2.
        trackingEngine.track(TrackedEvent.now(
                player.getUniqueId(),
                player.getName(),
                ActionType.CRAFT,
                result.getType(),
                result.getAmount(),
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
        ));
    }
}