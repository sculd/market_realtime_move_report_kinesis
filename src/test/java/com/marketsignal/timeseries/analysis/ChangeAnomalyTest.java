package com.marketsignal.timeseries.analysis;

import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import com.marketsignal.timeseries.analysis.changes.Changes;
import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;
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

        ChangesAnomaly changesAnomaly = new ChangesAnomaly();
        ChangesAnomaly.AnalyzeResult analysis = changesAnomaly.analyze(bwtSlidingWindow, parameter);

        assertEquals(1, analysis.anomalies.size());
        Changes.AnalyzeResult changeAnalysisWanted = Changes.AnalyzeResult.builder()
                .market("dummy_market")
                .symbol("dummy_symbol")
                .minDrop(Changes.Change.builder().change(0.0).priceAtChange(50).priceAtChangeEpochSeconds(660).priceChangedFrom(50).priceChangedFromEpochSeconds(660).build())
                .maxJump(Changes.Change.builder().change(3.0).priceAtChange(200).priceAtChangeEpochSeconds(1200).priceChangedFrom(50).priceChangedFromEpochSeconds(660).build())
                .change(1.0)
                .epochSecondsAtAnalysis(20 * 60)
                .priceAtAnalysis(200)
                .analyzeParameter(Changes.AnalyzeParameter.builder().windowSize(Duration.ofMinutes(10)).build())
                .build();
        ChangesAnomaly.AnalyzeResult analysisWanted = ChangesAnomaly.AnalyzeResult.builder()
                        .anomalies(
                                new ArrayList<>(List.of(
                                        ChangesAnomaly.Anomaly.builder()
                                                .changeThreshold(0.1)
                                                .market("dummy_market")
                                                .symbol("dummy_symbol")
                                                .windowSize(Duration.ofMinutes(10))
                                                .changeAnalysis(changeAnalysisWanted)
                                                .build()))
                        ).build();

        assertThat(analysis).usingRecursiveComparison().isEqualTo(analysisWanted);
    }
}
