package com.tradingchangesanomaly.performance;

import com.trading.performance.ParameterScanCommon;
import com.trading.state.*;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ParameterScan {
    static public List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> generateScanGrids(
            ParameterScanCommon.ScanGridDoubleParam seekChangeAmplitudeScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetReturnFromEntryScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam targetStopLossScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam maxJumpThresholdScanGridParam,
            ParameterScanCommon.ScanGridDoubleParam minDropThresholdScanGridParam,
            ParameterScanCommon.ScanGridIntParam changeAnalysisWindowScanGridParam,
            ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType triggerAnomalyType
    ) {
        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> grid = new ArrayList<>();

        for (Double seekChangeAmplitude : seekChangeAmplitudeScanGridParam.getValues()) {
            for (Double targetReturnFromEntry : targetReturnFromEntryScanGridParam.getValues()) {
                for (Double targetStopLoss : targetStopLossScanGridParam.getValues()) {
                    for (Double maxJumpThreshold : maxJumpThresholdScanGridParam.getValues()) {
                        for (Double minDropThreshold : minDropThresholdScanGridParam.getValues()) {
                            for (Integer changeAnalysisWindow : changeAnalysisWindowScanGridParam.getValues()) {
                                ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter =
                                        ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter.builder()
                                                .statesInitParameter(States.StatesInitParameter.builder()
                                                        .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                                                                .targetFiatVolume(1000)
                                                                .seekChangeAmplitude(seekChangeAmplitude)
                                                                .build())
                                                        .enterInProgressInitParameter(EnterInProgress.EnterInProgressInitParameter.builder()
                                                                .timeoutPlanInitParameter(
                                                                        TimeoutPlan.TimeoutPlanInitParameter.builder()
                                                                                .expirationDuration(Duration.ofSeconds(10))
                                                                                .build())
                                                                .build())
                                                        .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.builder()
                                                                .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.builder()
                                                                        .takeProfitType(TakeProfitPlan.TakeProfitType.NO_TAKE_PROFIT)
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
                                                        .exitInProgressInitParameter(ExitInProgress.ExitInProgressInitParameter.builder()
                                                                .timeoutPlanInitParameter(
                                                                        TimeoutPlan.TimeoutPlanInitParameter.builder()
                                                                                .expirationDuration(Duration.ofSeconds(10))
                                                                                .build())
                                                                .build())
                                                        .build())
                                                .transitionInitParameter(ChangesAnomalyStateTransition.TransitionInitParameter.builder()
                                                        .maxJumpThreshold(maxJumpThreshold)
                                                        .minDropThreshold(minDropThreshold)
                                                        .changeAnalysisWindow(Duration.ofMinutes(changeAnalysisWindow))
                                                        .triggerAnomalyType(triggerAnomalyType)
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
}
