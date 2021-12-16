package com.marketsignal.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatTest {
    @Test
    public void testRatioToPercent() {
        assertThat(Format.ratioToPercent(0.1)).isEqualTo("10.0%");
        assertThat(Format.ratioToPercent(0.12)).isEqualTo("12.0%");
        assertThat(Format.ratioToPercent(0.123)).isEqualTo("12.3%");
        assertThat(Format.ratioToPercent(0.1234)).isEqualTo("12.3%");
        assertThat(Format.ratioToPercent(0.1236)).isEqualTo("12.4%");
    }

    @Test
    public void testTruncatePrice() {
        assertThat(Format.truncatePrice(1.0)).isEqualTo(1.0);
        assertThat(Format.truncatePrice(0.1)).isEqualTo(0.1);
        assertThat(Format.truncatePrice(0.12)).isEqualTo(0.1);
        assertThat(Format.truncatePrice(0.19)).isEqualTo(0.2);
        assertThat(Format.truncatePrice(123.44)).isEqualTo(123.4);
    }
}
