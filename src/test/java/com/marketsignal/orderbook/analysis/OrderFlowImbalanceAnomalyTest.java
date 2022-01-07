package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderFlowImbalanceAnomalyTest {
    @Test
    public void testAnalyzeEmptyWindow() {
        OrderbookSlidingWindow orderbookSlidingWindow = new OrderbookSlidingWindow("dummy_market", "dummy_symbol", Duration.ofMinutes(10), Duration.ofSeconds(10));

        OrderFlowImbalanceAnomaly orderFlowImbalanceAnomaly = new OrderFlowImbalanceAnomaly();
        OrderFlowImbalanceAnomaly.AnalyzeParameter parameter = OrderFlowImbalanceAnomaly.AnalyzeParameter.builder()
                .windowSizes(List.of(Duration.ofMinutes(10)))
                .sampleDurations(List.of(Duration.ofSeconds(10)))
                .thresholds(List.of(Double.valueOf(2)))
                .build();

        OrderFlowImbalanceAnomaly.AnalyzeResult result = orderFlowImbalanceAnomaly.analyze(orderbookSlidingWindow, parameter);
        assertEquals(0, result.anomalies.size());
    }

    @Test
    public void testAnalyzeNoAnomaly() {
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

        OrderFlowImbalanceAnomaly orderFlowImbalanceAnomaly = new OrderFlowImbalanceAnomaly();
        OrderFlowImbalanceAnomaly.AnalyzeParameter parameter = OrderFlowImbalanceAnomaly.AnalyzeParameter.builder()
                .windowSizes(List.of(Duration.ofMinutes(10)))
                .sampleDurations(List.of(Duration.ofSeconds(10)))
                .thresholds(List.of(Double.valueOf(2)))
                .build();

        OrderFlowImbalanceAnomaly.AnalyzeResult result = orderFlowImbalanceAnomaly.analyze(orderbookSlidingWindow, parameter);
        assertEquals(0, result.anomalies.size());
    }

    @Test
    public void testAnalyzeWithAnomaly() {
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

        orderbook = new Orderbook("dummy_market", "dummy_symbol", 30);
        orderbook.asks.quotes.add(Orderbook.Quote.builder().price(150).volume(100).build());
        orderbook.bids.quotes.add(Orderbook.Quote.builder().price(130).volume(100).build());
        orderbookSlidingWindow.window.add(orderbook);

        OrderFlowImbalanceAnomaly orderFlowImbalanceAnomaly = new OrderFlowImbalanceAnomaly();
        OrderFlowImbalanceAnomaly.AnalyzeParameter parameter = OrderFlowImbalanceAnomaly.AnalyzeParameter.builder()
                .windowSizes(List.of(Duration.ofMinutes(10)))
                .sampleDurations(List.of(Duration.ofSeconds(10)))
                .thresholds(List.of(Double.valueOf(2), Double.valueOf(5)))
                .build();

        OrderFlowImbalanceAnomaly.AnalyzeResult result = orderFlowImbalanceAnomaly.analyze(orderbookSlidingWindow, parameter);
        assertEquals(2, result.anomalies.size());
    }
}
