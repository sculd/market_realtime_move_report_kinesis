package com.marketsignal;

import org.apache.commons.cli.Options;

public abstract class AppOption extends Options {
    public static String KEY_SHARD_ID = "shardid";
    public static String KEY_NUM_REPLICAS = "numreplicas";
    public static String KEY_ENV_JSON = "envjson";

    public static Options create() {
        final Options options = new Options();
        options
                .addOption("si", KEY_SHARD_ID, true,
                        "zero based shard id for concurrent execution")
                .addOption("nr", KEY_NUM_REPLICAS, true,
                        "number of replicas is the number of the instance for concurrent execution")
                .addOption("ej", KEY_ENV_JSON, true,
                        "json file that defines the env vars")

        ;
        return options;
    }
}
