package com.marketsignal.publish;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.regions.Regions;

import com.marketsignal.timeseries.analysis.ChangesAnomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamoDbPublisher {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbPublisher.class);

    private final String tableName = "financial_signal";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    private final DynamoDB dynamoDB = new DynamoDB(client);

    public void publish(ChangesAnomaly.Anomaly anomaly) {
        Table table = dynamoDB.getTable(tableName);

        try {
            System.out.println("Adding a new item...");
            PutItemOutcome outcome = table
                    .putItem(new Item().withPrimaryKey("date_et", "2021-12-04", "timestamp", anomaly.changeAnalysis.epochSecondsAtAnalysis)
                            .withString("datetime_et", "")
                            .withString("market", anomaly.market)
                            .withString("symbol", anomaly.symbol)
                            .withLong("window_size_minutes", anomaly.changeAnalysis.analyzeParameter.windowSize.toMinutes())
                            .withString("close", String.valueOf(anomaly.changeAnalysis.priceAtAnalysis))
                            .withString("max_jump", String.valueOf(anomaly.changeAnalysis.maxJump))
                            .withString("price_at_max_jump", String.valueOf(anomaly.changeAnalysis.priceAtMaxJump))
                            .withLong("max_jump_epoch_seconds", anomaly.changeAnalysis.maxJumpEpochSeconds)
                            .withString("min_price_for_max_jump", String.valueOf(anomaly.changeAnalysis.minPriceForMaxJump))
                            .withLong("min_price_for_max_jump_epoch_seconds", anomaly.changeAnalysis.minPriceForMaxJumpEpochSeconds)
                            .withString("min_drop", String.valueOf(anomaly.changeAnalysis.minDrop))
                            .withString("price_at_min_drop", String.valueOf(anomaly.changeAnalysis.priceAtMinDrop))
                            .withLong("min_drop_epoch_seconds", anomaly.changeAnalysis.minDropEpochSeconds)
                            .withString("max_price_for_min_drop", String.valueOf(anomaly.changeAnalysis.maxPriceForMinDrop))
                            .withLong("max_price_for_min_drop_epoch_seconds", anomaly.changeAnalysis.maxPriceForMinDropEpochSeconds)
                            .withString("threshold", String.valueOf(anomaly.changeThreshold))
                            .withString("type_str", anomaly.getChangeTypeStr())
                            .withString("summary", anomaly.getChangeSummaryStr())
                    );

            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
