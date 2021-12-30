package com.marketsignal.publish.orderbookflowimbalanceanomaly;

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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DynamoDbPublisher {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbPublisher.class);

    private final String tableName = "order_flow_imbalance_anomaly";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void publish(OrderFlowImbalanceAnomaly.AnalyzeResult anomalyAnalysis) {
        log.info("[DynamoDbPublisher] publishing {} orderFlowImbalanceAnalysises", anomalyAnalysis.anomalies.size());
        try {
            TableWriteItems forumTableWriteItems = new TableWriteItems(tableName);
            for (OrderFlowImbalanceAnomaly.Anomaly anomaly : anomalyAnalysis.anomalies) {
                forumTableWriteItems.addItemToPut(
                        new Item().withPrimaryKey("date_et", Time.fromEpochSecondsToDateStr(anomaly.orderFlowImbalanceAnalysis.epochSeconds), "timestamp", anomaly.orderFlowImbalanceAnalysis.epochSeconds)
                        .withString("datetime_et", Time.fromEpochSecondsToDateTimeStr(anomaly.orderFlowImbalanceAnalysis.epochSeconds))
                        .with("datetime_recorded", Time.fromEpochSecondsToDateTimeStr(java.time.Instant.now().getEpochSecond()))
                        .with("recordDelaySeconds", java.time.Instant.now().getEpochSecond() - anomaly.orderFlowImbalanceAnalysis.epochSeconds)
                        .withString("market", anomaly.market)
                        .withString("symbol", anomaly.symbol)
                        .with("recentOrderFlowImbalance", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                        .with("orderFlowImbalanceAverage", anomaly.orderFlowImbalanceAnalysis.orderFlowImbalanceAverage)
                        .with("orderFlowImbalanceStandardDeviationWithoutOutliers", anomaly.orderFlowImbalanceAnalysis.orderFlowImbalanceStandardDeviationWithoutOutliers)
                        .with("recentOrderFlowImbalance", anomaly.orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                        .with("flowDuration", anomaly.orderFlowImbalanceAnalysis.parameter.flowDuration)
                        .with("sampleDuration", anomaly.orderFlowImbalanceAnalysis.parameter.sampleDuration));
            }

            BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(forumTableWriteItems);
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
