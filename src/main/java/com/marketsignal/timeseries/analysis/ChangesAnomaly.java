package com.marketsignal.timeseries.analysis;

import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import lombok.Builder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangesAnomaly {
    @Builder
    static public class Anomaly {
        double changeThreshold;
        Changes.AnalyzeResult changeAnalysis;
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

    public static AnalyzeResult analyze(BarWithTimeSlidingWindow bwtSlidingWindow, AnalyzeParameter parameter) {
        AnalyzeResult ret = AnalyzeResult.builder().build();

        for (Duration windowSize : parameter.windowSizes) {
            Changes.AnalyzeParameter changeParameter = Changes.AnalyzeParameter.builder().windowSize(windowSize).build();

            Changes.AnalyzeResult analyzeResult = Changes.analyze(bwtSlidingWindow, changeParameter);
            for (Double changeThreshold : parameter.changeThresholds) {
                if (Math.abs(analyzeResult.minDrop) >= changeThreshold || analyzeResult.maxJump >= changeThreshold) {
                    ret.anomalies.add(Anomaly.builder().changeThreshold(changeThreshold).changeAnalysis(analyzeResult).build());
                }
            }
        }

        return ret;
    }
}
