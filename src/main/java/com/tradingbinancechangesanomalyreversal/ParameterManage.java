package com.tradingbinancechangesanomalyreversal;

import com.trading.state.*;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;
import com.tradingchangesanomaly.util.ChangesAnomalyTradingInitParameter;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ParameterManage {
    // manually run just once
    static void exportParam() {
        ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter =
                ChangesAnomalyTradingStreamInitParameter.builder()
                        .statesInitParameter(States.StatesInitParameter.builder()
                                .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                                        .targetFiatVolume(100)
                                        .seekChangeAmplitude(0.01)
                                        .seekSpreadToMidRatio(0.01)
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
                                                .targetReturnFromEntry(0.05)
                                                .build())
                                        .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.builder()
                                                .stopLossType(StopLossPlan.StopLossType.STOP_LOSS_FROM_TOP_PROFIT)
                                                .targetStopLoss(-0.03)
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
                                .maxJumpThreshold(0.10)
                                .minDropThreshold(-0.10)
                                .changeAnalysisWindow(Duration.ofMinutes(40))
                                .triggerAnomalyType(ChangesAnomalyStateTransition.TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP)
                                .build())
                        .build();

        ChangesAnomalyTradingInitParameter changesAnomalyTradingInitParameter = ChangesAnomalyTradingInitParameter.builder()
                .changesAnomalyTradingStreamInitParameter(changesAnomalyTradingStreamInitParameter)
                .build();

        String fileName = "k8s/secrets/tradingparam.json";
        File fileOutput = new File(fileName);
        try {
            fileOutput.createNewFile();
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName, true));
            String paramJson = changesAnomalyTradingInitParameter.toJson();
            fileWriter.write(paramJson);
            fileWriter.newLine();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static void importParam() {
        String fileName = "k8s/secrets/tradingparam.json";
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = fileReader.readLine()) != null) {
                lines.add(line);
            }
            String paramJson = String.join("\n", lines);
            fileReader.close();
            ChangesAnomalyTradingInitParameter changesAnomalyTradingInitParameter = ChangesAnomalyTradingInitParameter.builder().build();
            changesAnomalyTradingInitParameter.initFromJson(paramJson);
            System.out.println(changesAnomalyTradingInitParameter.toJson());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String... args) {
        importParam();
    }
}
