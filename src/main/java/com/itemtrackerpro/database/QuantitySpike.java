package com.itemtrackerpro.database;

public record QuantitySpike(
        String playerUuid,
        String playerName,
        String material,
        String actionType,
        int totalQuantity,
        int eventCount,
        long firstTimestamp,
        long lastTimestamp
) {
}