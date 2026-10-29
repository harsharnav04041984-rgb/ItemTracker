package com.itemtrackerpro.database;

import com.itemtrackerpro.ItemTrackerPro;
import com.itemtrackerpro.gui.model.LogFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class TrackedEventDAO {

    private final ItemTrackerPro plugin;
    private final DatabaseManager databaseManager;

    private static final String INSERT_SQL =
            "INSERT INTO " + DatabaseSchema.TABLE_NAME +
            " (player_uuid, player_name, action_type, material, quantity, world, x, y, z, timestamp, event_hash) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public TrackedEventDAO(ItemTrackerPro plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    // ---------- Phase 1 core methods ----------

    public long insertEventSync(EventRecord record) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, record.playerUuid());
            ps.setString(2, record.playerName());
            ps.setString(3, record.actionType());
            ps.setString(4, record.material());
            ps.setInt(5, record.quantity());
            ps.setString(6, record.world());
            setNullableInt(ps, 7, record.x());
            setNullableInt(ps, 8, record.y());
            setNullableInt(ps, 9, record.z());
            ps.setLong(10, record.timestamp());
            ps.setString(11, record.eventHash());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to insert tracked event", e);
        }
        return -1L;
    }

    public CompletableFuture<Long> insertEventAsync(EventRecord record) {
        return CompletableFuture.supplyAsync(() -> insertEventSync(record), databaseManager.getAsyncExecutor());
    }

    public List<EventRecord> findByPlayer(UUID playerUuid, int limit) {
        String sql = "SELECT * FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ?";
        List<EventRecord> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUuid.toString());
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query events by player", e);
        }
        return results;
    }

    public List<EventRecord> findByMaterial(String material, int limit) {
        String sql = "SELECT * FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE material = ? ORDER BY timestamp DESC LIMIT ?";
        List<EventRecord> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, material);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query events by material", e);
        }
        return results;
    }

    public boolean existsByHashSince(String eventHash, long sinceTimestamp) {
        String sql = "SELECT 1 FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE event_hash = ? AND timestamp >= ? LIMIT 1";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, eventHash);
            ps.setLong(2, sinceTimestamp);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check duplicate event hash", e);
            return false;
        }
    }

    // ---------- Phase 2, Part 2 additions ----------

    public List<EventRecord> findFiltered(LogFilter filter, int page, int pageSize) {
        EventQueryBuilder.BuiltQuery query = EventQueryBuilder.build(filter);
        String sql = "SELECT * FROM " + DatabaseSchema.TABLE_NAME + query.whereClause() +
                " ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        List<EventRecord> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (Object param : query.parameters()) {
                ps.setObject(idx++, param);
            }
            ps.setInt(idx++, pageSize);
            ps.setInt(idx, Math.max(0, page) * pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query filtered events", e);
        }
        return results;
    }

    public int countFiltered(LogFilter filter) {
        EventQueryBuilder.BuiltQuery query = EventQueryBuilder.build(filter);
        String sql = "SELECT COUNT(*) FROM " + DatabaseSchema.TABLE_NAME + query.whereClause();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (Object param : query.parameters()) {
                ps.setObject(idx++, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to count filtered events", e);
        }
        return 0;
    }

    public EventRecord findById(long id) {
        String sql = "SELECT * FROM " + DatabaseSchema.TABLE_NAME + " WHERE id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to query event by id", e);
        }
        return null;
    }

    // ---------- Phase 2, Part 3 additions ----------

    /** Finds distinct players (grouped by UUID) whose recorded name matches the given partial string. */
    public List<PlayerSummary> findDistinctPlayersByNameLike(String partial, int limit) {
        String sql = "SELECT player_uuid, MAX(player_name) as player_name, COUNT(*) as cnt, MAX(timestamp) as last_ts " +
                "FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE player_name LIKE ? " +
                "GROUP BY player_uuid " +
                "ORDER BY last_ts DESC LIMIT ?";

        List<PlayerSummary> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + partial + "%");
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new PlayerSummary(
                            rs.getString("player_uuid"),
                            rs.getString("player_name"),
                            rs.getInt("cnt"),
                            rs.getLong("last_ts")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find players by name", e);
        }
        return results;
    }

    /** Returns a recorded-activity summary for an exact player UUID, or null if no events exist. */
    public PlayerSummary findPlayerSummaryByUuid(String uuid) {
        String countSql = "SELECT COUNT(*) as cnt, MAX(timestamp) as last_ts FROM " +
                DatabaseSchema.TABLE_NAME + " WHERE player_uuid = ?";
        String nameSql = "SELECT player_name FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT 1";

        int count;
        long lastSeen;
        String name;

        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt("cnt") == 0) {
                        return null;
                    }
                    count = rs.getInt("cnt");
                    lastSeen = rs.getLong("last_ts");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    name = rs.next() ? rs.getString("player_name") : "Unknown";
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to fetch player summary by uuid", e);
            return null;
        }

        return new PlayerSummary(uuid, name, count, lastSeen);
    }

    public int countByMaterialExact(String material) {
        String sql = "SELECT COUNT(*) FROM " + DatabaseSchema.TABLE_NAME + " WHERE material = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, material);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to count events by material", e);
        }
        return 0;
    }

    public List<ActionBreakdown> getActionBreakdownForMaterial(String material) {
        String sql = "SELECT action_type, COUNT(*) as cnt FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE material = ? GROUP BY action_type ORDER BY cnt DESC";

        List<ActionBreakdown> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, material);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new ActionBreakdown(rs.getString("action_type"), rs.getInt("cnt")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get action breakdown for material", e);
        }
        return results;
    }

    /**
     * Finds event hashes that appear more than once since the given timestamp.
     * A duplicate hash surviving the in-memory filter (e.g. across a server
     * restart) is a statistical indicator worth manual review, not proof of
     * an exploit.
     */
    public List<DuplicateHashGroup> findDuplicateHashes(long sinceTimestamp, int limit) {
        String sql = "SELECT event_hash, COUNT(*) as cnt, " +
                "MAX(material) as material, MAX(action_type) as action_type, MAX(player_name) as player_name " +
                "FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE timestamp >= ? AND event_hash IS NOT NULL AND event_hash != '' " +
                "GROUP BY event_hash HAVING COUNT(*) > 1 " +
                "ORDER BY cnt DESC LIMIT ?";

        List<DuplicateHashGroup> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, sinceTimestamp);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new DuplicateHashGroup(
                            rs.getString("event_hash"),
                            rs.getInt("cnt"),
                            rs.getString("material"),
                            rs.getString("action_type"),
                            rs.getString("player_name")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find duplicate hashes", e);
        }
        return results;
    }

    /**
     * Groups events into fixed-width time buckets per player/material/action
     * and flags groups whose summed quantity exceeds the configured
     * threshold. Uses fixed buckets rather than a true sliding window for
     * cross-dialect simplicity (SQLite/MySQL); a spike split exactly across
     * a bucket boundary may not be flagged. This is a statistical indicator
     * only — always reviewed manually before action is taken.
     */
    public List<QuantitySpike> findQuantitySpikes(long sinceTimestamp, long spikeWindowMs,
                                                   int thresholdQuantity, int limit) {
        String sql = "SELECT player_uuid, MAX(player_name) as player_name, material, action_type, " +
                "(timestamp / ?) as bucket, SUM(quantity) as total_qty, COUNT(*) as event_count, " +
                "MIN(timestamp) as first_ts, MAX(timestamp) as last_ts " +
                "FROM " + DatabaseSchema.TABLE_NAME +
                " WHERE timestamp >= ? " +
                "GROUP BY player_uuid, material, action_type, bucket " +
                "HAVING SUM(quantity) >= ? " +
                "ORDER BY total_qty DESC LIMIT ?";

        List<QuantitySpike> results = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, Math.max(1, spikeWindowMs));
            ps.setLong(2, sinceTimestamp);
            ps.setInt(3, thresholdQuantity);
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new QuantitySpike(
                            rs.getString("player_uuid"),
                            rs.getString("player_name"),
                            rs.getString("material"),
                            rs.getString("action_type"),
                            rs.getInt("total_qty"),
                            rs.getInt("event_count"),
                            rs.getLong("first_ts"),
                            rs.getLong("last_ts")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find quantity spikes", e);
        }
        return results;
    }

    private EventRecord mapRow(ResultSet rs) throws SQLException {
        return new EventRecord(
                rs.getLong("id"),
                rs.getString("player_uuid"),
                rs.getString("player_name"),
                rs.getString("action_type"),
                rs.getString("material"),
                rs.getInt("quantity"),
                rs.getString("world"),
                getNullableInt(rs, "x"),
                getNullableInt(rs, "y"),
                getNullableInt(rs, "z"),
                rs.getLong("timestamp"),
                rs.getString("event_hash")
        );
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}