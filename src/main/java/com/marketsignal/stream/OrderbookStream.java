package com.marketsignal.stream;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.orderbook.analysis.OrderFlowImbalance;
import com.marketsignal.orderbook.analysis.LiquidityImbalance;
import com.marketsignal.orderbook.analysis.publish.orderbookflowimbalance.FlowImbalanceDynamoDbPublisher;
import com.marketsignal.orderbook.analysis.publish.orderbookliquidityimbalance.LiquidityDynamoDbPublisher;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class OrderbookStream {
    Duration windowSize;
    Duration timeSeriesResolution;
    Duration exportInterval;
    Map<String, OrderbookSlidingWindow> keyedOrderbookSlidingWindows = new HashMap<>();
    Map<String, Long> keyedExportAnchoredEpochSeconds = new HashMap<>();
    FlowImbalanceDynamoDbPublisher flowImbalanceDynamoDbPublisher = new FlowImbalanceDynamoDbPublisher();
    LiquidityDynamoDbPublisher liquidityDynamoDbPublisher = new LiquidityDynamoDbPublisher();

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
            //publishOrderFlowImbalance(keyedOrderbookSlidingWindows.get(key));
            //publishLiquidityImbalanceAnomaly(orderbook);
            keyedExportAnchoredEpochSeconds.put(key, anochoredEpochSeconds);
        }
    }

    private void publishOrderFlowImbalance(OrderbookSlidingWindow orderbooksSlidingWindow) {
        OrderFlowImbalance.Parameter parameter = OrderFlowImbalance.Parameter.builder()
                .windowDuration(Duration.ofMinutes(10))
                .aggregationDuration(Duration.ofSeconds(10))
                .sampleDuration(Duration.ofSeconds(timeSeriesResolution.toSeconds()))
                .build();

        OrderFlowImbalance.Analysis analysis = OrderFlowImbalance.analyze(orderbooksSlidingWindow, parameter);
        flowImbalanceDynamoDbPublisher.publish(analysis);
    }

    private void publishLiquidityImbalanceAnomaly(Orderbook orderbook) {
        LiquidityImbalance.Parameter parameter = LiquidityImbalance.Parameter.builder().build();
        LiquidityImbalance.Analysis analysis = LiquidityImbalance.analyze(orderbook, parameter);
        liquidityDynamoDbPublisher.publish(analysis);
    }
}