package com.itemtrackerpro.gui.input;

import com.itemtrackerpro.ItemTrackerPro;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Single-purpose anvil-based text input prompt. One instance of this
 * listener serves all anvil prompts plugin-wide, keyed by player UUID.
 */
public class AnvilInputGUI implements Listener {

    private record Session(Consumer<String> callback, boolean[] completed) {
    }

    private final ItemTrackerPro plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public AnvilInputGUI(ItemTrackerPro plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String promptText, Consumer<String> onSubmit) {
        Inventory anvil = plugin.getServer().createInventory(null, InventoryType.ANVIL,
                Component.text("Enter Value"));

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.displayName(Component.text(promptText));
        paper.setItemMeta(meta);

        anvil.setItem(0, paper);
        sessions.put(player.getUniqueId(), new Session(onSubmit, new boolean[]{false}));
        player.openInventory(anvil);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() == 2) {
            String typed = null;

            if (event.getInventory() instanceof AnvilInventory anvilInventory) {
                String renameText = anvilInventory.getRenameText();
                if (renameText != null && !renameText.isBlank()) {
                    typed = renameText.trim();
                }
            }

            session.completed()[0] = true;
            sessions.remove(player.getUniqueId());
            player.closeInventory();

            String finalValue = typed;
            plugin.getServer().getScheduler().runTask(plugin, () -> session.callback().accept(finalValue));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Session session = sessions.remove(player.getUniqueId());
        if (session != null && !session.completed()[0]) {
            plugin.getServer().getScheduler().runTask(plugin, () -> session.callback().accept(null));
        }
    }
}