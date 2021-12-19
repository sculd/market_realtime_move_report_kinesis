package com.marketsignal.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeTest {
    @Test
    public void testFromEpochSecondsToDateTimeStr() {
        assertThat(Time.fromEpochSecondsToDateTimeStr(1639689000)).isEqualTo("2021-12-16T16:10:00-05:00");
        assertThat(Time.fromEpochSecondsToDateTimeStr(1639706400)).isEqualTo("2021-12-16T21:00:00-05:00");
    }

    @Test
    public void testFromEpochSecondsToDateStr() {
        assertThat(Time.fromEpochSecondsToDateStr(1639689000)).isEqualTo("2021-12-16");
        assertThat(Time.fromEpochSecondsToDateStr(1639706400)).isEqualTo("2021-12-16");
    }
}
