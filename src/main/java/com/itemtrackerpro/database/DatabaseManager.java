package com.itemtrackerpro.database;

import com.itemtrackerpro.ItemTrackerPro;
import com.itemtrackerpro.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DatabaseManager {

    public enum DatabaseType {
        SQLITE, MYSQL, MARIADB
    }

    private final ItemTrackerPro plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;
    private ExecutorService asyncExecutor;
    private DatabaseType databaseType;

    public DatabaseManager(ItemTrackerPro plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public boolean initialize() {
        try {
            String typeStr = configManager.getDatabaseType();
            switch (typeStr) {
                case "mysql" -> {
                    databaseType = DatabaseType.MYSQL;
                    initializeMysql();
                }
                case "mariadb" -> {
                    databaseType = DatabaseType.MARIADB;
                    initializeMysql(); // Uses the MySQL connector; compatible for standard CRUD usage.
                }
                default -> {
                    databaseType = DatabaseType.SQLITE;
                    initializeSqlite();
                }
            }

            this.asyncExecutor = Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "ItemTrackerPro-DB-Worker");
                t.setDaemon(true);
                return t;
            });

            createSchema();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }

    private void initializeSqlite() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, configManager.getSqliteFileName());

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        // SQLite handles one writer safely; a single pooled connection avoids lock contention.
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setConnectionTimeout(configManager.getPoolConnectionTimeoutMs());
        hikariConfig.setPoolName("ItemTrackerPro-SQLite");

        this.dataSource = new HikariDataSource(hikariConfig);

        // Enable WAL mode for better read/write concurrency.
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Could not set SQLite PRAGMA options", e);
        }
    }

    private void initializeMysql() {
        HikariConfig hikariConfig = new HikariConfig();
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&characterEncoding=utf8",
                configManager.getMysqlHost(),
                configManager.getMysqlPort(),
                configManager.getMysqlDatabase(),
                configManager.isMysqlUseSSL()
        );

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(configManager.getMysqlUsername());
        hikariConfig.setPassword(configManager.getMysqlPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(configManager.getPoolMaxSize());
        hikariConfig.setConnectionTimeout(configManager.getPoolConnectionTimeoutMs());
        hikariConfig.setPoolName("ItemTrackerPro-MySQL");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private void createSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(DatabaseSchema.getCreateTableStatement(databaseType));

            for (String indexStmt : DatabaseSchema.getCreateIndexStatements()) {
                try {
                    stmt.execute(indexStmt);
                } catch (SQLException e) {
                    // MySQL has no "IF NOT EXISTS" for indexes — safely ignore "already exists" errors.
                    if (configManager.isDebugEnabled()) {
                        plugin.getLogger().info("Index creation skipped: " + e.getMessage());
                    }
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database has not been initialized.");
        }
        return dataSource.getConnection();
    }

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}