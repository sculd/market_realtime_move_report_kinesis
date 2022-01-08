package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class OrderFlowImbalanceTest {
    final double equalDelta = 0.001;

    @Test
    public void testAnalyzeEmptyWindow() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), Duration.ofSeconds(10));
        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(1))
                .sampleDuration(Duration.ofSeconds(10))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbookSlidingWindow, parameter);
        assertEquals(0, analysis.recentOrderFlowImbalance, equalDelta);
        assertEquals(0, analysis.orderFlowImbalanceStandardDeviationWithoutOutliers, equalDelta);
    }

    @Test
    public void testAnalyzeTwoSamples() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), Duration.ofSeconds(10));
        Orderbook orderbook = new Orderbook("dummy_market", "dummy_symbol", 0);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(150).volume(20).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 1);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(140).volume(15).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(110).volume(30).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 10);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(130).volume(5).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(120).volume(40).build());
        orderbookSlidingWindow.window.add(orderbook);

        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(1))
                .aggregationDuration(Duration.ofSeconds(10))
                .sampleDuration(Duration.ofSeconds(10))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbookSlidingWindow, parameter);
        // -5 (from ask) + 40 (from bid)
        assertEquals(35, analysis.recentOrderFlowImbalance, equalDelta);
        assertEquals(0, analysis.orderFlowImbalanceStandardDeviationWithoutOutliers, equalDelta);
    }

    @Test
    public void testAnalyzeThreeSamples() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), Duration.ofSeconds(10));
        Orderbook orderbook = new Orderbook("dummy_market", "dummy_symbol", 0);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(150).volume(20).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 10);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(130).volume(5).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(120).volume(40).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 20);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(130).volume(6).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(120).volume(66).build());
        orderbookSlidingWindow.window.add(orderbook);

        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(1))
                .aggregationDuration(Duration.ofSeconds(10))
                .sampleDuration(Duration.ofSeconds(10))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbookSlidingWindow, parameter);
        assertEquals(25, analysis.recentOrderFlowImbalance, equalDelta);
        // 0 to 10: 35, 10 to 20: 25
        assertEquals(30, analysis.orderFlowImbalanceAverage, equalDelta);
    }

    @Test
    public void testAnalyzeAggregation() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), Duration.ofSeconds(10));
        Orderbook orderbook = new Orderbook("dummy_market", "dummy_symbol", 0);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(200).volume(20).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(10).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 10);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(190).volume(20).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 20);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(180).volume(20).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(120).volume(10).build());
        orderbookSlidingWindow.window.add(orderbook);

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 30);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(170).volume(25).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(130).volume(5).build());
        orderbookSlidingWindow.window.add(orderbook);

        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(1))
                .aggregationDuration(Duration.ofSeconds(20))
                .sampleDuration(Duration.ofSeconds(10))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbookSlidingWindow, parameter);
        // 0 to 10: -10 (0 to 10: -20+10)
        // 0 to 20: -10 (0 to 10: -20+10) + -10 (10 to 20: -20+10) = -20
        // 10 to 30: -10 + -20(-25+5) = -30
        assertEquals(-30, analysis.recentOrderFlowImbalance, equalDelta);
        assertEquals(-20, analysis.orderFlowImbalanceAverage, equalDelta);
    }
}
