package com.marketsignal;

import com.marketsignal.publish.DynamoDbPublisher;
import com.marketsignal.publish.NotificationPublisher;
import com.marketsignal.publish.SlackPublisher;
import com.marketsignal.timeseries.analysis.Changes;
import com.marketsignal.timeseries.analysis.ChangesAnomaly;

import java.time.Duration;


public class Main {
    public static void main(String[] args)
    {
        // for sandbox testing.
        DynamoDbPublisher dynamoDbPublisher = new DynamoDbPublisher();
        ChangesAnomaly.Anomaly anomaly = ChangesAnomaly.Anomaly.builder()
                .changeThreshold(0.1)
                .market("dummy_market")
                .symbol("dummy_symbol")
                .changeAnalysis(Changes.AnalyzeResult.builder()
                        .maxJump(0.15)
                        .analyzeParameter(Changes.AnalyzeParameter.builder()
                                .windowSize(Duration.ofMinutes(60))
                                .build())
                        .build())
                .build();

        SlackPublisher slackPublisher = new SlackPublisher();
        //dynamoDbPublisher.publish(anomaly);
        //slackPublisher.publish(anomaly);

        NotificationPublisher notificationPublisher = new NotificationPublisher();
        notificationPublisher.publish(anomaly);

        System.out.println("hello world!");
    }
}
