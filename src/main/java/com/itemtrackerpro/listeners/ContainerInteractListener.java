package com.itemtrackerpro.listeners;

import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.model.ActionType;
import com.itemtrackerpro.model.TrackedEvent;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ContainerInteractListener implements Listener {

    private final TrackingEngine trackingEngine;

    public ContainerInteractListener(TrackingEngine trackingEngine) {
        this.trackingEngine = trackingEngine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder topHolder = topInventory.getHolder();

        if (!isTrackedContainer(topHolder)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        InventoryAction action = event.getAction();
        Location containerLoc = resolveLocation(topHolder);

        if (clickedInventory.equals(topInventory)) {
            ItemStack relevant = event.getCurrentItem();
            if (relevant == null || relevant.getType().isAir()) {
                return;
            }

            if (isWithdrawAction(action)) {
                track(player, ActionType.CONTAINER_WITHDRAW, relevant, containerLoc);
            } else if (isDepositAction(action)) {
                track(player, ActionType.CONTAINER_DEPOSIT, relevant, containerLoc);
            }
        } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // Shift-click from the player's own inventory into the open container.
            ItemStack moved = event.getCurrentItem();
            if (moved == null || moved.getType().isAir()) {
                return;
            }
            track(player, ActionType.CONTAINER_DEPOSIT, moved, containerLoc);
        }
    }

    private boolean isTrackedContainer(InventoryHolder holder) {
        // BlockState covers chests, barrels, shulker boxes, furnaces,
        // dispensers, droppers, brewing stands, and hoppers.
        return holder instanceof BlockState || holder instanceof DoubleChest;
    }

    private boolean isWithdrawAction(InventoryAction action) {
        return switch (action) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME, MOVE_TO_OTHER_INVENTORY -> true;
            default -> false;
        };
    }

    private boolean isDepositAction(InventoryAction action) {
        return switch (action) {
            case PLACE_ALL, PLACE_ONE, PLACE_SOME, SWAP_WITH_CURSOR -> true;
            default -> false;
        };
    }

    private Location resolveLocation(InventoryHolder holder) {
        if (holder instanceof BlockState blockState) {
            return blockState.getLocation();
        }
        if (holder instanceof DoubleChest doubleChest) {
            return doubleChest.getLocation();
        }
        return null;
    }

    private void track(Player player, ActionType actionType, ItemStack stack, Location loc) {
        trackingEngine.track(TrackedEvent.now(
                player.getUniqueId(),
                player.getName(),
                actionType,
                stack.getType(),
                stack.getAmount(),
                loc != null && loc.getWorld() != null ? loc.getWorld().getName() : player.getWorld().getName(),
                loc != null ? loc.getBlockX() : null,
                loc != null ? loc.getBlockY() : null,
                loc != null ? loc.getBlockZ() : null
        ));
    }
}