package com.itemtrackerpro.database;

import java.util.List;

public final class DatabaseSchema {

    private DatabaseSchema() {}

    public static final String TABLE_NAME = "tracked_events";

    public static String getCreateTableStatement(DatabaseManager.DatabaseType type) {
        String idColumn = switch (type) {
            case SQLITE -> "id INTEGER PRIMARY KEY AUTOINCREMENT";
            case MYSQL, MARIADB -> "id BIGINT AUTO_INCREMENT PRIMARY KEY";
        };

        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                idColumn + ", " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "action_type VARCHAR(32) NOT NULL, " +
                "material VARCHAR(64) NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "world VARCHAR(64), " +
                "x INTEGER, " +
                "y INTEGER, " +
                "z INTEGER, " +
                "timestamp BIGINT NOT NULL, " +
                "event_hash VARCHAR(64)" +
                ")";
    }

    public static List<String> getCreateIndexStatements() {
        return List.of(
                "CREATE INDEX idx_itp_player_uuid ON " + TABLE_NAME + " (player_uuid)",
                "CREATE INDEX idx_itp_material ON " + TABLE_NAME + " (material)",
                "CREATE INDEX idx_itp_action_type ON " + TABLE_NAME + " (action_type)",
                "CREATE INDEX idx_itp_timestamp ON " + TABLE_NAME + " (timestamp)",
                "CREATE INDEX idx_itp_event_hash ON " + TABLE_NAME + " (event_hash)"
        );
    }
}