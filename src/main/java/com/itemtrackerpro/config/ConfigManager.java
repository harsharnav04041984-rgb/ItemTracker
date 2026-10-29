package com.itemtrackerpro.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        load();
    }

    // ---------- Database ----------

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite").toLowerCase();
    }

    public String getSqliteFileName() {
        return config.getString("database.sqlite.file", "database.db");
    }

    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "itemtracker");
    }

    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "");
    }

    public boolean isMysqlUseSSL() {
        return config.getBoolean("database.mysql.useSSL", false);
    }

    public int getPoolMaxSize() {
        return config.getInt("database.pool.maximumPoolSize", 10);
    }

    public long getPoolConnectionTimeoutMs() {
        return config.getLong("database.pool.connectionTimeoutMs", 30000);
    }

    // ---------- Tracking toggles ----------

    public boolean isTrackingEnabled(String key) {
        return config.getBoolean("tracking." + key, true);
    }

    // ---------- Performance ----------

    public boolean isAsyncWritesEnabled() {
        return config.getBoolean("performance.async-writes", true);
    }

    public int getBatchInsertSize() {
        return config.getInt("performance.batch-insert-size", 50);
    }

    public long getDuplicateEventWindowMs() {
        return config.getLong("performance.duplicate-event-window-ms", 100);
    }

    // ---------- Logging ----------

    public boolean isDebugEnabled() {
        return config.getBoolean("logging.debug", false);
    }
    
    // ---------- Anti-Dupe ----------

    public boolean isAntiDupeEnabled() {
        return config.getBoolean("anti-dupe.enabled", true);
    }

    public int getAntiDupeLookbackHours() {
        return config.getInt("anti-dupe.lookback-hours", 24);
    }

    public long getAntiDupeSpikeWindowMs() {
        return config.getLong("anti-dupe.spike-window-ms", 60000);
    }

    public int getAntiDupeSpikeThreshold() {
        return config.getInt("anti-dupe.spike-threshold-quantity", 64);
    }

    public int getAntiDupeHashScanLimit() {
        return config.getInt("anti-dupe.duplicate-hash-scan-limit", 500);
    }
}