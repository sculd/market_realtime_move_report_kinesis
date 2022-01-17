package com.marketsignal.timeseries.analysis.anomalypublish;

import com.marketsignal.timeseries.analysis.ChangesAnomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbPublisher.class);
    DynamoDbPublisher dynamoDbPublisher = new DynamoDbPublisher();
    SlackPublisher slackPublisher = new SlackPublisher();

    public void publish(ChangesAnomaly.Anomaly anomaly) {
        log.info("{}", anomaly);
        dynamoDbPublisher.publish(anomaly);
        if (slackPublisher.isNonTrivialAnomaly(anomaly)) {
            slackPublisher.publish(anomaly);
        }
    }
}
