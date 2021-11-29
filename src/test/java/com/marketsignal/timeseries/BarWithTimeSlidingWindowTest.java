package com.marketsignal.timeseries;

import java.time.Duration;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;


public class BarWithTimeSlidingWindowTest {
    @Test
    public void testAddBarWithTime() {
        BarWithTimeSlidingWindow bwtSlidingWindow = new BarWithTimeSlidingWindow("dummy_market", "dummy_symbol", Duration.ofSeconds(300), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);
        Bar bar = new Bar("dummy_market", "dummy_symbol", new OHLC(10, 11, 9, 10), 100);
        BarWithTime bwt = new BarWithTime(bar, 1200);
        bwtSlidingWindow.AddBarWithTime(bwt);
        assertEquals(bwtSlidingWindow.window.size(), 5);
        assertEquals(bwtSlidingWindow.window.getFirst().bar.volume, 0, 0);
        assertThat(bwtSlidingWindow.window.getLast().bar).usingRecursiveComparison().isEqualTo(bar);

        bwt = new BarWithTime(bar, 1200 + 240);
        bwtSlidingWindow.AddBarWithTime(bwt);
        assertEquals(bwtSlidingWindow.window.size(), 5);
        assertThat(bwtSlidingWindow.window.getFirst().bar).usingRecursiveComparison().isEqualTo(bar);
        assertThat(bwtSlidingWindow.window.getLast().bar).usingRecursiveComparison().isEqualTo(bar);

        // new bwt on the same timestamp is to be aggregated.
        Bar secondBar = new Bar("dummy_market", "dummy_symbol", new OHLC(10, 12, 8, 11), 100);
        bwt = new BarWithTime(secondBar, 1200 + 240);
        bwtSlidingWindow.AddBarWithTime(bwt);
        assertEquals(bwtSlidingWindow.window.size(), 5);
        Bar aggregatedBar = new Bar(bar);
        aggregatedBar.Aggregate(secondBar);
        assertThat(bwtSlidingWindow.window.getLast().bar).usingRecursiveComparison().isEqualTo(aggregatedBar);
    }
}
