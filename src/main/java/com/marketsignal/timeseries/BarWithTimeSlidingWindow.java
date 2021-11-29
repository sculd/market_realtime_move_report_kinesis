package com.marketsignal.timeseries;

import java.time.Duration;
import java.util.ArrayDeque;

public class BarWithTimeSlidingWindow {
    public String market;
    public String symbol;
    public ArrayDeque<BarWithTime> window;
    public Duration windowSize;

    public enum TimeSeriesResolution {
        MINUTE (60);

        private final long seconds;
        TimeSeriesResolution(long seconds) {
            this.seconds = seconds;
        }
        private long seconds() { return seconds; }
    }
    public TimeSeriesResolution timeSeriesResolution;

    public BarWithTimeSlidingWindow(String market, String symbol, Duration windowSize, TimeSeriesResolution timeSeriesResolution) {
        this.market = market;
        this.symbol = symbol;
        this.window = new ArrayDeque<BarWithTime>();
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
    }

    private void fillInZeroVolumeBwt(BarWithTime newBwt) {
        long latestEpochSecondsInWindow = newBwt.epochSeconds - this.windowSize.toSeconds();
        OHLC latestOhlc = new OHLC(newBwt.bar.ohlc);
        if (!this.window.isEmpty()) {
            latestEpochSecondsInWindow = this.window.getLast().epochSeconds;
            latestOhlc = new OHLC(this.window.getLast().bar.ohlc);
        }
        while (true) {
            if (latestEpochSecondsInWindow >= newBwt.epochSeconds - timeSeriesResolution.seconds()) {
                break;
            }
            BarWithTime dummyBwt = new BarWithTime(new Bar(this.market, this.symbol, latestOhlc, 0), latestEpochSecondsInWindow + timeSeriesResolution.seconds());
            this.window.addLast(dummyBwt);
            latestEpochSecondsInWindow = dummyBwt.epochSeconds;
        }
    }

    public boolean isEpochSecondsInWindow(long epochSeconds, Duration windowSize) {
        long deltaSeconds = this.window.getLast().epochSeconds - epochSeconds;
        boolean withinLeft = deltaSeconds < windowSize.toSeconds();
        boolean withinRight = deltaSeconds >= 0;
        return withinLeft && withinRight;
    }

    boolean isEpochSecondsInWindow(long epochSeconds) {
        return isEpochSecondsInWindow(epochSeconds, this.windowSize);
    }

    private void truncateSlidingWindowHead() {
        while (true) {
            if (this.window.isEmpty()) {
                break;
            }
            if (isEpochSecondsInWindow(this.window.getFirst().epochSeconds)) {
                break;
            } else {
                this.window.removeFirst();
            }
        }
    }

    private void appendOrAggregate(BarWithTime newBwt) {
        boolean aggregated = false;
        if (!this.window.isEmpty()) {
            if (this.window.getLast().epochSeconds == newBwt.epochSeconds) {
                this.window.getLast().bar.aggregate(newBwt.bar);
                aggregated = true;
            }
        }
        if (!aggregated) {
            this.window.addLast(newBwt);
        }
    }

    public void addBarWithTime(BarWithTime newBwt) {
        fillInZeroVolumeBwt(newBwt);
        appendOrAggregate(newBwt);
        truncateSlidingWindowHead();
    }
}
