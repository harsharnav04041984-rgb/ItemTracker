package com.itemtrackerpro.database;

import com.itemtrackerpro.gui.model.LogFilter;

import java.util.ArrayList;
import java.util.List;

public final class EventQueryBuilder {

    private EventQueryBuilder() {
    }

    public record BuiltQuery(String whereClause, List<Object> parameters) {
    }

    public static BuiltQuery build(LogFilter filter) {
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (filter.playerName() != null && !filter.playerName().isBlank()) {
            clauses.add("player_name LIKE ?");
            params.add("%" + filter.playerName() + "%");
        }
        if (filter.playerUuid() != null && !filter.playerUuid().isBlank()) {
            clauses.add("player_uuid = ?");
            params.add(filter.playerUuid());
        }
        if (filter.material() != null && !filter.material().isBlank()) {
            if (filter.materialExact()) {
                clauses.add("material = ?");
                params.add(filter.material().toUpperCase());
            } else {
                clauses.add("material LIKE ?");
                params.add("%" + filter.material().toUpperCase() + "%");
            }
        }
        if (filter.actionType() != null && !filter.actionType().isBlank()) {
            clauses.add("action_type = ?");
            params.add(filter.actionType());
        }
        if (filter.world() != null && !filter.world().isBlank()) {
            clauses.add("world LIKE ?");
            params.add("%" + filter.world() + "%");
        }
        if (filter.eventHash() != null && !filter.eventHash().isBlank()) {
            clauses.add("event_hash = ?");
            params.add(filter.eventHash());
        }
        if (filter.fromTimestamp() != null) {
            clauses.add("timestamp >= ?");
            params.add(filter.fromTimestamp());
        }
        if (filter.toTimestamp() != null) {
            clauses.add("timestamp <= ?");
            params.add(filter.toTimestamp());
        }

        String where = clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
        return new BuiltQuery(where, params);
    }
}