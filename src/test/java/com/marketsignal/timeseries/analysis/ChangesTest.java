package com.marketsignal.timeseries.analysis;

import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import java.time.Duration;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ChangesTest {
    double equalDelta = 0.001;
    Bar CreateBar(double p) {
        return new Bar("dummy_market", "dummy_symbol", new OHLC(p, p, p, p), 1);
    }

    @Test
    public void testAnalyze() {
        BarWithTimeSlidingWindow bwtSlidingWindow = new BarWithTimeSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(20), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);

        for (int m = 0; m < 5; m++) {
            bwtSlidingWindow.addBarWithTime(new BarWithTime(CreateBar(100), 60 * m));
        }
        for (int m = 5; m < 15; m++) {
            bwtSlidingWindow.addBarWithTime(new BarWithTime(CreateBar(50), 60 * m));
        }
        for (int m = 15; m < 20; m++) {
            bwtSlidingWindow.addBarWithTime(new BarWithTime(CreateBar(100), 60 * m));
        }
        bwtSlidingWindow.addBarWithTime(new BarWithTime(CreateBar(200), 60 * 20));
        // at 0: 100, at 5: 50, at 15: 100, at 20: 200

        Changes.AnalyzeParameter parameter = new Changes.AnalyzeParameter(Duration.ofMinutes(20));
        Changes.AnalyzeResult analysis = Changes.analyze(bwtSlidingWindow, parameter);

        assertEquals(-0.5, analysis.minDrop, equalDelta);
        assertEquals(50, analysis.priceAtMinDrop, equalDelta);
        assertEquals(300, analysis.minDropEpochSeconds);

        assertEquals(3.0, analysis.maxJump, equalDelta);
        assertEquals(200, analysis.priceAtMaxJump, equalDelta);
        assertEquals(1200, analysis.maxJumpEpochSeconds);

        assertEquals(1.0, analysis.change, equalDelta);
    }
}
