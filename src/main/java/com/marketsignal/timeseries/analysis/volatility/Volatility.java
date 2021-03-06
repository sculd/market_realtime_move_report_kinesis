package com.marketsignal.timeseries.analysis.volatility;

import com.marketsignal.timeseries.analysis.Analysis;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import lombok.Builder;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Volatility {

    @Builder
    static public class WindowedVolatility {
        public double volatility;
        public Duration windowSize;
    }

    @Builder
    static public class AnalyzeResult extends Analysis {
        @Builder.Default
        List<WindowedVolatility> windowedVolatilities = new ArrayList<>();

        public List<String> getCsvHeaderColumns() {
            return windowedVolatilities.stream().map(wv -> String.format("volatility_%d", wv.windowSize.toMinutes())).collect(Collectors.toList());
        }

        public List<String> getCsvValueColumns() {
            return windowedVolatilities.stream().map(wv -> String.format("%.2f", wv.volatility)).collect(Collectors.toList());
        }
    }

    @Builder
    static public class AnalyzeParameter {
        public List<Duration> windowSizes;
    }

    public static AnalyzeResult analyze(BarWithTimeSlidingWindow bwtSlidingWindow, AnalyzeParameter analyzeParameter) {
        AnalyzeResult ret = AnalyzeResult.builder().build();
        if (bwtSlidingWindow.window.isEmpty()) {
            return ret;
        }

        StandardDeviation sd = new StandardDeviation();
        for (Duration windowSize : analyzeParameter.windowSizes) {
            List<BarWithTime> bwts = bwtSlidingWindow.window.stream().filter(bwt -> bwtSlidingWindow.isEpochSecondsInWindow(bwt.epochSeconds, windowSize))
                    .collect(Collectors.toList());

            WindowedVolatility windowedVolatility = WindowedVolatility.builder()
                    .volatility(sd.evaluate(bwts.stream().mapToDouble(bwt -> bwt.bar.ohlc.close).toArray()))
                    .windowSize(windowSize)
                    .build();
            ret.windowedVolatilities.add(windowedVolatility);
        }

        return ret;
    }
}
