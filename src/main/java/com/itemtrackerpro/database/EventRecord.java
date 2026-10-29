package com.itemtrackerpro.database;

public record EventRecord(
        long id,
        String playerUuid,
        String playerName,
        String actionType,
        String material,
        int quantity,
        String world,
        Integer x,
        Integer y,
        Integer z,
        long timestamp,
        String eventHash
) {
    /**
     * Factory for building a record prior to insertion, where the database
     * will generate the real ID.
     */
    public static EventRecord forInsert(String playerUuid, String playerName, String actionType,
                                         String material, int quantity, String world,
                                         Integer x, Integer y, Integer z,
                                         long timestamp, String eventHash) {
        return new EventRecord(-1L, playerUuid, playerName, actionType, material,
                quantity, world, x, y, z, timestamp, eventHash);
    }
}