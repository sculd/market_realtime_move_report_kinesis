package com.marketsignal;

import org.apache.commons.cli.Options;

public abstract class AppOption extends Options {
    public static String KEY_SHARD_ID = "shardid";
    public static String KEY_ENV_FILE = "envfile";
    public static String APP_TYPE = "apptype";
    public static String APP_TYPE_VALUE_CHANGES_ANOMALY_STREAM = "changes_anomaly_stream";
    public static String APP_TYPE_VALUE_ORDERBOOK_ANOMALY_STREAM = "orderbook_anomaly_stream";

    public static Options create() {
        final Options options = new Options();
        options
                .addOption("si", KEY_SHARD_ID, true,
                        "zero based shard id for concurrent execution")
                .addOption("ef", KEY_ENV_FILE, true,
                        "env file that defines the env vars")
                .addOption("at", APP_TYPE, true,
                        "app type (changes_anomaly_stream, orderbook_anomaly_stream)")

        ;
        return options;
    }
}
