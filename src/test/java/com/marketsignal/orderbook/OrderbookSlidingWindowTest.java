package com.marketsignal.orderbook;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class OrderbookSlidingWindowTest {
    @Test
    public void testAddOrderbookSampled() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), OrderbookSlidingWindow.TimeSeriesResolution.TEN_SECONDS);
        orderbookSlidingWindow.addOrderbook(new Orderbook("dummy_market", "dummy_symbol", 0));
        orderbookSlidingWindow.addOrderbook(new Orderbook("dummy_market", "dummy_symbol", 1));
        orderbookSlidingWindow.addOrderbook(new Orderbook("dummy_market", "dummy_symbol", 10));

        assertEquals(2, orderbookSlidingWindow.window.size());
    }

    @Test
    public void testAddOrderbookTruncated() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), OrderbookSlidingWindow.TimeSeriesResolution.TEN_SECONDS);
        orderbookSlidingWindow.addOrderbook(new Orderbook("dummy_market", "dummy_symbol", 0));
        // the first one truncated after adding the second.
        orderbookSlidingWindow.addOrderbook(new Orderbook("dummy_market", "dummy_symbol", 601));

        assertEquals(1, orderbookSlidingWindow.window.size());
    }
}
