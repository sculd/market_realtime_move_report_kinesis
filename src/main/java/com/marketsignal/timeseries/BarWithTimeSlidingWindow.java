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

    private void FillInZeroVolumeBwt(BarWithTime newBwt) {
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

    public boolean IsEpochSecondsInWindow(long epochSeconds) {
        long deltaSeconds = this.window.getLast().epochSeconds - epochSeconds;
        boolean withinLeft = deltaSeconds < this.windowSize.toSeconds();
        boolean withinRight = deltaSeconds >= 0;
        return withinLeft && withinRight;
    }

    private void TruncateSlidingWindowHead() {
        while (true) {
            if (this.window.isEmpty()) {
                break;
            }
            if (IsEpochSecondsInWindow(this.window.getFirst().epochSeconds)) {
                break;
            } else {
                this.window.removeFirst();
            }
        }
    }

    private void AppendOrAggregate(BarWithTime newBwt) {
        boolean aggregated = false;
        if (!this.window.isEmpty()) {
            if (this.window.getLast().epochSeconds == newBwt.epochSeconds) {
                this.window.getLast().bar.Aggregate(newBwt.bar);
                aggregated = true;
            }
        }
        if (!aggregated) {
            this.window.addLast(newBwt);
        }
    }

    public void AddBarWithTime(BarWithTime newBwt) {
        FillInZeroVolumeBwt(newBwt);
        AppendOrAggregate(newBwt);
        TruncateSlidingWindowHead();
    }
}
