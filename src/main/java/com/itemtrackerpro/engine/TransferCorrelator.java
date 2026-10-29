package com.itemtrackerpro.engine;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransferCorrelator {

    public record DropInfo(UUID playerUuid, String playerName, long timestamp) {}

    private final Map<UUID, DropInfo> recentDrops = new ConcurrentHashMap<>();
    private final long windowMs;

    public TransferCorrelator(long windowMs) {
        this.windowMs = windowMs;
    }

    public void registerDrop(UUID itemEntityId, UUID playerUuid, String playerName) {
        recentDrops.put(itemEntityId, new DropInfo(playerUuid, playerName, System.currentTimeMillis()));
    }

    /**
     * Consumes the drop record if the picker is a different player and the
     * pickup happened within the correlation window. Returns empty if this
     * is just the original dropper picking their own item back up, or if no
     * matching drop was registered.
     */
    public Optional<DropInfo> consumeIfTransfer(UUID itemEntityId, UUID pickerUuid) {
        DropInfo info = recentDrops.remove(itemEntityId);
        if (info == null) {
            return Optional.empty();
        }
        if (info.playerUuid().equals(pickerUuid)) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() - info.timestamp() > windowMs) {
            return Optional.empty();
        }
        return Optional.of(info);
    }

    public void cleanup() {
        long cutoff = System.currentTimeMillis() - windowMs;
        recentDrops.entrySet().removeIf(e -> e.getValue().timestamp() < cutoff);
    }
}