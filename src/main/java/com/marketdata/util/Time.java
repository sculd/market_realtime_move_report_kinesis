package com.marketdata.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Time {
    public static long fromNewYorkDateTimeInfoToEpochSeconds(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date t = cal.getTime();
        return (long)(t.getTime() / 1000.);
    }
}
