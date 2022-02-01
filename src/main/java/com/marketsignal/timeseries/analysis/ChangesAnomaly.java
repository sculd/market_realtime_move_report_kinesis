package com.marketsignal.timeseries.analysis;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.util.Format;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangesAnomaly {

    private static final Logger log = LoggerFactory.getLogger(ChangesAnomaly.class);

    @Builder
    static public class Anomaly {
        public double changeThreshold;
        public String market;
        public String symbol;
        public Duration windowSize;
        public Changes.AnalyzeResult changeAnalysis;

        public String getChangeTypeStr() {
            if (Math.abs(changeAnalysis.minDrop) < changeThreshold && changeAnalysis.maxJump < changeThreshold) {
                return "";
            } else if (Math.abs(changeAnalysis.minDrop) < changeThreshold) {
                return "Jump";
            } else if (changeAnalysis.maxJump < changeThreshold) {
                return "Drop";
            } else {
                if (changeAnalysis.minDropEpochSeconds < changeAnalysis.maxJumpEpochSeconds) {
                    return "Drop->Jump";
                } else {
                    return "Jump->Drop";
                }
            }
        }

        public String getChangeSummaryStr() {
            return "";
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Anomaly.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("changeThreshold", Format.ratioToPercent(changeThreshold))
                    .add("changeTypeStr", getChangeTypeStr())
                    .add("changeAnalysis", changeAnalysis.toString())
                    .toString();
        }

        public String getThrottleKey() {
            return MoreObjects.toStringHelper(Anomaly.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("changeThreshold", changeThreshold)
                    .add("windowSize", windowSize.toString())
                    .add("changeType", getChangeTypeStr())
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
        public List<Double>  changeThresholds;

        public AnalyzeParameter(List<Duration> windowSizes, List<Double> changeThresholds) {
            this.windowSizes = windowSizes;
            this.changeThresholds = changeThresholds;
        }
    }

    Map<String, Long> prevAnomalyEpochSeconds = new HashMap<>();

    static public boolean isMinDropAnomaly(Changes.AnalyzeResult analyzeResult, double changeThreshold) {
        if (analyzeResult.minDrop > changeThreshold) {
            return false;
        }
        if (analyzeResult.epochSecondsAtAnalysis - analyzeResult.minDropEpochSeconds >= Duration.ofMinutes(1).toSeconds()) {
            return false;
        }
        return true;
    }

    static public boolean isMaxJumpAnomaly(Changes.AnalyzeResult analyzeResult, double changeThreshold) {
        if (analyzeResult.maxJump < changeThreshold) {
            return false;
        }
        if (analyzeResult.epochSecondsAtAnalysis - analyzeResult.maxJumpEpochSeconds >= Duration.ofMinutes(1).toSeconds()) {
            return false;
        }
        return true;
    }

    public AnalyzeResult analyze(BarWithTimeSlidingWindow bwtSlidingWindow, AnalyzeParameter parameter) {
        AnalyzeResult ret = AnalyzeResult.builder().build();

        for (Duration windowSize : parameter.windowSizes) {
            Changes.AnalyzeParameter changeParameter = Changes.AnalyzeParameter.builder().windowSize(windowSize).build();

            Changes.AnalyzeResult analyzeResult = Changes.analyze(bwtSlidingWindow, changeParameter);
            for (Double changeThreshold : parameter.changeThresholds) {
                if (!isMinDropAnomaly(analyzeResult, changeThreshold) && !isMaxJumpAnomaly(analyzeResult, changeThreshold)) {
                    continue;
                }
                Anomaly anomaly = Anomaly.builder()
                        .changeThreshold(changeThreshold)
                        .changeAnalysis(analyzeResult)
                        .market(bwtSlidingWindow.market)
                        .symbol(bwtSlidingWindow.symbol)
                        .windowSize(changeParameter.windowSize)
                        .build();

                long secondsSincePrev = anomaly.changeAnalysis.epochSecondsAtAnalysis -
                        prevAnomalyEpochSeconds.getOrDefault(anomaly.getThrottleKey(), Long.valueOf(0));
                if (secondsSincePrev < windowSize.toSeconds()) {
                    continue;
                }
                prevAnomalyEpochSeconds.put(anomaly.getThrottleKey(), anomaly.changeAnalysis.epochSecondsAtAnalysis);
                log.info("an anomaly found market: {}, symbol: {}, window: {}, threshold: {}",
                        bwtSlidingWindow.market, bwtSlidingWindow.symbol, windowSize, changeThreshold);
                ret.anomalies.add(anomaly);
            }
        }

        return ret;
    }
}
