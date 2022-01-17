package com.marketsignal.orderbook.analysis.publish.orderbookflowimbalanceanomaly;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.marketsignal.orderbook.analysis.OrderFlowImbalanceAnomaly;
import com.marketsignal.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FlowImbalanceAnomalyDynamoDbPublisher {
    private static final Logger log = LoggerFactory.getLogger(FlowImbalanceAnomalyDynamoDbPublisher.class);

    private final String tableName = "order_flow_imbalance_anomaly";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);

    public void publish(OrderFlowImbalanceAnomaly.AnalyzeResult anomalyAnalysis) {
        log.info("[DynamoDbPublisher] publishing {} orderFlowImbalanceAnalysises", anomalyAnalysis.anomalies.size());
        try {
            TableWriteItems tableWriteItems = new TableWriteItems(tableName);
            for (OrderFlowImbalanceAnomaly.Anomaly anomaly : anomalyAnalysis.anomalies) {
                tableWriteItems.addItemToPut(
                        new Item().withPrimaryKey("market_symbol_threshold", String.format("%s.%s.%s", anomaly.market, anomaly.symbol, anomaly.threshold), "timestamp", anomaly.orderFlowImbalanceAnalysis.epochSeconds)
                                .withString("date_et", Time.fromEpochSecondsToDateStr(anomaly.orderFlowImbalanceAnalysis.epochSeconds))
                                .withString("datetime_et", Time.fromEpochSecondsToDateTimeStr(anomaly.orderFlowImbalanceAnalysis.epochSeconds))
                                .withString("datetime_recorded", Time.fromEpochSecondsToDateTimeStr(java.time.Instant.now().getEpochSecond()))
                                .with("recordDelaySeconds", java.time.Instant.now().getEpochSecond() - anomaly.orderFlowImbalanceAnalysis.epochSeconds)
                                .with("bidPrice", anomaly.orderFlowImbalanceAnalysis.bidPrice)
                                .with("askPrice", anomaly.orderFlowImbalanceAnalysis.askPrice)
                                .with("recentOrderFlowImbalance", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                                .with("orderFlowImbalanceAverage", anomaly.orderFlowImbalanceAnalysis.orderFlowImbalanceAverage)
                                .with("orderFlowImbalanceMedian", anomaly.orderFlowImbalanceAnalysis.orderFlowImbalanceMedian)
                                .with("orderFlowImbalanceStandardDeviationWithoutOutliers", anomaly.orderFlowImbalanceAnalysis.orderFlowImbalanceStandardDeviationWithoutOutliers)
                                .with("recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers)
                                .with("recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers)
                                .with("recentOrderFlowImbalance", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                                .with("flowDurationSeconds", anomaly.orderFlowImbalanceAnalysis.parameter.windowDuration.toSeconds())
                                .with("sampleDurationSeconds", anomaly.orderFlowImbalanceAnalysis.parameter.sampleDuration.toSeconds()));
            }

            BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);
            do {
                Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();
                if (outcome.getUnprocessedItems().size() == 0) {
                    log.info("No unprocessed items found");
                }
                else {
                    log.info("Retrieving the unprocessed items");
                    outcome = dynamoDB.batchWriteItemUnprocessed(unprocessedItems);
                }
            } while (outcome.getUnprocessedItems().size() > 0);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
