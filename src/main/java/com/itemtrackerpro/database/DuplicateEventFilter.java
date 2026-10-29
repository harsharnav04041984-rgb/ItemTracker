package com.itemtrackerpro.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DuplicateEventFilter {

    private final Map<String, Long> recentEvents = new ConcurrentHashMap<>();
    private final long windowMs;

    public DuplicateEventFilter(long windowMs) {
        this.windowMs = windowMs;
    }

    /**
     * Registers this event hash and reports whether an identical hash was
     * already seen within the configured time window.
     */
    public boolean isDuplicate(String eventHash) {
        long now = System.currentTimeMillis();
        Long last = recentEvents.put(eventHash, now);
        return last != null && (now - last) < windowMs;
    }

    /** Periodically called to purge stale entries and prevent unbounded memory growth. */
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - windowMs;
        recentEvents.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    public int size() {
        return recentEvents.size();
    }
}