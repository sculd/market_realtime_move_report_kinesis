package com.changesanomalytrading.performance;

import com.changesanomalytrading.state.stream.ChangesAnomalyTradingStream;
import com.changesanomalytrading.transition.ChangesAnomalyReversalStateTransition;
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
                    ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter.toCsvHeader(),
                    ClosedTrades.toCsvHeader()));
            exportFileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Builder
    public static class ParameterRun {
        public ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;
        public ClosedTrades closedTrades;
    }
    List<ParameterRun> parameterRuns = new ArrayList<>();

    static public List<ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter> generateScanGrids(
            ParameterScanCommon.ScanGridDoubleParam seekReverseChangeAmplitudeScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam,
            ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam
    ) {
        List<ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter> grid = new ArrayList<>();

        for (Double seekReverseChangeAmplitude : seekReverseChangeAmplitudeScanGridParam.getValues()) {
            for (Double targetReturnFromEntry : targetReturnFromEntryScanGridParam.getValues()) {
                for (Double targetStopLoss : targetStopLossScanGridParam.getValues()) {
                    for (Double maxJumpThreshold : maxJumpThresholdScanGridParam.getValues()) {
                        for (Double minDropThreshold : minDropThresholdScanGridParam.getValues()) {
                            for (Integer changeAnalysisWindow : changeAnalysisWindowScanGridParam.getValues()) {
                                ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter =
                                        ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter.builder()
                                                .statesInitParameter(States.StatesInitParameter.builder()
                                                        .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                                                                .targetFiatVolume(1000)
                                                                .seekReverseChangeAmplitude(seekReverseChangeAmplitude)
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
                                                        .triggerAnomalyType(ChangesAnomalyReversalStateTransition.TransitionInitParameter.TriggerAnomalyType.DROP)
                                                        .build())
                                                .build();
                                grid.add(changesAnomalyTradingStreamInitParameter);
                            }
                        }
                    }
                }
            }
        }

        return grid;
    }

    public void addParameterRuns(ChangesAnomalyTradingStream.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter,
                                 ClosedTrades closedTrades) {
        ParameterRun parameterRun = ParameterRun.builder()
                .changesAnomalyTradingStreamInitParameter(changesAnomalyTradingStreamInitParameter)
                .closedTrades(closedTrades)
                .build();
        parameterRuns.add(parameterRun);
        appendRunToCsv(parameterRun);
    }

    void appendRunToCsv(ParameterRun parameterRun) {
        String line = String.format("%s,%s\n",
                parameterRun.changesAnomalyTradingStreamInitParameter.toCsvLine(),
                parameterRun.closedTrades.toCsvLine());
        try {
            Files.write(Paths.get(exportFileName), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
}
