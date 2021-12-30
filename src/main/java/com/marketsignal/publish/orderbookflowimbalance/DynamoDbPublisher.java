package com.marketsignal.publish.orderbookflowimbalance;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.marketsignal.orderbook.analysis.OrderFlowImbalance;
import com.marketsignal.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamoDbPublisher {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbPublisher.class);

    private final String tableName = "order_flow_imbalance";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    List<OrderFlowImbalance.Analysis> orderFlowImbalanceAnalysisWriteBuffer = new ArrayList<>();
    private final int WRITE_BUFFER_SIZE = 30;

    public void publish(List<OrderFlowImbalance.Analysis> orderFlowImbalanceAnalysisList) {
        log.info("[DynamoDbPublisher] publishing {} orderFlowImbalanceAnalysises", orderFlowImbalanceAnalysisList.size());
        try {
            TableWriteItems forumTableWriteItems = new TableWriteItems(tableName);
            for (OrderFlowImbalance.Analysis orderFlowImbalanceAnalysis : orderFlowImbalanceAnalysisList) {
                forumTableWriteItems.addItemToPut(new Item().withPrimaryKey("market_symbol", String.format("%s.%s", orderFlowImbalanceAnalysis.market, orderFlowImbalanceAnalysis.symbol), "timestamp", orderFlowImbalanceAnalysis.epochSeconds)
                        .withString("datetime_et", Time.fromEpochSecondsToDateTimeStr(orderFlowImbalanceAnalysis.epochSeconds))
                        .with("datetime_recorded", Time.fromEpochSecondsToDateTimeStr(java.time.Instant.now().getEpochSecond()))
                        .with("recentOrderFlowImbalance", orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                        .with("orderFlowImbalanceAverage", orderFlowImbalanceAnalysis.orderFlowImbalanceAverage)
                        .with("orderFlowImbalanceStandardDeviation", orderFlowImbalanceAnalysis.orderFlowImbalanceStandardDeviationWithoutOutliers)
                        .with("recentOrderFlowImbalance", orderFlowImbalanceAnalysis.recentOrderFlowImbalance)
                        .with("flowDuration", orderFlowImbalanceAnalysis.parameter.flowDuration)
                        .with("sampleDuration", orderFlowImbalanceAnalysis.parameter.sampleDuration));
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

    public void publish(OrderFlowImbalance.Analysis orderFlowImbalanceAnalysis) {
        orderFlowImbalanceAnalysisWriteBuffer.add(orderFlowImbalanceAnalysis);
        if (orderFlowImbalanceAnalysisWriteBuffer.size() >= WRITE_BUFFER_SIZE) {
            List<OrderFlowImbalance.Analysis> writeItems = new ArrayList<>(orderFlowImbalanceAnalysisWriteBuffer);
            orderFlowImbalanceAnalysisWriteBuffer.clear();
            publish(writeItems);
        }
    }
}
