package com.marketsignal.orderbook.analysis.publish.orderbookliquidityimbalance;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.marketsignal.orderbook.analysis.LiquidityImbalance;
import com.marketsignal.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiquidityDynamoDbPublisher {
    private static final Logger log = LoggerFactory.getLogger(LiquidityDynamoDbPublisher.class);

    private final String tableName = "liquidity_imbalance";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    List<LiquidityImbalance.Analysis> orderFlowImbalanceAnalysisWriteBuffer = new ArrayList<>();
    private final int WRITE_BUFFER_SIZE = 20;

    public void publish(List<LiquidityImbalance.Analysis> liquidityImbalanceAnalysisList) {
        log.info("[DynamoDbPublisher] publishing {} orderFlowImbalanceAnalysises", liquidityImbalanceAnalysisList.size());
        try {
            TableWriteItems forumTableWriteItems = new TableWriteItems(tableName);
            for (LiquidityImbalance.Analysis liquidityImbalanceAnalysis : liquidityImbalanceAnalysisList) {
                forumTableWriteItems.addItemToPut(
                        new Item().withPrimaryKey("market_symbol", String.format("%s.%s", liquidityImbalanceAnalysis.market, liquidityImbalanceAnalysis.symbol), "timestamp", liquidityImbalanceAnalysis.epochSeconds)
                                .withString("datetime_et", Time.fromEpochSecondsToDateTimeStr(liquidityImbalanceAnalysis.epochSeconds))
                                .with("datetime_recorded", Time.fromEpochSecondsToDateTimeStr(java.time.Instant.now().getEpochSecond()))
                                .with("bidPrice", liquidityImbalanceAnalysis.bidPrice)
                                .with("askPrice", liquidityImbalanceAnalysis.askPrice)
                                .with("liquidityImbalance5", liquidityImbalanceAnalysis.liquidityImbalance5)
                                .with("liquidityImbalance10", liquidityImbalanceAnalysis.liquidityImbalance10)
                                .with("liquidityImbalance15", liquidityImbalanceAnalysis.liquidityImbalance15)
                                .with("liquidityImbalance20", liquidityImbalanceAnalysis.liquidityImbalance20));
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

    public void publish(LiquidityImbalance.Analysis orderFlowImbalanceAnalysis) {
        orderFlowImbalanceAnalysisWriteBuffer.add(orderFlowImbalanceAnalysis);
        if (orderFlowImbalanceAnalysisWriteBuffer.size() >= WRITE_BUFFER_SIZE) {
            List<LiquidityImbalance.Analysis> writeItems = new ArrayList<>(orderFlowImbalanceAnalysisWriteBuffer);
            orderFlowImbalanceAnalysisWriteBuffer.clear();
            publish(writeItems);
        }
    }
}
