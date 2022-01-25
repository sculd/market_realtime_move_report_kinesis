package com.changesanomalytrading.state.stream;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.Changes;
import com.trading.performance.ClosedTrade;
import com.trading.performance.ClosedTrades;

import com.trading.state.*;

import com.changesanomalytrading.transition.ChangesAnomalyStateTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    public Map<String, States> keyedStates = new HashMap<>();
    public Map<String, ChangesAnomalyStateTransition> keyedStateTransition = new HashMap<>();
    public ClosedTrades closedTrades = new ClosedTrades();
    States.StatesInitParameter statesInitParameter;
    ChangesAnomalyStateTransition.TransitionInitParameter transitionInitParameter;

    public ChangesAnomalyTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
        statesInitParameter = States.StatesInitParameter.builder()
                .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.builder()
                        .targetVolume(1000)
                        .seekReverseChangeAmplitude(0.02)
                        .build())
                .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.builder()
                        .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.builder()
                                .takeProfitType(TakeProfitPlan.TakeProfitType.TAKE_PROFIT_FROM_ENTRY)
                                .targetReturnFromEntry(0.03)
                                .build())
                        .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.builder()
                                .stopLossType(StopLossPlan.StopLossType.STOP_LOSS_FROM_TOP_PROFIT)
                                .targetStopLoss(-0.02)
                                .build())
                        .build())
                .build();

        transitionInitParameter = ChangesAnomalyStateTransition.TransitionInitParameter.builder()
                .maxJumpThreshold(0.10)
                .build();
    }

    States getState(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key, new States(bwt.bar.market, bwt.bar.symbol, statesInitParameter));
        }
        return keyedStates.get(key);
    }

    ChangesAnomalyStateTransition getStateTransition(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStateTransition.containsKey(key)) {
            keyedStateTransition.put(key,
                    new ChangesAnomalyStateTransition(bwt.bar.market, bwt.bar.symbol, transitionInitParameter));
        }
        return keyedStateTransition.get(key);
    }

    void onClosedTrade(ClosedTrade closedTrade) {
        log.info(String.format("onClosedTrade: %s", closedTrade.toString()));
        closedTrades.addClosedTrades(closedTrade);
    }

    public void onBarWithTime(BarWithTime bwt) {
        States state = getState(bwt);
        ChangesAnomalyStateTransition stateTransition = getStateTransition(bwt);

        Changes.AnalyzeParameter parameter = Changes.AnalyzeParameter.builder()
                .windowSize(Duration.ofMinutes(20))
                .build();
        Changes.AnalyzeResult analysis = Changes.analyze(barWithTimeStream.keyedBarWithTimeSlidingWindows.get(BarWithTimeStream.bwtToKeyString(bwt)), parameter);
        ChangesAnomalyStateTransition.HandleStateResult result = stateTransition.handleState(state, analysis);
        if (result.closedTrade != null) {
            onClosedTrade(result.closedTrade);
        }
    }
}