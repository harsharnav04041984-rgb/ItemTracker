package com.itemtrackerpro.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Implemented by every custom ItemTracker Pro GUI. GuiListener treats any
 * inventory whose holder implements this interface as protected: all clicks
 * and drags are cancelled by default, and only explicitly handled slots can
 * trigger behavior.
 */
public interface ItpGuiHolder extends InventoryHolder {

    /**
     * Called by GuiListener after permission has been re-verified and the
     * click has already been cancelled. Implementations decide what, if
     * anything, happens for the given raw slot.
     */
    void handleClick(Player player, int slot, ClickType clickType);

    /**
     * Called when the viewing player closes this inventory. Used to cancel
     * scheduled refresh tasks or clear pending input state. No-op by default.
     */
    default void onClose(Player player) {
    }

    @Override
    Inventory getInventory();
}