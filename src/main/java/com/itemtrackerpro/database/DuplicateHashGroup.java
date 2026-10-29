package com.itemtrackerpro.database;

public record DuplicateHashGroup(
        String eventHash,
        int count,
        String material,
        String actionType,
        String playerName
) {
}