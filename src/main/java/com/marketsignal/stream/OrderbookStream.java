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
    Duration timeSeriesResolution;
    Duration exportInterval;
    Map<String, OrderbookSlidingWindow> keyedOrderbookSlidingWindows = new HashMap<>();
    Map<String, Long> keyedExportAnchoredEpochSeconds = new HashMap<>();
    DynamoDbPublisher dynamoDbPublisher = new DynamoDbPublisher();

    public OrderbookStream(Duration windowSize, Duration timeSeriesResolution, Duration exportInterval) {
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
        this.exportInterval = exportInterval;
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

        if (!keyedExportAnchoredEpochSeconds.containsKey(key)) {
            keyedExportAnchoredEpochSeconds.put(key, Long.valueOf(0));
        }
        long prevAnochoredEpochSeconds = keyedExportAnchoredEpochSeconds.get(key);
        long anochoredEpochSeconds = orderbook.epochSeconds - (orderbook.epochSeconds % exportInterval.toSeconds());


        if (anochoredEpochSeconds > prevAnochoredEpochSeconds) {
            publishOrderFlowImbalanceAnomaly(keyedOrderbookSlidingWindows.get(key), orderbook);
            keyedExportAnchoredEpochSeconds.put(key, anochoredEpochSeconds);
        }
    }

    private void publishOrderFlowImbalanceAnomaly(OrderbookSlidingWindow orderbooksSlidingWindow, Orderbook orderbook) {
        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(10))
                .aggregationDuration(Duration.ofSeconds(10))
                .sampleDuration(Duration.ofSeconds(timeSeriesResolution.toSeconds()))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbooksSlidingWindow, parameter);
        dynamoDbPublisher.publish(analysis);
    }
}