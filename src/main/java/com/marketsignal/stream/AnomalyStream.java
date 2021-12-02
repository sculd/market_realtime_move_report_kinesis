package com.marketsignal.stream;

import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.ChangesAnomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AnomalyStream {
    MarketStream marketStream;

    private static final Logger log = LoggerFactory.getLogger(AnomalyStream.class);

    public AnomalyStream(MarketStream marketStream) {
        this.marketStream = marketStream;
    }

    public void onBarWithTime(BarWithTime bwt) {
        ChangesAnomaly.AnalyzeParameter parameter = ChangesAnomaly.AnalyzeParameter.builder()
                .windowSizes(new ArrayList<>(List.of(Duration.ofMinutes(20), Duration.ofMinutes(60), Duration.ofMinutes(360))))
                .changeThresholds(new ArrayList<>(List.of(0.05, 0.1, 0.2)))
                .build();

        String key = MarketStream.bwtToKeyString(bwt);
        ChangesAnomaly.AnalyzeResult analysis = ChangesAnomaly.analyze(marketStream.KeyedBarWithTimeSlidingWindows.get(key), parameter);
        for (ChangesAnomaly.Anomaly anomaly : analysis.anomalies) {
            log.info("{}", anomaly);
        }
    }
}
