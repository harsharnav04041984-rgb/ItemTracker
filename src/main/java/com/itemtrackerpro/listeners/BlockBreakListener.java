package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BlockBreakListener implements Listener {

    private final TrackingEngine trackingEngine;

    public BlockBreakListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        String world = loc.getWorld() != null ? loc.getWorld().getName() : null;

        if (event.isDropItems()) {
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
            Collection<ItemStack> drops = event.getBlock().getDrops(tool, event.getPlayer());

            if (!drops.isEmpty()) {
                for (ItemStack drop : drops) {
                    if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0) {
                        continue;
                    }
                    trackingEngine.track(TrackedEvent.now(
                            event.getPlayer().getUniqueId(),
                            event.getPlayer().getName(),
                            ActionType.BLOCK_BREAK,
                            drop.getType(),
                            drop.getAmount(),
                            world,
                            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
                    ));
                }
                return;
            }
        }

        // Fallback: no drops (creative mode, wrong tool tier, silk-required block, etc.)
        // Still record the raw block type so the break activity itself isn't lost.
        trackingEngine.track(TrackedEvent.now(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                ActionType.BLOCK_BREAK,
                event.getBlock().getType(),
                1,
                world,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()
        ));
    }
}