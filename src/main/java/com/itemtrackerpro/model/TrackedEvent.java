package com.itemtrackerpro.model;

import com.itemtrackerpro.util.TimestampUtil;
import org.bukkit.Material;

import java.util.UUID;

public record TrackedEvent(
        UUID playerUuid,
        String playerName,
        ActionType actionType,
        Material material,
        int quantity,
        String world,
        Integer x,
        Integer y,
        Integer z,
        long timestamp
) {
    public static TrackedEvent now(UUID playerUuid, String playerName, ActionType actionType,
                                    Material material, int quantity, String world,
                                    Integer x, Integer y, Integer z) {
        return new TrackedEvent(playerUuid, playerName, actionType, material, quantity,
                world, x, y, z, TimestampUtil.now());
    }
}