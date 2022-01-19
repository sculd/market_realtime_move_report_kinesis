package com.changesanomalytrading;

import com.changesanomalytrading.recordprocessor.BarWithTimestampCSVProcessor;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketsignal.App;
import com.marketsignal.AppOption;
import com.marketsignal.OptionParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class BackTest {
    private static final Logger log = LoggerFactory.getLogger(BackTest.class);

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
                try (Stream<String> lines = Files.lines(Paths.get(envVarFile), Charset.defaultCharset())) {
                    lines.forEachOrdered(line -> App.setEnv(line.split("=")[0], line.split("=")[1]));
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }

            App.AppType appType = App.AppType.CHANGES_ANOMALY_STREAM;
            String appTypeStr = commandLine.getOptionValue(AppOption.APP_TYPE);
            if (appTypeStr == null || appTypeStr.isEmpty()) {
                log.warn("the option apptype is null (or empty string)");
            } else {
                if (appTypeStr.equals(AppOption.APP_TYPE_VALUE_CHANGES_ANOMALY_STREAM)) {
                    appType = App.AppType.CHANGES_ANOMALY_STREAM;
                } else if (appTypeStr.equals(AppOption.APP_TYPE_VALUE_ORDERBOOK_ANOMALY_STREAM)) {
                    appType = App.AppType.ORDERBOOK_ANOMALY_STREAM;
                }
            }

            new BackTest(appType).run();
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }

    private final App.AppType appType;

    private BackTest(App.AppType appType) {
        this.appType = appType;
    }

    private void run() {
        BarWithTimestampCSVProcessor barWithTimestampCSVProcessor = new BarWithTimestampCSVProcessor();
        String filename = BigQueryImport.getImportedFileName("marketdata/", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList(), 1642514400, 1642521600);
        barWithTimestampCSVProcessor.run(filename);
    }
}
