package com.tradingchangesanomaly;

import com.google.common.io.Files;
import com.marketdata.imports.BigQueryImport;
import com.marketsignalbinance.marginasset.MarginAssetBinance;
import com.trading.performance.*;
import com.tradingchangesanomaly.performance.ParameterScan;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;
import com.tradingchangesanomaly.recordprocessor.BarWithTimestampAnomalyReversalCSVProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackTestBase {
    private static final Logger log = LoggerFactory.getLogger(BackTestBase.class);

    protected ParameterRuns parameterRuns = new ParameterRuns();
    protected ParameterPnls parameterPnls = new ParameterPnls();

    protected List<ChangesAnomalyTradingStreamInitParameter> generateScanGrids() {
        ParameterScanCommon.ScanGridDoubleParam seekChangeAmplitudeScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.01).endDouble(0.01).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.05).endDouble(0.05).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.03).endDouble(-0.03).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(0.10).endDouble(0.10).stepDouble(0.01).build();
        ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam =
                ParameterScanCommon.ScanGridDoubleParam.builder().startDouble(-0.20).endDouble(-0.10).stepDouble(0.10).build();
        ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam =
                ParameterScanCommon.ScanGridIntParam.builder().startInt(20).endInt(40).stepInt(20).build();

        List<ChangesAnomalyTradingStreamInitParameter> scanGrids = new ArrayList<>();
        for (ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType triggerAnomalyType :
                Arrays.asList(ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP,
                        ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP,
                        ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.DROP)) {
            scanGrids.addAll(ParameterScan.generateScanGrids(
                    seekChangeAmplitudeScanGridParam,
                    targetReturnFromEntryScanGridParam,
                    targetStopLossScanGridParam,
                    maxJumpThresholdScanGridParam,
                    minDropThresholdScanGridParam,
                    changeAnalysisWindowScanGridParam,
                    triggerAnomalyType));
        }
        return scanGrids;
    }

    protected void runForParam(BigQueryImport.ImportParam importParam,
                     String runsExportDir,
                     String pnlsExportFileName,
                     ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        String filename = BigQueryImport.getImportedFileName(importParam);
        if (!BigQueryImport.getIfFileExist(importParam)) {
            log.info(String.format("Ingesting a file %s before a run", filename));
            BigQueryImport bqImport = new BigQueryImport();
            bqImport.importAsCSV(importParam);
        }
        log.info(String.format("Back testing from %s file", filename));

        log.info(String.format("Starting a new run: %s", changesAnomalyTradingStreamInitParameter));
        BarWithTimestampAnomalyReversalCSVProcessor barWithTimestampAnomalyReversalCSVProcessor = new BarWithTimestampAnomalyReversalCSVProcessor(new MarginAssetBinance());
        barWithTimestampAnomalyReversalCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
        ParameterRun parameterRun = ParameterRun.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTrades(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.closedTrades)
                .build();
        parameterRuns.addParameterRun(parameterRun);
        parameterRuns.exportToCsv(runsExportDir, parameterRun);
        ZonedDateTime startSateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(importParam.startEpochSeconds), ZoneId.of("America/New_York"));
        ZonedDateTime endDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(importParam.endEpochSeconds), ZoneId.of("America/New_York"));
        ZonedDateTime dateTime = startSateTime;
        while (!dateTime.isAfter(endDateTime)) {
            String dailyRunsExportDir = Paths.get(runsExportDir).resolve(dateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))).toString();
            ZonedDateTime nextDateTime = dateTime.plusDays(1);
            parameterRuns.exportToCsv(dailyRunsExportDir, parameterRun.ofRange(dateTime.toInstant().toEpochMilli() / 1000, nextDateTime.toInstant().toEpochMilli() / 1000));
            dateTime = nextDateTime;
        }

        ClosedTrades closedTrades = barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.closedTrades;
        ParameterPnl parameterPnl = ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTradesPnl(closedTrades.getClosedTradesPnl())
                .build();
        parameterPnls.addParameterPnl(parameterPnl);
        parameterPnls.appendPnlToCsv(pnlsExportFileName, parameterPnl);
        dateTime = startSateTime;
        while (!dateTime.isAfter(endDateTime)) {
            String dailyPnlsExportFileName = Paths.get(pnlsExportFileName).getParent()
                    .resolve(Files.getNameWithoutExtension(pnlsExportFileName))
                    .resolve(dateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd.csv"))).toString();
            ZonedDateTime nextDateTime = dateTime.plusDays(1);
            parameterPnls.appendPnlToCsv(dailyPnlsExportFileName, parameterPnl.ofClosedTrades(
                    closedTrades.ofRange(dateTime.toInstant().toEpochMilli() / 1000, nextDateTime.toInstant().toEpochMilli() / 1000)));
            dateTime = nextDateTime;
        }
    }
}
