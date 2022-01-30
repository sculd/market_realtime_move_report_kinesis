package com.changesanomalytrading;

import com.changesanomalytrading.recordprocessor.BarWithTimestampCSVProcessor;
import com.changesanomalytrading.state.stream.ChangesAnomalyTradingStream;
import com.changesanomalytrading.transition.ChangesAnomalyStateTransition;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;
import com.marketsignal.App;
import com.marketsignal.AppOption;
import com.marketsignal.OptionParser;
import com.trading.state.*;
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
import java.time.Duration;
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
        int year = 2022;
        int month = 1;
        int day = 23;
        String filename = BigQueryImport.getImportedFileName("marketdata/", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList(),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 0, 0),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 23, 59)
        );
        log.info(String.format("Back testing from %s file", filename));

        ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter =
                ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter.builder()
                        .statesInitParameter(States.StatesInitParameter.builder()
                                .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                                        .targetFiatVolume(1000)
                                        .seekReverseChangeAmplitude(0.01)
                                        .build())
                                .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.builder()
                                        .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.builder()
                                                .takeProfitType(TakeProfitPlan.TakeProfitType.TAKE_PROFIT_FROM_ENTRY)
                                                .targetReturnFromEntry(0.05)
                                                .build())
                                        .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.builder()
                                                .stopLossType(StopLossPlan.StopLossType.STOP_LOSS_FROM_TOP_PROFIT)
                                                .targetStopLoss(-0.02)
                                                .build())
                                        .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.builder()
                                                .expirationDuration(Duration.ofMinutes(60))
                                                .build())
                                        .build())
                                .build())
                        .transitionInitParameter(ChangesAnomalyStateTransition.TransitionInitParameter.builder()
                                .maxJumpThreshold(0.10)
                                .minDropThreshold(-0.10)
                                .changeAnalysisWindow(Duration.ofMinutes(20))
                                .triggerAnomalyType(ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP)
                                .build())
                        .build();

        barWithTimestampCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
    }
}
