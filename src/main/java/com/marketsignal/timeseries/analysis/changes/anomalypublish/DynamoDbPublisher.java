package com.marketsignal.timeseries.analysis.changes.anomalypublish;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.regions.Regions;

import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;
import com.marketsignal.util.Time;

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

        log.info("[DynamoDbPublisher] publishing a new anomaly: {}", anomaly.toString());
        try {
            final String dateEtStr = Time.fromEpochSecondsToDateStr(anomaly.changeAnalysis.epochSecondsAtAnalysis);
            final String dateTimeEtStr = Time.fromEpochSecondsToDateTimeStr(anomaly.changeAnalysis.epochSecondsAtAnalysis);
            PutItemOutcome outcome = table
                    .putItem(new Item().withPrimaryKey("date_et", dateEtStr, "timestamp", anomaly.changeAnalysis.epochSecondsAtAnalysis)
                            .withString("datetime_et", dateTimeEtStr)
                            .with("datetime_recorded", Time.fromEpochSecondsToDateTimeStr(java.time.Instant.now().getEpochSecond()))
                            .with("recordDelaySeconds", java.time.Instant.now().getEpochSecond() - anomaly.changeAnalysis.epochSecondsAtAnalysis)
                            .withString("market", anomaly.market)
                            .withString("symbol", anomaly.symbol)
                            .withLong("window_size_minutes", anomaly.changeAnalysis.analyzeParameter.windowSize.toMinutes())
                            .withString("close", String.valueOf(anomaly.changeAnalysis.priceAtAnalysis))
                            .withString("max_jump", String.valueOf(anomaly.changeAnalysis.maxJump.change))
                            .withString("price_at_max_jump", String.valueOf(anomaly.changeAnalysis.maxJump.priceAtChange))
                            .withLong("max_jump_epoch_seconds", anomaly.changeAnalysis.maxJump.priceAtChangeEpochSeconds)
                            .withString("min_price_for_max_jump", String.valueOf(anomaly.changeAnalysis.maxJump.priceChangedFrom))
                            .withLong("min_price_for_max_jump_epoch_seconds", anomaly.changeAnalysis.maxJump.priceChangedFromEpochSeconds)
                            .withString("min_drop", String.valueOf(anomaly.changeAnalysis.minDrop.change))
                            .withString("price_at_min_drop", String.valueOf(anomaly.changeAnalysis.minDrop.priceAtChange))
                            .withLong("min_drop_epoch_seconds", anomaly.changeAnalysis.minDrop.priceAtChangeEpochSeconds)
                            .withString("max_price_for_min_drop", String.valueOf(anomaly.changeAnalysis.maxJump.priceChangedFrom))
                            .withLong("max_price_for_min_drop_epoch_seconds", anomaly.changeAnalysis.maxJump.priceChangedFromEpochSeconds)
                            .withString("threshold", String.valueOf(anomaly.changeThreshold))
                            .withString("type_str", anomaly.getChangeTypeStr())
                            .withString("summary", anomaly.getChangeSummaryStr())
                    );
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
