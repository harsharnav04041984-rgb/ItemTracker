package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class TeleportConfirmHolder implements ItpGuiHolder {

    private static final int SLOT_CONFIRM = 11;
    private static final int SLOT_CANCEL = 15;

    private final GuiManager guiManager;
    private final Inventory inventory;
    private final String worldName;
    private final int x, y, z;
    private final Runnable onCancelReturn;

    public TeleportConfirmHolder(GuiManager guiManager, String worldName, int x, int y, int z,
                                  Runnable onCancelReturn) {
        this.guiManager = guiManager;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onCancelReturn = onCancelReturn;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Confirm Teleport"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        inventory.setItem(SLOT_CONFIRM, GuiItemBuilder.of(Material.LIME_WOOL)
                .name("&a&lConfirm Teleport")
                .lore("&7World: &f" + worldName,
                        "&7X: &f" + x + " &7Y: &f" + y + " &7Z: &f" + z,
                        "",
                        "&eClick to teleport")
                .build());

        inventory.setItem(SLOT_CANCEL, GuiItemBuilder.of(Material.RED_WOOL)
                .name("&c&lCancel")
                .lore("&7Return without teleporting")
                .build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == SLOT_CONFIRM) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.closeInventory();
                player.sendMessage("§cThat world is not currently loaded. Teleport aborted.");
                return;
            }
            Location destination = new Location(world, x + 0.5, y, z + 0.5);
            player.closeInventory();
            player.teleportAsync(destination).thenAccept(success -> {
                if (Boolean.TRUE.equals(success)) {
                    player.sendMessage("§a[ItemTracker] §fTeleported to recorded event location.");
                } else {
                    player.sendMessage("§c[ItemTracker] §fTeleport failed.");
                }
            });
        } else if (slot == SLOT_CANCEL) {
            player.closeInventory();
            if (onCancelReturn != null) {
                onCancelReturn.run();
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}