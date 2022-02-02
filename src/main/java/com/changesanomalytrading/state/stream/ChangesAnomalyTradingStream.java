package com.changesanomalytrading.state.stream;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.Monitor;
import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.trading.performance.ClosedTrade;
import com.trading.performance.ClosedTrades;

import com.trading.state.*;

import com.changesanomalytrading.transition.ChangesAnomalyReversalStateTransition;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    public Map<String, States> keyedStates = new HashMap<>();
    public Map<String, ChangesAnomalyReversalStateTransition> keyedStateTransition = new HashMap<>();
    public ClosedTrades closedTrades = new ClosedTrades();
    Monitor mutex = new Monitor();

    @Builder
    public static class ChangesAnomalyTradingStreamInitParameter {
        public States.StatesInitParameter statesInitParameter;
        public ChangesAnomalyReversalStateTransition.TransitionInitParameter transitionInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ChangesAnomalyTradingStreamInitParameter.class)
                    .add("statesInitParameter", statesInitParameter)
                    .add("transitionInitParameter", transitionInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add("enterPlanInitParameter.seekReverseChangeAmplitude");
            headers.add("exitPlanInitParameter.takeProfitPlanInitParameter.takeProfitType");
            headers.add("exitPlanInitParameter.takeProfitPlanInitParameter.targetReturnFromEntry");
            headers.add("exitPlanInitParameter.stopLossPlanInitParameter.stopLossType");
            headers.add("exitPlanInitParameter.stopLossPlanInitParameter.targetStopLoss");
            headers.add("exitPlanInitParameter.timeoutPlanInitParameter.expirationDuration");
            headers.add("transitionInitParameter.maxJumpThreshold");
            headers.add("transitionInitParameter.minDropThreshold");
            headers.add("transitionInitParameter.changeAnalysisWindow");
            headers.add("transitionInitParameter.triggerAnomalyType");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%f", statesInitParameter.enterPlanInitParameter.seekReverseChangeAmplitude));
            columns.add(String.format("%s", statesInitParameter.exitPlanInitParameter.takeProfitPlanInitParameter.takeProfitType));
            columns.add(String.format("%f", statesInitParameter.exitPlanInitParameter.takeProfitPlanInitParameter.targetReturnFromEntry));
            columns.add(String.format("%s", statesInitParameter.exitPlanInitParameter.stopLossPlanInitParameter.stopLossType));
            columns.add(String.format("%f", statesInitParameter.exitPlanInitParameter.stopLossPlanInitParameter.targetStopLoss));
            columns.add(String.format("%d", statesInitParameter.exitPlanInitParameter.timeoutPlanInitParameter.expirationDuration.toMinutes()));
            columns.add(String.format("%f", transitionInitParameter.maxJumpThreshold));
            columns.add(String.format("%f", transitionInitParameter.minDropThreshold));
            columns.add(String.format("%d", transitionInitParameter.changeAnalysisWindow.toMinutes()));
            columns.add(String.format("%s", transitionInitParameter.triggerAnomalyType));
            return String.join(",", columns);
        }
    }
    public ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;

    public void init(ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        this.changesAnomalyTradingStreamInitParameter = changesAnomalyTradingStreamInitParameter;
    }

    public ChangesAnomalyTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    States getState(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key, new States(bwt.bar.market, bwt.bar.symbol, changesAnomalyTradingStreamInitParameter.statesInitParameter));
        }
        return keyedStates.get(key);
    }

    ChangesAnomalyReversalStateTransition getStateTransition(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStateTransition.containsKey(key)) {
            keyedStateTransition.put(key,
                    new ChangesAnomalyReversalStateTransition(bwt.bar.market, bwt.bar.symbol, changesAnomalyTradingStreamInitParameter.transitionInitParameter));
        }
        return keyedStateTransition.get(key);
    }

    void onClosedTrade(ClosedTrade closedTrade) {
        log.info(String.format("onClosedTrade: %s", closedTrade.toString()));
        closedTrades.addClosedTrades(closedTrade);
    }

    public void onBarWithTime(BarWithTime bwt) {
        mutex.enter();
        try {
            States state = getState(bwt);
            ChangesAnomalyReversalStateTransition stateTransition = getStateTransition(bwt);

            BarWithTimeSlidingWindow barWithTimeSlidingWindow = barWithTimeStream.keyedBarWithTimeSlidingWindows.get(BarWithTimeStream.bwtToKeyString(bwt));
            ChangesAnomalyReversalStateTransition.HandleStateResult result = stateTransition.handleState(state, barWithTimeSlidingWindow);
            if (result.closedTrade != null) {
                onClosedTrade(result.closedTrade);
            }
        } finally {
            mutex.leave();
        }
    }
}
