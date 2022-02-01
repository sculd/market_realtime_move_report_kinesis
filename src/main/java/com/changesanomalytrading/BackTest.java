package com.changesanomalytrading;

import com.changesanomalytrading.recordprocessor.BarWithTimestampCSVProcessor;
import com.changesanomalytrading.state.stream.ChangesAnomalyTradingStream;
import com.changesanomalytrading.performance.ParameterScan;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;
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
import java.util.List;
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
        int year = 2022;
        int month = 1;
        int day = 23;
        String filename = BigQueryImport.getImportedFileName("marketdata/", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList(),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 0, 0),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 23, 59)
        );
        log.info(String.format("Back testing from %s file", filename));

        ParameterScan parameterScan = new ParameterScan("backtestdata/backtest.csv");

        ParameterScan.ScanGridDoubleParam seekReverseChangeAmplitudeScanGridParam =
                ParameterScan.ScanGridDoubleParam.builder().startDouble(0.01).endDouble(0.03).stepDouble(0.01).build();
        ParameterScan.ScanGridDoubleParam targetReturnFromEntryScanGridParam =
                ParameterScan.ScanGridDoubleParam.builder().startDouble(0.05).endDouble(0.06).stepDouble(0.01).build();
        ParameterScan.ScanGridDoubleParam targetStopLossScanGridParam =
                ParameterScan.ScanGridDoubleParam.builder().startDouble(-0.04).endDouble(-0.03).stepDouble(0.01).build();
        ParameterScan.ScanGridDoubleParam maxJumpThresholdScanGridParam =
                ParameterScan.ScanGridDoubleParam.builder().startDouble(0.10).endDouble(0.10).stepDouble(0.01).build();
        ParameterScan.ScanGridDoubleParam minDropThresholdScanGridParam =
                ParameterScan.ScanGridDoubleParam.builder().startDouble(-0.10).endDouble(-0.06).stepDouble(0.02).build();
        ParameterScan.ScanGridIntParam changeAnalysisWindowScanGridParam =
                ParameterScan.ScanGridIntParam.builder().startInt(20).endInt(30).stepInt(10).build();
        List<ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter> scanGrids = ParameterScan.generateScanGrids(
                seekReverseChangeAmplitudeScanGridParam,
                targetReturnFromEntryScanGridParam,
                targetStopLossScanGridParam,
                maxJumpThresholdScanGridParam,
                minDropThresholdScanGridParam,
                changeAnalysisWindowScanGridParam);

        for (ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            log.info(String.format("Starting a new run: %s", changesAnomalyTradingStreamInitParameter));
            BarWithTimestampCSVProcessor barWithTimestampCSVProcessor = new BarWithTimestampCSVProcessor();
            barWithTimestampCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
            parameterScan.addParameterRuns(
                    barWithTimestampCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter,
                    barWithTimestampCSVProcessor.changesAnomalyTradingStream.closedTrades);
        }
    }
}
