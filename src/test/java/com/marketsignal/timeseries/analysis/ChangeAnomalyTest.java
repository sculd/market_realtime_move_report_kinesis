package com.marketsignal.timeseries.analysis;

import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ChangeAnomalyTest {
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

        ChangesAnomaly.AnalyzeParameter parameter = ChangesAnomaly.AnalyzeParameter.builder()
                .windowSizes(new ArrayList<>(List.of(Duration.ofMinutes(10))))
                .changeThresholds(new ArrayList<>(List.of(0.1)))
                .build();

        ChangesAnomaly.AnalyzeResult analysis = ChangesAnomaly.analyze(bwtSlidingWindow, parameter);

        assertEquals(1, analysis.anomalyResults.size());
        Changes.AnalyzeResult w = Changes.AnalyzeResult.builder()
                .minDrop(0.0).minDropEpochSeconds(660).maxPriceForMinDrop(50).maxPriceForMinDropEpochSeconds(660).priceAtMinDrop(50)
                .maxJump(3.0).maxJumpEpochSeconds(1200).minPriceForMaxJump(50).minPriceForMaxJumpEpochSeconds(660).priceAtMaxJump(200)
                .change(1.0)
                .build();
        assertThat(analysis.anomalyResults.get(0.1)).usingRecursiveComparison().isEqualTo(w);
    }
}
