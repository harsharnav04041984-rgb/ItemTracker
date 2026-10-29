package com.itemtrackerpro.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimestampUtil {

    private TimestampUtil() {}

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static long now() {
        return Instant.now().toEpochMilli();
    }

    public static String format(long epochMillis) {
        return FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }
}