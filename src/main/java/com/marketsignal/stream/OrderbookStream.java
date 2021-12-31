package com.marketsignal.stream;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.orderbook.analysis.OrderFlowImbalance;
import com.marketsignal.publish.orderbookflowimbalance.DynamoDbPublisher;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class OrderbookStream {
    Duration windowSize;
    OrderbookSlidingWindow.TimeSeriesResolution timeSeriesResolution;
    Map<String, OrderbookSlidingWindow> keyedOrderbookSlidingWindows = new HashMap<>();
    DynamoDbPublisher dynamoDbPublisher = new DynamoDbPublisher();

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
        boolean sampledIn = keyedOrderbookSlidingWindows.get(key).addOrderbook(orderbook);

        if (sampledIn) {
            publishOrderFlowImbalanceAnomaly(keyedOrderbookSlidingWindows.get(key), orderbook);
        }
    }

    private void publishOrderFlowImbalanceAnomaly(OrderbookSlidingWindow orderbooksSlidingWindow, Orderbook orderbook) {
        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .flowDuration(Duration.ofMinutes(10))
                .sampleDuration(Duration.ofSeconds(timeSeriesResolution.seconds()))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbooksSlidingWindow, parameter);
        dynamoDbPublisher.publish(analysis);
    }
}