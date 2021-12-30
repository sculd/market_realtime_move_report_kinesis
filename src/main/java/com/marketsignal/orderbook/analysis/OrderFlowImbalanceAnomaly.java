package com.marketsignal.orderbook.analysis;

import com.google.common.base.MoreObjects;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.util.Format;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFlowImbalanceAnomaly {
    private static final Logger log = LoggerFactory.getLogger(OrderFlowImbalanceAnomaly.class);

    @Builder
    static public class Anomaly {
        public double threshold;
        public String market;
        public String symbol;
        public OrderFlowImbalance.Analysis orderFlowImbalanceAnalysis;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Anomaly.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("threshold", Format.ratioToPercent(threshold))
                    .toString();
        }

        public String getThrottleKey() {
            return MoreObjects.toStringHelper(Anomaly.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("threshold", threshold)
                    .toString();
        }
    }

    @Builder
    static public class AnalyzeResult {
        @Builder.Default
        public List<Anomaly> anomalies = new ArrayList<>();
    }

    @Builder
    static public class AnalyzeParameter {
        public List<Duration> windowSizes;
        public List<Duration> sampleDurations;
        public List<Double>  thresholds;

        public AnalyzeParameter(List<Duration> windowSizes, List<Duration> sampleDurations, List<Double> thresholds) {
            this.windowSizes = windowSizes;
            this.sampleDurations = sampleDurations;
            this.thresholds = thresholds;
        }
    }

    Map<String, Long> prevAnomalyEpochSeconds = new HashMap<>();

    public AnalyzeResult analyze(OrderbookSlidingWindow slidingWindow, AnalyzeParameter parameter) {
        AnalyzeResult ret = AnalyzeResult.builder().build();

        for (Duration windowSize : parameter.windowSizes) {
            for (Duration sampleDuration : parameter.sampleDurations) {
                OrderFlowImbalance.Parameter orderFlowImbalanceAnalysisParameter = OrderFlowImbalance.Parameter.builder()
                        .flowDuration(windowSize)
                        .sampleDuration(sampleDuration)
                        .build();

                OrderFlowImbalance.Analysis imbalanceAnalyzeResult = OrderFlowImbalance.analyze(slidingWindow, orderFlowImbalanceAnalysisParameter);
                for (Double threshold : parameter.thresholds) {
                    if (Math.abs(imbalanceAnalyzeResult.recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers) < threshold) {
                        continue;
                    }
                    Anomaly anomaly = Anomaly.builder()
                            .threshold(threshold)
                            .orderFlowImbalanceAnalysis(imbalanceAnalyzeResult)
                            .market(slidingWindow.market)
                            .symbol(slidingWindow.symbol)
                            .orderFlowImbalanceAnalysis(imbalanceAnalyzeResult)
                            .build();

                    long prevAnomalyEpochSecond = prevAnomalyEpochSeconds.getOrDefault(anomaly.getThrottleKey(), Long.valueOf(-1));
                    long secondsSincePrev = anomaly.orderFlowImbalanceAnalysis.epochSeconds - prevAnomalyEpochSecond;

                    if (prevAnomalyEpochSecond >= 0 && secondsSincePrev < windowSize.toSeconds()) {
                        continue;
                    }
                    prevAnomalyEpochSeconds.put(anomaly.getThrottleKey(), anomaly.orderFlowImbalanceAnalysis.epochSeconds);
                    log.info("an anomaly found market: {}, symbol: {}, window: {}, threshold: {}",
                            slidingWindow.market, slidingWindow.symbol, windowSize, threshold);
                    ret.anomalies.add(anomaly);
                }
            }
        }

        return ret;
    }
}
