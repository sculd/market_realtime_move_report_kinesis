package com.marketsignal.stream;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class OrderbookStream {
    Duration windowSize;
    OrderbookSlidingWindow.TimeSeriesResolution timeSeriesResolution;
    Map<String, OrderbookSlidingWindow> keyedOrderbookSlidingWindows = new HashMap<>();

    public OrderbookStream(Duration windowSize, OrderbookSlidingWindow.TimeSeriesResolution timeSeriesResolution) {
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
    }

    static String orderbookToKeyString(Orderbook orderbook) {
        return String.format("%s.%s", orderbook.market, orderbook.symbol);
    }

    public void onOrderbook(Orderbook orderbook) {
        String key = orderbookToKeyString(orderbook);
        if (!keyedOrderbookSlidingWindows.containsKey(key)) {
            keyedOrderbookSlidingWindows.put(key, new OrderbookSlidingWindow(orderbook.market, orderbook.symbol, this.windowSize, this.timeSeriesResolution));
        }
        keyedOrderbookSlidingWindows.get(key).addOrderbook(orderbook);
    }
}