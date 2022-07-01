package com.main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class will run a simple app that uses the KCL to read data and uses the AWS SDK to publish data.
 * Before running this program you must first create a Kinesis stream through the AWS console or AWS SDK.
 */
public class MainApp {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);


    public static void setEnv(String key, String value) {
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

    public enum AppType {
        CHANGES_ANOMALY_STREAM ("realtime_market_stream"),
        ORDERBOOK_ANOMALY_STREAM ("realtime_orderbook_stream"),
        CHANGES_ANOMALY_TRADING_BINANCE ("realtime_trading_binance_stream");

        private final String streamName;
        AppType(String streamName) {
            this.streamName = streamName;
        }
        public String streamName() { return streamName; }
    }

    /**
     * Verifies valid inputs and then starts running the app.
     */
    public static void main(String... args) {
        final CommandLineParser parser = new OptionParser(true);
        Options options = AppOption.create();
        try {
            CommandLine commandLine = parser.parse(options, args);

            String shardId = commandLine.getOptionValue(AppOption.KEY_SHARD_ID);
            log.info("shardId: {}", shardId);

            String envVarFile = commandLine.getOptionValue(AppOption.KEY_ENV_FILE);
            if (envVarFile == null || envVarFile.isEmpty()) {
                log.warn("the option envfile is null (or empty string)");
            } else {
                log.info("processing the envfile: {}", envVarFile);
                try (Stream<String> lines = Files.lines(Paths.get(envVarFile), Charset.defaultCharset())) {
                    lines.forEachOrdered(line -> setEnv(line.split("=")[0], line.split("=")[1]));
                } catch (IOException ex) {
                    log.error("an exception occurred processing envfile: {}", ex.toString());
                }
            }

            String appTypeStr = commandLine.getOptionValue(AppOption.APP_TYPE);
            if (appTypeStr == null || appTypeStr.isEmpty()) {
                log.warn("the option apptype is null (or empty string)");
            } else {
                log.info("appTypeStr: {}", appTypeStr);
                if (appTypeStr.equals(AppOption.APP_TYPE_VALUE_CHANGES_ANOMALY_STREAM)) {
                    new com.marketsignal.App(AppType.CHANGES_ANOMALY_STREAM).run();
                } else if (appTypeStr.equals(AppOption.APP_TYPE_VALUE_ORDERBOOK_ANOMALY_STREAM)) {
                    new com.marketsignal.App(AppType.ORDERBOOK_ANOMALY_STREAM).run();
                } else if (appTypeStr.equals(AppOption.APP_TYPE_VALUE_CHANGES_ANOMALY_TRADING_BINANCE)) {
                    new com.tradingbinancechangesanomalyreversal.App(AppType.CHANGES_ANOMALY_TRADING_BINANCE).run();
                }
            }
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }
}
