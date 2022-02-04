package com.tradingchangesanomalyreversal.performance;

import com.tradingchangesanomalyreversal.stream.ChangesAnomalyReversalTradingStream;
import com.tradingchangesanomalyreversal.state.transition.ChangesAnomalyReversalStateTransition;
import com.trading.performance.ClosedTrades;
import com.trading.performance.ParameterScanCommon;
import com.trading.state.*;
import lombok.Builder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ParameterScan {
    String exportFileName;

    public ParameterScan(String exportFileName) {
        try {
            this.exportFileName = exportFileName;
            FileWriter exportFileWriter = new FileWriter(exportFileName);
            exportFileWriter.write(String.format("%s,%s\n",
                    ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter.toCsvHeader(),
                    ClosedTrades.toCsvHeader()));
            exportFileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Builder
    public static class ParameterRun {
        public ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter changesAnomalyReversalTradingStreamInitParameter;
        public ClosedTrades closedTrades;
    }
    List<ParameterRun> parameterRuns = new ArrayList<>();

    static public List<ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter> generateScanGrids(
            ParameterScanCommon.ScanGridDoubleParam seekChangeAmplitudeScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam,
            ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam
    ) {
        List<ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter> grid = new ArrayList<>();

        for (Double seekChangeAmplitude : seekChangeAmplitudeScanGridParam.getValues()) {
            for (Double targetReturnFromEntry : targetReturnFromEntryScanGridParam.getValues()) {
                for (Double targetStopLoss : targetStopLossScanGridParam.getValues()) {
                    for (Double maxJumpThreshold : maxJumpThresholdScanGridParam.getValues()) {
                        for (Double minDropThreshold : minDropThresholdScanGridParam.getValues()) {
                            for (Integer changeAnalysisWindow : changeAnalysisWindowScanGridParam.getValues()) {
                                ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter changesAnomalyReversalTradingStreamInitParameter =
                                        ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter.builder()
                                                .statesInitParameter(States.StatesInitParameter.builder()
                                                        .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                                                                .targetFiatVolume(1000)
                                                                .seekChangeAmplitude(seekChangeAmplitude)
                                                                .build())
                                                        .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.builder()
                                                                .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.builder()
                                                                        .takeProfitType(TakeProfitPlan.TakeProfitType.TAKE_PROFIT_FROM_ENTRY)
                                                                        .targetReturnFromEntry(targetReturnFromEntry)
                                                                        .build())
                                                                .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.builder()
                                                                        .stopLossType(StopLossPlan.StopLossType.STOP_LOSS_FROM_TOP_PROFIT)
                                                                        .targetStopLoss(targetStopLoss)
                                                                        .build())
                                                                .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.builder()
                                                                        .expirationDuration(Duration.ofMinutes(60))
                                                                        .build())
                                                                .build())
                                                        .build())
                                                .transitionInitParameter(ChangesAnomalyReversalStateTransition.TransitionInitParameter.builder()
                                                        .maxJumpThreshold(maxJumpThreshold)
                                                        .minDropThreshold(minDropThreshold)
                                                        .changeAnalysisWindow(Duration.ofMinutes(changeAnalysisWindow))
                                                        .triggerAnomalyType(ChangesAnomalyReversalStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP)
                                                        .build())
                                                .build();
                                grid.add(changesAnomalyReversalTradingStreamInitParameter);
                            }
                        }
                    }
                }
            }
        }

        return grid;
    }

    public void addParameterRuns(ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter changesAnomalyReversalTradingStreamInitParameter,
                                 ClosedTrades closedTrades) {
        ParameterRun parameterRun = ParameterRun.builder()
                .changesAnomalyReversalTradingStreamInitParameter(changesAnomalyReversalTradingStreamInitParameter)
                .closedTrades(closedTrades)
                .build();
        parameterRuns.add(parameterRun);
        appendRunToCsv(parameterRun);
    }

    void appendRunToCsv(ParameterRun parameterRun) {
        String line = String.format("%s,%s\n",
                parameterRun.changesAnomalyReversalTradingStreamInitParameter.toCsvLine(),
                parameterRun.closedTrades.toCsvLine());
        try {
            Files.write(Paths.get(exportFileName), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
}