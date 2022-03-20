package com.tradingchangesanomalyfollowing;

import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.recordprocessor.BarWithTimestampAnomalyFollowingCSVProcessor;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;
import com.tradingchangesanomaly.performance.ParameterScan;
import com.trading.performance.ParameterRun;
import com.trading.performance.ParameterRuns;
import com.trading.performance.ParameterPnl;
import com.trading.performance.ParameterPnls;
import com.tradingchangesanomalyreversal.BackTestBinance;
import com.main.AppOption;
import com.main.OptionParser;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;
import com.trading.performance.ParameterScanCommon;
import lombok.Builder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class BackTest {
    private static final Logger log = LoggerFactory.getLogger(BackTestBinance.class);

    public static void main(String... args) {
        final CommandLineParser parser = new OptionParser(true);
        Options options = AppOption.create();
        try {
            CommandLine commandLine = parser.parse(options, args);

            new BackTest().run();
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }

    @Builder
    static public class DailyRunParameter {
        int year;
        int month;
        int day;
    }

    private void run(DailyRunParameter dailyRunParameter) {
        BigQueryImport.ImportParam importParam = BigQueryImport.ImportParam.builder()
                .baseDirPath("marketdata/")
                .table(QueryTemplates.Table.BINANCE_BAR_WITH_TIME)
                .symbols(Arrays.asList())
                .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day, 0, 0))
                .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day, 23, 59))
                .build();

        String filename = BigQueryImport.getImportedFileName(importParam);
        if (!BigQueryImport.getIfFileExist(importParam)) {
            log.info(String.format("Ingesting a file %s before a run", filename));
            BigQueryImport bqImport = new BigQueryImport();
            bqImport.importAsCSV(importParam);
        }
        log.info(String.format("Back testing from %s file", filename));


        ParameterScanCommon.ScanGridDoubleParam seekChangeAmplitudeScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0).endDouble(0).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.05).endDouble(0.08).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.04).endDouble(-0.01).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.04).endDouble(0.07).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.05).endDouble(-0.05).stepDouble(0.05).build();
        ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam =
                ParameterScanCommon.ScanGridIntParam.builder().startInt(20).endInt(20).stepInt(10).build();
        List<ChangesAnomalyTradingStreamInitParameter> scanGrids = ParameterScan.generateScanGrids(
                seekChangeAmplitudeScanGridParam,
                targetReturnFromEntryScanGridParam,
                targetStopLossScanGridParam,
                maxJumpThresholdScanGridParam,
                minDropThresholdScanGridParam,
                changeAnalysisWindowScanGridParam,
                ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP);

        String runsExportDir = String.format("backtestdata/following/backtest_runs_%d_%d_%d", dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day);
        String pnlsExportFileName = String.format("backtestdata/following/backtest_%d_%d_%d.csv", dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day);
        ParameterRuns parameterRuns = new ParameterRuns();
        ParameterPnls parameterPnls = new ParameterPnls();
        for (ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            log.info(String.format("Starting a new run: %s", changesAnomalyTradingStreamInitParameter));
            BarWithTimestampAnomalyFollowingCSVProcessor barWithTimestampAnomalyFollowingCSVProcessor = new BarWithTimestampAnomalyFollowingCSVProcessor();
            barWithTimestampAnomalyFollowingCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
            ParameterRun parameterRun = ParameterRun.builder()
                    .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyFollowingCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                    .closedTrades(barWithTimestampAnomalyFollowingCSVProcessor.changesAnomalyTradingStream.closedTrades)
                    .build();
            parameterRuns.addParameterRun(parameterRun);
            parameterRuns.appendRunToCsv(runsExportDir, parameterRun);

            ParameterPnl parameterPnl = ParameterPnl.builder()
                    .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyFollowingCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                    .closedTradesPnl(barWithTimestampAnomalyFollowingCSVProcessor.changesAnomalyTradingStream.closedTrades.getClosedTradesPnl())
                    .build();
            parameterPnls.addParameterPnl(parameterPnl);
            parameterPnls.appendPnlToCsv(pnlsExportFileName, parameterPnl);
        }
    }

    private void run() {
        for (int day = 20; day <= 31; day++) {
            DailyRunParameter dailyRunParameter = DailyRunParameter.builder()
                    .year(2022)
                    .month(1)
                    .day(day)
                    .build();

            run(dailyRunParameter);
        }
    }
}