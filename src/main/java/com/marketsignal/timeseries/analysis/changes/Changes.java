package com.marketsignal.timeseries.analysis.changes;

import com.marketsignal.timeseries.analysis.Analysis;
import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.util.Format;
import com.marketsignal.util.Time;
import lombok.Builder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Changes {
    @Builder
    static public class Change {
        public double change;
        public double priceAtChange;
        public double priceChangedFrom;
        public long priceAtChangeEpochSeconds;
        public long priceChangedFromEpochSeconds;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Change.class)
                    .add("change", Format.ratioToPercent(change))
                    .add("priceAtChange", priceAtChange)
                    .add("priceChangedFrom", priceChangedFrom)
                    .add("priceAtChangeEpochSeconds", Time.fromEpochSecondsToDateTimeStr(priceAtChangeEpochSeconds))
                    .add("priceChangedFromEpochSeconds", Time.fromEpochSecondsToDateTimeStr(priceChangedFromEpochSeconds))
                    .toString();
        }
    }

    @Builder
    static public class AnalyzeResult extends Analysis {
        public String market;
        public String symbol;
        public double priceAtAnalysis;
        public long epochSecondsAtAnalysis;
        public Change minDrop;
        public Change maxJump;
        public double change;
        public AnalyzeParameter analyzeParameter;

        public AnalyzeResult(
                 String market, String symbol,
                 double priceAtAnalysis, long epochSecondsAtAnalysis,
                 Change minDrop,
                 Change maxJump,
                 double change,
                 AnalyzeParameter analyzeParameter) {
            this.market = market;
            this.symbol = symbol;
            this.priceAtAnalysis = priceAtAnalysis;
            this.epochSecondsAtAnalysis = epochSecondsAtAnalysis;
            this.minDrop = minDrop;
            this.maxJump = maxJump;
            this.change = change;
            this.analyzeParameter = analyzeParameter;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(AnalyzeResult.class)
                    .add("priceAtAnalysis", Format.truncatePrice(priceAtAnalysis))
                    .add("epochSecondsAtAnalysis", Time.fromEpochSecondsToDateTimeStr(epochSecondsAtAnalysis))
                    .add("datetime_str", Time.fromEpochSecondsToDateTimeStr(epochSecondsAtAnalysis))
                    .add("change", Format.ratioToPercent(change))
                    .add("minDrop", minDrop)
                    .add("maxJump", maxJump)
                    .add("analyzeParameter", analyzeParameter.toString())
                    .toString();
        }

        public List<String> getCsvHeaderColumns() {
            return Arrays.asList("priceAtAnalysis", "minDrop", "maxJump");
        }

        public List<String> getCsvValueColumns() {
            return Arrays.asList(String.format("%.2f", priceAtAnalysis), String.format("%.2f", minDrop), String.format("%.2f", maxJump));
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

    public static AnalyzeResult analyze(BarWithTimeSlidingWindow bwtSlidingWindow, AnalyzeParameter parameter) {
        if (bwtSlidingWindow.window.isEmpty()) {
            return AnalyzeResult.builder().build();
        }

        Double minPrice = null;
        Double maxPrice = null;
        Change maxJump = null;
        Change minDrop = null;
        long minPriceEpochSeconds = 0;
        long maxPriceEpochSeconds = 0;
        double change = 0;

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
            if (maxJump == null || jump > maxJump.change) {
                maxJump = Change.builder()
                        .change(jump)
                        .priceAtChange(bwtClose)
                        .priceAtChangeEpochSeconds(bwt.epochSeconds)
                        .priceChangedFrom(minPrice)
                        .priceChangedFromEpochSeconds(minPriceEpochSeconds)
                        .build();
            }

            if (maxPrice == null || bwtClose > maxPrice) {
                maxPrice = bwtClose;
                maxPriceEpochSeconds = bwt.epochSeconds;
            }
            double drop = (bwtClose - maxPrice) / maxPrice;
            if (minDrop == null || drop < minDrop.change) {
                minDrop = Change.builder()
                        .change(drop)
                        .priceAtChange(bwtClose)
                        .priceAtChangeEpochSeconds(bwt.epochSeconds)
                        .priceChangedFrom(maxPrice)
                        .priceChangedFromEpochSeconds(maxPriceEpochSeconds)
                        .build();
            }
        }

        double firstClose = bwtSlidingWindow.window.getFirst().bar.ohlc.close;
        double recentClose = bwtSlidingWindow.window.getLast().bar.ohlc.close;
        change = (recentClose - firstClose) / firstClose;

        return AnalyzeResult.builder()
                .market(bwtSlidingWindow.market)
                .symbol(bwtSlidingWindow.symbol)
                .priceAtAnalysis(recentClose)
                .epochSecondsAtAnalysis(bwtSlidingWindow.window.getLast().epochSeconds)
                .minDrop(minDrop)
                .maxJump(maxJump)
                .change(change)
                .analyzeParameter(parameter)
                .build();
    }
}
