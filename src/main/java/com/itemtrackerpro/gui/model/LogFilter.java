package com.itemtrackerpro.gui.model;

public record LogFilter(
        String playerName,
        String playerUuid,
        String material,
        boolean materialExact,
        String actionType,
        String world,
        String eventHash,
        Long fromTimestamp,
        Long toTimestamp
) {
    public static LogFilter empty() {
        return new LogFilter(null, null, null, false, null, null, null, null, null);
    }

    public LogFilter withPlayerName(String value) {
        return new LogFilter(value, playerUuid, material, materialExact, actionType, world, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withPlayerUuid(String value) {
        return new LogFilter(playerName, value, material, materialExact, actionType, world, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withMaterial(String value) {
        return new LogFilter(playerName, playerUuid, value, false, actionType, world, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withMaterialExact(String value) {
        return new LogFilter(playerName, playerUuid, value, true, actionType, world, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withActionType(String value) {
        return new LogFilter(playerName, playerUuid, material, materialExact, value, world, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withWorld(String value) {
        return new LogFilter(playerName, playerUuid, material, materialExact, actionType, value, eventHash, fromTimestamp, toTimestamp);
    }

    public LogFilter withEventHash(String value) {
        return new LogFilter(playerName, playerUuid, material, materialExact, actionType, world, value, fromTimestamp, toTimestamp);
    }

    public LogFilter withDateRange(Long from, Long to) {
        return new LogFilter(playerName, playerUuid, material, materialExact, actionType, world, eventHash, from, to);
    }

    public boolean isEmpty() {
        return playerName == null && playerUuid == null && material == null
                && actionType == null && world == null && eventHash == null
                && fromTimestamp == null && toTimestamp == null;
    }
}