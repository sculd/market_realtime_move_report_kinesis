package com.marketsignal.timeseries.analysis.changes.anomalypublish;

import kong.unirest.Unirest;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationPublisher {
    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    @Builder
    static public class NotificationParameter {
        public String symbol;
        public String market;
        public double current_price;
        public long epoch;
        public int min_drop_percent;
        public double price_at_min_drop;
        public long epoch_at_min_drop;
        public int max_jump_percent;
        public double price_at_max_jump;
        public long epoch_at_max_jump;
        public long window_size_minutes;
        public int threshold_percent;
        public String summary;
        public String move_type;
    }

    final String endpoint = System.getenv("NOTIFICATION_ENDPOINT");
    public void publish(ChangesAnomaly.Anomaly anomaly) {
        String api_key = System.getenv("NOTIFICATION_ENDPOINT_API_KEY");

        log.info("[NotificationPublisher] publishing a new anomaly: {}", anomaly.toString());
        try {
            NotificationParameter notificationParameter = NotificationParameter.builder()
                    .market(anomaly.market)
                    .symbol(anomaly.symbol)
                    .current_price(anomaly.changeAnalysis.priceAtAnalysis)
                    .epoch(anomaly.changeAnalysis.epochSecondsAtAnalysis)
                    .min_drop_percent((int) (anomaly.changeAnalysis.minDrop.change * 100))
                    .price_at_min_drop(anomaly.changeAnalysis.minDrop.priceAtChange)
                    .epoch_at_min_drop(anomaly.changeAnalysis.minDrop.priceAtChangeEpochSeconds)
                    .max_jump_percent((int) (anomaly.changeAnalysis.maxJump.change * 100))
                    .price_at_max_jump(anomaly.changeAnalysis.maxJump.priceAtChange)
                    .epoch_at_max_jump(anomaly.changeAnalysis.maxJump.priceAtChangeEpochSeconds)
                    .window_size_minutes(anomaly.changeAnalysis.analyzeParameter.windowSize.toMinutes())
                    .threshold_percent((int) (anomaly.changeThreshold * 100))
                    .summary(anomaly.getChangeSummaryStr())
                    .move_type(anomaly.getChangeTypeStr())
                    .build();

            HttpResponse<JsonNode> response = Unirest.post(endpoint)
                    .header("Content-Type", "application/json")
                    .header("x-api-key", api_key)
                    .body(notificationParameter)
                    .asJson();

            if (!response.isSuccess()) {
                log.error(response.getStatusText());
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }
}
