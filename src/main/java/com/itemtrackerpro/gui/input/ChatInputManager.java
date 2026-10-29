package com.itemtrackerpro.gui.input;

import com.itemtrackerpro.ItemTrackerPro;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Captures a player's next chat message for use as GUI search input, instead
 * of broadcasting it normally. Register one callback per player via
 * {@link #awaitInput(Player, Consumer)}; typing "cancel" aborts and invokes
 * the callback with null.
 */
public class ChatInputManager implements Listener {

    private final ItemTrackerPro plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public ChatInputManager(ItemTrackerPro plugin) {
        this.plugin = plugin;
    }

    public void awaitInput(Player player, Consumer<String> callback) {
        pending.put(player.getUniqueId(), callback);
        player.sendMessage("§e[ItemTracker] §fType your search in chat, or type §ccancel§f to abort.");
    }

    public boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = pending.remove(player.getUniqueId());
        if (callback == null) {
            return;
        }

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage("§7[ItemTracker] §fSearch cancelled.");
                callback.accept(null);
            } else {
                callback.accept(message);
            }
        });
    }
}