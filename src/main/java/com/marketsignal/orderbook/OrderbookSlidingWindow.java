package com.marketsignal.orderbook;

import com.marketsignal.timeseries.BarWithTime;

import java.time.Duration;
import java.util.ArrayDeque;

public class OrderbookSlidingWindow {
    public String market;
    public String symbol;
    public ArrayDeque<Orderbook> window;
    public Duration windowSize;

    public enum TimeSeriesResolution {
        MINUTE (60),
        TEN_SECONDS (10),
        SECOND (1)
        ;

        private final long seconds;
        TimeSeriesResolution(long seconds) {
            this.seconds = seconds;
        }
        private long seconds() { return seconds; }
    }
    public TimeSeriesResolution timeSeriesResolution;

    public OrderbookSlidingWindow(String market, String symbol, Duration windowSize, TimeSeriesResolution timeSeriesResolution) {
        this.market = market;
        this.symbol = symbol;
        this.window = new ArrayDeque<Orderbook>();
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
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

    public void addOrderbook(Orderbook orderbook) {
        this.window.addLast(orderbook);
        truncateSlidingWindowHead();
    }

    public long getLatestEpochSeconds() {
        if (window.isEmpty()) {
            return 0;
        }
        return window.getLast().epochSeconds;
    }
}
