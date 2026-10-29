package com.itemtrackerpro.command;

import com.itemtrackerpro.ItemTrackerPro;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemTrackerCommand implements CommandExecutor {

    private final ItemTrackerPro plugin;

    public ItemTrackerCommand(ItemTrackerPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        if (!player.isOp() && !player.hasPermission("itemtrackerpro.admin")) {
            player.sendMessage("§cYou do not have permission to use ItemTracker Pro.");
            return true;
        }

        plugin.getGuiManager().openMainMenu(player);
        return true;
    }
}