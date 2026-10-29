package com.itemtrackerpro.gui;

import com.itemtrackerpro.ItemTrackerPro;
import com.itemtrackerpro.gui.holders.AntiDupeHolder;
import com.itemtrackerpro.gui.holders.CheckHandItemHolder;
import com.itemtrackerpro.gui.holders.ConsoleLogsHolder;
import com.itemtrackerpro.gui.holders.ItemSearchHolder;
import com.itemtrackerpro.gui.holders.MainMenuHolder;
import com.itemtrackerpro.gui.holders.PlayerTrackerHolder;
import com.itemtrackerpro.gui.model.LogFilter;
import org.bukkit.entity.Player;

public class GuiManager {

    private final ItemTrackerPro plugin;

    public GuiManager(ItemTrackerPro plugin) {
        this.plugin = plugin;
    }

    public ItemTrackerPro getPlugin() {
        return plugin;
    }

    public void openMainMenu(Player player) {
        MainMenuHolder holder = new MainMenuHolder(this);
        player.openInventory(holder.getInventory());
    }

    public void openConsoleLogs(Player player) {
        ConsoleLogsHolder holder = new ConsoleLogsHolder(this, player, LogFilter.empty());
        player.openInventory(holder.getInventory());
    }

    public void openAntiDupe(Player player) {
        AntiDupeHolder holder = new AntiDupeHolder(this);
        player.openInventory(holder.getInventory());
    }

    public void openPlayerTracker(Player player) {
        PlayerTrackerHolder holder = new PlayerTrackerHolder(this);
        player.openInventory(holder.getInventory());
    }

    public void openItemSearch(Player player) {
        ItemSearchHolder holder = new ItemSearchHolder(this);
        player.openInventory(holder.getInventory());
    }

    public void openHandItemMenu(Player player) {
        CheckHandItemHolder holder = new CheckHandItemHolder(this, player);
        player.openInventory(holder.getInventory());
    }
}