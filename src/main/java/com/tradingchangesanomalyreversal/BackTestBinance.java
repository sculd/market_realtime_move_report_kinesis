package com.tradingchangesanomalyreversal;

import com.trading.performance.*;
import com.tradingchangesanomaly.performance.*;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import com.tradingchangesanomalyreversal.recordprocessor.BarWithTimestampAnomalyCSVProcessor;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;
import com.marketsignal.App;
import com.marketsignal.AppOption;
import com.marketsignal.OptionParser;
import lombok.Builder;
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

public class BackTestBinance {
    private static final Logger log = LoggerFactory.getLogger(BackTestBinance.class);

    ParameterRuns parameterRuns = new ParameterRuns();
    ParameterPnls parameterPnls = new ParameterPnls();

    public static void main(String... args) {
        final CommandLineParser parser = new OptionParser(true);
        Options options = AppOption.create();
        try {
            CommandLine commandLine = parser.parse(options, args);

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

            new BackTestBinance().doRunRange();
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }

    private void doRunRange() {
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2022)
                .monthBegin(1)
                .dayBegin(1)
                .yearEnd(2022)
                .monthEnd(1)
                .dayEnd(31)
                .build();

        runRange(rangeRunParameter);
    }

    private void runEachDay() {
        for (int day = 22; day <= 31; day++) {
            DailyRunParameter dailyRunParameter = DailyRunParameter.builder()
                    .year(2022)
                    .month(1)
                    .day(day)
                    .build();

            runDaily(dailyRunParameter);
        }
    }

    private void run(BigQueryImport.ImportParam importParam, String runsExportDir, String pnlsExportFileName,
                     ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        String filename = BigQueryImport.getImportedFileName(importParam);
        if (!BigQueryImport.getIfFileExist(importParam)) {
            log.info(String.format("Ingesting a file %s before a run", filename));
            BigQueryImport bqImport = new BigQueryImport();
            bqImport.importAsCSV(importParam);
        }
        log.info(String.format("Back testing from %s file", filename));

        log.info(String.format("Starting a new run: %s", changesAnomalyTradingStreamInitParameter));
        BarWithTimestampAnomalyCSVProcessor barWithTimestampAnomalyCSVProcessor = new BarWithTimestampAnomalyCSVProcessor();
        barWithTimestampAnomalyCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
        ParameterRun parameterRun = ParameterRun.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTrades(barWithTimestampAnomalyCSVProcessor.changesAnomalyTradingStream.closedTrades)
                .build();
        parameterRuns.addParameterRun(parameterRun);
        parameterRuns.appendRunToCsv(runsExportDir, parameterRun);

        ParameterPnl parameterPnl = ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTradesPnl(barWithTimestampAnomalyCSVProcessor.changesAnomalyTradingStream.closedTrades.getClosedTradesPnl())
                .build();
        parameterPnls.addParameterPnl(parameterPnl);
        parameterPnls.appendPnlToCsv(pnlsExportFileName, parameterPnl);
    }

    private List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> generateScanGrids() {
        ParameterScanCommon.ScanGridDoubleParam seekChangeAmplitudeScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.01).endDouble(0.01).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.05).endDouble(0.05).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.03).endDouble(-0.03).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.10).endDouble(0.10).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.20).endDouble(-0.10).stepDouble(0.05).build();
        ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam =
                ParameterScanCommon.ScanGridIntParam.builder().startInt(20).endInt(40).stepInt(10).build();
        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> scanGrids = ParameterScan.generateScanGrids(
                seekChangeAmplitudeScanGridParam,
                targetReturnFromEntryScanGridParam,
                targetStopLossScanGridParam,
                maxJumpThresholdScanGridParam,
                minDropThresholdScanGridParam,
                changeAnalysisWindowScanGridParam,
                ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP);
        return scanGrids;
    }

    @Builder
    static public class DailyRunParameter {
        int year;
        int month;
        int day;
    }

    private void runDaily(DailyRunParameter dailyRunParameter) {
        BigQueryImport.ImportParam importParam = BigQueryImport.ImportParam.builder()
                .baseDirPath("marketdata/")
                .table(QueryTemplates.Table.BINANCE_BAR_WITH_TIME)
                .symbols(Arrays.asList())
                .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day, 0, 0))
                .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day, 23, 59))
                .build();

        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> scanGrids = generateScanGrids();

        String runsExportDir = String.format("backtestdata/binance/runs/reversal/backtest_runs_%d_%d_%d", dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day);
        String pnlsExportFileName = String.format("backtestdata/binance/pnls/reversal/backtest_%d_%d_%d_.csv", dailyRunParameter.year, dailyRunParameter.month, dailyRunParameter.day);

        ParameterPnls.createNew(pnlsExportFileName);
        for (ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            run(importParam, runsExportDir, pnlsExportFileName, changesAnomalyTradingStreamInitParameter);
        }
    }

    @Builder
    static public class RangeRunParameter {
        int yearBegin;
        int monthBegin;
        int dayBegin;
        int yearEnd;
        int monthEnd;
        int dayEnd;

        public String toFileNamePhrase() {
            return String.format("from_%d_%d_%d_to_%d_%d_%d", yearBegin, monthBegin, dayBegin, yearEnd, monthEnd, dayEnd);
        }

        BigQueryImport.ImportParam getImportParam() {
            return BigQueryImport.ImportParam.builder()
                    .baseDirPath("marketdata/")
                    .table(QueryTemplates.Table.BINANCE_BAR_WITH_TIME)
                    .symbols(Arrays.asList())
                    .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearBegin, monthBegin, dayBegin, 0, 0))
                    .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearEnd, monthEnd, dayEnd, 0, -1))
                    .build();
        }
    }

    private void runRange(RangeRunParameter rangeRunParameter) {
        BigQueryImport.ImportParam importParam = rangeRunParameter.getImportParam();
        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> scanGrids = generateScanGrids();

        String runsExportDir = String.format("backtestdata/binance/runs/reversal/backtest_runs_%s", rangeRunParameter.toFileNamePhrase());
        String pnlsExportFileName = String.format("backtestdata/binance/pnls/reversal/backtest_%s.csv", rangeRunParameter.toFileNamePhrase());

        ParameterPnls.createNew(pnlsExportFileName);
        for (ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            run(importParam, runsExportDir, pnlsExportFileName, changesAnomalyTradingStreamInitParameter);
        }
    }
}
