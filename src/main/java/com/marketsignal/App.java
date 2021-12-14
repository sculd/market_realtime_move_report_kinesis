package com.marketsignal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

/**
 * This class will run a simple app that uses the KCL to read data and uses the AWS SDK to publish data.
 * Before running this program you must first create a Kinesis stream through the AWS console or AWS SDK.
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final String STREAM_ID = "realtime_market_stream";
    private static final String REGION = "us-east-2";

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
    /**
     * Invoke the main method with 2 args: the stream name and (optionally) the region.
     * Verifies valid inputs and then starts running the app.
     */
    public static void main(String... args) {
        final CommandLineParser parser = new OptionParser(true);
        Options options = AppOption.create();
        try {
            CommandLine commandLine = parser.parse(options, args);
            String shardId = commandLine.getOptionValue(AppOption.KEY_SHARD_ID);
            log.info("shardId: {}", shardId);
            /*
            String envVarJsonFile = commandLine.getOptionValue(AppOption.KEY_ENV_JSON);
            if (envVarJsonFile == null || envVarJsonFile.isEmpty()) {
                log.warn("the option envjson is null (or empty string)");
            } else {
                try {
                    JsonObject envVarJson = new Gson().fromJson(new FileReader(envVarJsonFile), JsonObject.class);
                    for (Map.Entry<String, JsonElement> entry : envVarJson.entrySet()) {
                        log.info("Setting up env var {}", entry.getKey());
                        setEnv(entry.getKey(), entry.getValue().getAsString());
                    }
                } catch (FileNotFoundException ex) {
                    log.warn("the option envjson config file is not present: {}", ex.getMessage());
                }
            }
            //*/

            new App().run();
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }

    private final String streamName;
    private final Region region;
    private final KinesisAsyncClient kinesisClient;

    /**
     * Constructor sets streamName and region. It also creates a KinesisClient object to send data to Kinesis.
     * This KinesisClient is used to send dummy data so that the consumer has something to read; it is also used
     * indirectly by the KCL to handle the consumption of the data.
     */
    private App() {
        this.streamName = STREAM_ID;
        this.region = Region.of(REGION);
        this.kinesisClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));
    }

    private void run() {
        /**
         * Sets up configuration for the KCL, including DynamoDB and CloudWatch dependencies. The final argument, a
         * ShardRecordProcessorFactory, is where the logic for record processing lives, and is located in a private
         * class below.
         */
        DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();
        ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, streamName, kinesisClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new RecordProcessorFactory());

        /**
         * The Scheduler (also called Worker in earlier versions of the KCL) is the entry point to the KCL. This
         * instance is configured with defaults provided by the ConfigsBuilder.
         */
        Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(streamName, kinesisClient))
        );

        /**
         * Kickoff the Scheduler. Record processing of the stream of dummy data will continue indefinitely
         * until an exit is triggered.
         */
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        /**
         * Allows termination of app by pressing Enter.
         */
        System.out.println("Press enter to shutdown");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException ioex) {
            log.error("Caught exception while waiting for confirm. Shutting down.", ioex);
        }
    }

    private static class RecordProcessorFactory implements ShardRecordProcessorFactory {
        public ShardRecordProcessor shardRecordProcessor() {
            return new RecordProcessor();
        }
    }

}
