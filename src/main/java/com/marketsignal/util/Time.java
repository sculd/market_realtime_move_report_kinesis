package com.marketsignal.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Time {
    static private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String fromEpochSecondsToDateTimeStr(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        return ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String fromEpochSecondsToDateStr(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        return ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York")).format(DATE_TIME_FORMATTER);
    }
}
