package com.marketsignal.orderbook;

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

}
