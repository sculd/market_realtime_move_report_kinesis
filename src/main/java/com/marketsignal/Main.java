package com.marketsignal;

import com.marketsignal.timeseries.analysis.changes.anomalypublish.DynamoDbPublisher;
import com.marketsignal.timeseries.analysis.changes.anomalypublish.NotificationPublisher;
import com.marketsignal.timeseries.analysis.changes.anomalypublish.SlackPublisher;
import com.marketsignal.timeseries.analysis.changes.Changes;
import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;


public class Main {
    static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    public static void main(String[] args)
    {
        String envVarFile = "k8s/secrets/envvar.env";
        try (Stream<String> lines = Files.lines(Paths.get(envVarFile), Charset.defaultCharset())) {
            lines.forEachOrdered(line -> setEnv(line.split("=")[0], line.split("=")[1]));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

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
        slackPublisher.publish(anomaly);

        NotificationPublisher notificationPublisher = new NotificationPublisher();
        notificationPublisher.publish(anomaly);
        System.out.println("done");
    }
}
