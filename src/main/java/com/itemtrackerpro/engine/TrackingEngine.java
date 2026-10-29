package com.itemtrackerpro.engine;

import com.itemtrackerpro.ItemTrackerPro;
import com.itemtrackerpro.config.ConfigManager;
import com.itemtrackerpro.database.DuplicateEventFilter;
import com.itemtrackerpro.database.EventRecord;
import com.itemtrackerpro.database.TrackedEventDAO;
import com.itemtrackerpro.model.TrackedEvent;
import com.itemtrackerpro.util.HashUtil;

import java.util.logging.Level;

public class TrackingEngine {

    private final ItemTrackerPro plugin;
    private final ConfigManager configManager;
    private final TrackedEventDAO dao;
    private final DuplicateEventFilter duplicateFilter;
    private final TransferCorrelator transferCorrelator;

    public TrackingEngine(ItemTrackerPro plugin, TrackedEventDAO dao,
                           DuplicateEventFilter duplicateFilter, TransferCorrelator transferCorrelator) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dao = dao;
        this.duplicateFilter = duplicateFilter;
        this.transferCorrelator = transferCorrelator;
    }

    public void track(TrackedEvent event) {
        if (event.actionType() == null || !event.actionType().isEnabled(configManager)) {
            return;
        }
        if (event.material() == null) {
            return;
        }

        String hash = buildHash(event);

        if (duplicateFilter.isDuplicate(hash)) {
            if (configManager.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Duplicate event filtered: " + hash);
            }
            return;
        }

        EventRecord record = EventRecord.forInsert(
                event.playerUuid().toString(),
                event.playerName(),
                event.actionType().name(),
                event.material().name(),
                event.quantity(),
                event.world(),
                event.x(),
                event.y(),
                event.z(),
                event.timestamp(),
                hash
        );

        dao.insertEventAsync(record).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist tracked event asynchronously", ex);
            return null;
        });

        if (configManager.isDebugEnabled()) {
            plugin.getLogger().info("[Debug] Tracked: " + event.actionType() + " " + event.material() +
                    " x" + event.quantity() + " by " + event.playerName());
        }
    }

    private String buildHash(TrackedEvent event) {
        // Bucket the timestamp to the duplicate window so near-simultaneous
        // duplicate firings collapse into the same hash, while genuinely
        // separate events (even identical in content) a bit later remain distinct.
        long bucket = event.timestamp() / Math.max(1, configManager.getDuplicateEventWindowMs());

        String raw = event.playerUuid() + "|" + event.actionType() + "|" + event.material() + "|" +
                event.quantity() + "|" + event.world() + "|" +
                event.x() + "|" + event.y() + "|" + event.z() + "|" + bucket;

        return HashUtil.sha256(raw);
    }

    public TransferCorrelator getTransferCorrelator() {
        return transferCorrelator;
    }
}