package com.tradingchangesanomaly;

import com.marketdata.imports.BigQueryImport;
import com.trading.performance.*;
import com.tradingchangesanomaly.performance.ParameterScan;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamUtil;
import com.tradingchangesanomaly.recordprocessor.BarWithTimestampAnomalyReversalCSVProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackTestBase {
    private static final Logger log = LoggerFactory.getLogger(BackTestBase.class);

    protected ParameterRuns parameterRuns = new ParameterRuns();
    protected ParameterPnls parameterPnls = new ParameterPnls();

    protected List<ChangesAnomalyTradingStreamUtil.ChangesAnomalyTradingStreamInitParameter> generateScanGrids() {
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
        List<ChangesAnomalyTradingStreamUtil.ChangesAnomalyTradingStreamInitParameter> scanGrids = ParameterScan.generateScanGrids(
                seekChangeAmplitudeScanGridParam,
                targetReturnFromEntryScanGridParam,
                targetStopLossScanGridParam,
                maxJumpThresholdScanGridParam,
                minDropThresholdScanGridParam,
                changeAnalysisWindowScanGridParam,
                ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP);
        return scanGrids;
    }

    protected void runForParam(BigQueryImport.ImportParam importParam,
                     String runsExportDir,
                     String pnlsExportFileName,
                     ChangesAnomalyTradingStreamUtil.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        String filename = BigQueryImport.getImportedFileName(importParam);
        if (!BigQueryImport.getIfFileExist(importParam)) {
            log.info(String.format("Ingesting a file %s before a run", filename));
            BigQueryImport bqImport = new BigQueryImport();
            bqImport.importAsCSV(importParam);
        }
        log.info(String.format("Back testing from %s file", filename));

        log.info(String.format("Starting a new run: %s", changesAnomalyTradingStreamInitParameter));
        BarWithTimestampAnomalyReversalCSVProcessor barWithTimestampAnomalyReversalCSVProcessor = new BarWithTimestampAnomalyReversalCSVProcessor();
        barWithTimestampAnomalyReversalCSVProcessor.run(filename, changesAnomalyTradingStreamInitParameter);
        ParameterRun parameterRun = ParameterRun.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTrades(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.closedTrades)
                .build();
        parameterRuns.addParameterRun(parameterRun);
        parameterRuns.appendRunToCsv(runsExportDir, parameterRun);

        ParameterPnl parameterPnl = ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.changesAnomalyTradingStreamInitParameter)
                .closedTradesPnl(barWithTimestampAnomalyReversalCSVProcessor.changesAnomalyTradingStream.closedTrades.getClosedTradesPnl())
                .build();
        parameterPnls.addParameterPnl(parameterPnl);
        parameterPnls.appendPnlToCsv(pnlsExportFileName, parameterPnl);
    }
}
