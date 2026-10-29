package com.itemtrackerpro.database;

public record PlayerSummary(
        String uuid,
        String name,
        int eventCount,
        long lastSeen
) {
}