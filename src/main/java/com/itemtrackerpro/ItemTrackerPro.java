package com.itemtrackerpro;

import com.itemtrackerpro.command.ItemTrackerCommand;
import com.itemtrackerpro.config.ConfigManager;
import com.itemtrackerpro.database.DatabaseManager;
import com.itemtrackerpro.database.DuplicateEventFilter;
import com.itemtrackerpro.database.TrackedEventDAO;
import com.itemtrackerpro.engine.TrackingEngine;
import com.itemtrackerpro.engine.TransferCorrelator;
import com.itemtrackerpro.gui.GuiListener;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.input.AnvilInputGUI;
import com.itemtrackerpro.gui.input.ChatInputManager;
import com.itemtrackerpro.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemTrackerPro extends JavaPlugin {

    private static ItemTrackerPro instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TrackedEventDAO trackedEventDAO;
    private DuplicateEventFilter duplicateEventFilter;
    private TransferCorrelator transferCorrelator;
    private TrackingEngine trackingEngine;
    private GuiManager guiManager;
    private ChatInputManager chatInputManager;
    private AnvilInputGUI anvilInputGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        getLogger().info("ItemTracker Pro v" + getDescription().getVersion() + " enabled.");
        getLogger().info("Database type: " + configManager.getDatabaseType());

        this.databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Database initialization failed. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.trackedEventDAO = new TrackedEventDAO(this, databaseManager);
        this.duplicateEventFilter = new DuplicateEventFilter(configManager.getDuplicateEventWindowMs());
        this.transferCorrelator = new TransferCorrelator(
                getConfig().getLong("performance.transfer-correlation-window-ms", 5000));
        this.trackingEngine = new TrackingEngine(this, trackedEventDAO, duplicateEventFilter, transferCorrelator);

        registerListeners();

        getServer().getScheduler().runTaskTimerAsynchronously(
                this, duplicateEventFilter::cleanup, 20L * 60, 20L * 60);
        getServer().getScheduler().runTaskTimerAsynchronously(
                this, transferCorrelator::cleanup, 20L * 60, 20L * 60);

        // --- Phase 2: GUI layer ---
        this.guiManager = new GuiManager(this);
        this.chatInputManager = new ChatInputManager(this);
        this.anvilInputGUI = new AnvilInputGUI(this);

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputManager, this);
        getServer().getPluginManager().registerEvents(anvilInputGUI, this);

        var itemTrackerCmd = getCommand("itemtracker");
        if (itemTrackerCmd != null) {
            itemTrackerCmd.setExecutor(new ItemTrackerCommand(this));
        } else {
            getLogger().warning("Command 'itemtracker' not found in plugin.yml.");
        }

        getLogger().info("ItemTracker Pro fully initialized. Tracking engine online.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreakListener(trackingEngine), this);
        pm.registerEvents(new BlockPlaceListener(trackingEngine), this);
        pm.registerEvents(new CraftListener(trackingEngine), this);
        pm.registerEvents(new SmeltListener(trackingEngine), this);
        pm.registerEvents(new PickupListener(trackingEngine, transferCorrelator), this);
        pm.registerEvents(new DropListener(trackingEngine, transferCorrelator), this);
        pm.registerEvents(new ContainerInteractListener(trackingEngine), this);
        pm.registerEvents(new HopperTransferListener(trackingEngine), this);
        pm.registerEvents(new TradeListener(trackingEngine), this);
        pm.registerEvents(new FishingListener(trackingEngine), this);
        pm.registerEvents(new InventoryChangeListener(trackingEngine), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemTracker Pro disabled. Flushing pending data...");
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    public static ItemTrackerPro getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TrackedEventDAO getTrackedEventDAO() {
        return trackedEventDAO;
    }

    public TrackingEngine getTrackingEngine() {
        return trackingEngine;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public AnvilInputGUI getAnvilInputGUI() {
        return anvilInputGUI;
    }
}