package com.marketsignal.timeseries.analysis;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.util.Format;
import com.marketsignal.util.Time;
import lombok.Builder;

import java.time.Duration;

public class Changes {
    @Builder
    static public class AnalyzeResult {
        public double priceAtAnalysis;
        public long epochSecondsAtAnalysis;
        public double minDrop;
        public double priceAtMinDrop;
        public long minDropEpochSeconds;
        public double maxPriceForMinDrop;
        public long maxPriceForMinDropEpochSeconds;
        public double maxJump;
        public double priceAtMaxJump;
        public long maxJumpEpochSeconds;
        public double minPriceForMaxJump;
        public long minPriceForMaxJumpEpochSeconds;
        public double change;
        public AnalyzeParameter analyzeParameter;

        public AnalyzeResult(
                 double priceAtAnalysis, long epochSecondsAtAnalysis,
                 double minDrop, double priceAtMinDrop, long minDropEpochSeconds,
                 double maxPriceForMinDrop, long maxPriceForMinDropEpochSeconds,
                 double maxJump, double priceAtMaxJump, long maxJumpEpochSeconds,
                 double minPriceForMaxJump, long minPriceForMaxJumpEpochSeconds,
                 double change,
         AnalyzeParameter analyzeParameter) {
            this.priceAtAnalysis = priceAtAnalysis;
            this.epochSecondsAtAnalysis = epochSecondsAtAnalysis;
            this.minDrop = minDrop;
            this.priceAtMinDrop = priceAtMinDrop;
            this.minDropEpochSeconds = minDropEpochSeconds;
            this.maxPriceForMinDrop = maxPriceForMinDrop;
            this.maxPriceForMinDropEpochSeconds = maxPriceForMinDropEpochSeconds;
            this.maxJump = maxJump;
            this.priceAtMaxJump = priceAtMaxJump;
            this.maxJumpEpochSeconds = maxJumpEpochSeconds;
            this.minPriceForMaxJump = minPriceForMaxJump;
            this.minPriceForMaxJumpEpochSeconds = minPriceForMaxJumpEpochSeconds;
            this.change = change;
            this.analyzeParameter = analyzeParameter;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(AnalyzeResult.class)
                    .add("priceAtAnalysis", Format.truncatePrice(priceAtAnalysis))
                    .add("epochSecondsAtAnalysis", Time.fromEpochSecondsToDateTimeStr(epochSecondsAtAnalysis))
                    .add("datetime_str", Time.fromEpochSecondsToDateTimeStr(epochSecondsAtAnalysis))
                    .add("minDrop", Format.ratioToPercent(minDrop))
                    .add("priceAtMinDrop", Format.truncatePrice(priceAtMinDrop))
                    .add("minDropEpochSeconds", Time.fromEpochSecondsToDateTimeStr(minDropEpochSeconds))
                    .add("maxPriceForMinDrop", Format.truncatePrice(maxPriceForMinDrop))
                    .add("maxPriceForMinDropEpochSeconds", Time.fromEpochSecondsToDateTimeStr(maxPriceForMinDropEpochSeconds))
                    .add("maxJump", Format.ratioToPercent(maxJump))
                    .add("priceAtMaxJump", Format.truncatePrice(priceAtMaxJump))
                    .add("maxJumpEpochSeconds", Time.fromEpochSecondsToDateTimeStr(maxJumpEpochSeconds))
                    .add("minPriceForMaxJump", Format.truncatePrice(minPriceForMaxJump))
                    .add("minPriceForMaxJumpEpochSeconds", Time.fromEpochSecondsToDateTimeStr(minPriceForMaxJumpEpochSeconds))
                    .add("change", Format.ratioToPercent(change))
                    .add("analyzeParameter", analyzeParameter.toString())
                    .toString();
        }
    }

    @Builder
    static public class AnalyzeParameter {
        public Duration windowSize;

        public AnalyzeParameter(Duration windowSize) {
            this.windowSize = windowSize;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(AnalyzeParameter.class)
                    .add("windowSize", windowSize)
                    .toString();
        }
    }

    static AnalyzeResult analyze(BarWithTimeSlidingWindow bwtSlidingWindow, AnalyzeParameter parameter) {
        if (bwtSlidingWindow.window.isEmpty()) {
            return AnalyzeResult.builder().build();
        }

        Double minPrice = null;
        Double maxPrice = null;
        long minPriceEpochSeconds = 0;
        long maxPriceEpochSeconds = 0;
        Double minDrop = null;
        Double maxJump = null;
        double change = 0;
        double priceAtMaxJump = 0;
        double priceAtMinDrop = 0;
        double maxPriceForMinDrop = 0;
        double minPriceForMaxJump = 0;
        long maxJumpEpochSeconds = 0;
        long minDropEpochSeconds = 0;
        long maxPriceForMinDropEpochSeconds = 0;
        long minPriceForMaxJumpEpochSeconds = 0;

        for (BarWithTime bwt : bwtSlidingWindow.window) {
            if (!bwtSlidingWindow.isEpochSecondsInWindow(bwt.epochSeconds, parameter.windowSize)) {
                continue;
            }

            double bwtClose = bwt.bar.ohlc.close;
            if (minPrice == null || bwtClose < minPrice) {
                minPrice = bwtClose;
                minPriceEpochSeconds = bwt.epochSeconds;
            }
            double jump = (bwtClose - minPrice) / minPrice;
            if (maxJump == null || jump > maxJump) {
                maxJump = jump;
                priceAtMaxJump = bwtClose;
                maxJumpEpochSeconds = bwt.epochSeconds;
                minPriceForMaxJump = minPrice;
                minPriceForMaxJumpEpochSeconds = minPriceEpochSeconds;
            }

            if (maxPrice == null || bwtClose > maxPrice) {
                maxPrice = bwtClose;
                maxPriceEpochSeconds = bwt.epochSeconds;
            }
            double drop = (bwtClose - maxPrice) / maxPrice;
            if (minDrop == null || drop < minDrop) {
                minDrop = drop;
                priceAtMinDrop = bwtClose;
                minDropEpochSeconds = bwt.epochSeconds;
                maxPriceForMinDrop = maxPrice;
                maxPriceForMinDropEpochSeconds = maxPriceEpochSeconds;
            }
        }

        double firstClose = bwtSlidingWindow.window.getFirst().bar.ohlc.close;
        double recentClose = bwtSlidingWindow.window.getLast().bar.ohlc.close;
        change = (recentClose - firstClose) / firstClose;

        return AnalyzeResult.builder()
                .priceAtAnalysis(recentClose)
                .epochSecondsAtAnalysis(bwtSlidingWindow.window.getLast().epochSeconds)
                .minDrop(minDrop).priceAtMinDrop(priceAtMinDrop).minDropEpochSeconds(minDropEpochSeconds).maxPriceForMinDrop(maxPriceForMinDrop).maxPriceForMinDropEpochSeconds(maxPriceForMinDropEpochSeconds)
                .maxJump(maxJump).priceAtMaxJump(priceAtMaxJump).maxJumpEpochSeconds(maxJumpEpochSeconds).minPriceForMaxJump(minPriceForMaxJump).minPriceForMaxJumpEpochSeconds(minPriceForMaxJumpEpochSeconds)
                .change(change)
                .analyzeParameter(parameter)
                .build();
    }
}
