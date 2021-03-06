package com.marketsignal.stream;

import com.marketsignal.timeseries.analysis.changes.anomalypublish.Publisher;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ChangesAnomalyStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyStream.class);

    BarWithTimeStream barWithTimeStream;
    Publisher publisher = new Publisher();
    ChangesAnomaly changesAnomaly = new ChangesAnomaly();

    public ChangesAnomalyStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    public void onBarWithTime(BarWithTime bwt) {
        ChangesAnomaly.AnalyzeParameter parameter = ChangesAnomaly.AnalyzeParameter.builder()
                .windowSizes(new ArrayList<>(List.of(Duration.ofMinutes(20), Duration.ofMinutes(60), Duration.ofMinutes(360))))
                .changeThresholds(new ArrayList<>(List.of(0.05, 0.1, 0.2)))
                .build();

        String key = BarWithTimeStream.bwtToKeyString(bwt);
        ChangesAnomaly.AnalyzeResult analysis = changesAnomaly.analyze(barWithTimeStream.keyedBarWithTimeSlidingWindows.get(key), parameter);
        for (ChangesAnomaly.Anomaly anomaly : analysis.anomalies) {
            publisher.publish(anomaly);
        }
    }
}
