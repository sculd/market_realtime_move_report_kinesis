package com.marketsignal.stream;

import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.orderbook.analysis.OrderFlowImbalanceAnomaly;
import com.marketsignal.orderbook.analysis.publish.orderbookflowimbalanceanomaly.FlowImbalanceAnomalyDynamoDbPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class OrderbookAnomalyStream {
    private static final Logger log = LoggerFactory.getLogger(OrderbookAnomalyStream.class);

    OrderbookStream orderbookStream;
    OrderFlowImbalanceAnomaly orderFlowImbalanceAnomaly = new OrderFlowImbalanceAnomaly();
    FlowImbalanceAnomalyDynamoDbPublisher flowImbalanceAnomalyDynamoDbPublisher = new FlowImbalanceAnomalyDynamoDbPublisher();

    public OrderbookAnomalyStream(OrderbookStream orderbookStream) {
        this.orderbookStream = orderbookStream;
    }

    public void onOrderbook(Orderbook orderbook) {
        String key = OrderbookStream.orderbookToKeyString(orderbook);
        OrderbookSlidingWindow orderbookSlidingWindow = orderbookStream.keyedOrderbookSlidingWindows.get(key);

        OrderFlowImbalanceAnomaly.AnalyzeParameter parameter = OrderFlowImbalanceAnomaly.AnalyzeParameter.builder()
                .windowSizes(List.of(Duration.ofMinutes(20), Duration.ofMinutes(60), Duration.ofMinutes(360)))
                .aggregationDurations(List.of(Duration.ofSeconds(10)))
                .sampleDurations(List.of(Duration.ofSeconds(orderbookSlidingWindow.timeSeriesResolution.toSeconds())))
                .thresholds(List.of(10.0, 50.0))
                .build();

        OrderFlowImbalanceAnomaly.AnalyzeResult analysis = orderFlowImbalanceAnomaly.analyze(orderbookStream.keyedOrderbookSlidingWindows.get(key), parameter);
        if (!analysis.anomalies.isEmpty()) {
            //flowImbalanceAnomalyDynamoDbPublisher.publish(analysis);
        }
    }
}
