package com.changesanomalytrading.state.stream;

import com.google.common.util.concurrent.Monitor;
import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.trading.performance.ClosedTrade;
import com.trading.performance.ClosedTrades;

import com.trading.state.*;

import com.changesanomalytrading.transition.ChangesAnomalyStateTransition;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    public Map<String, States> keyedStates = new HashMap<>();
    public Map<String, ChangesAnomalyStateTransition> keyedStateTransition = new HashMap<>();
    public ClosedTrades closedTrades = new ClosedTrades();
    Monitor mutex = new Monitor();

    @Builder
    public static class ChangesAnomalyTradingStreamInitParameter {
        public States.StatesInitParameter statesInitParameter;
        public ChangesAnomalyStateTransition.TransitionInitParameter transitionInitParameter;
    }
    ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;

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

    ChangesAnomalyStateTransition getStateTransition(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStateTransition.containsKey(key)) {
            keyedStateTransition.put(key,
                    new ChangesAnomalyStateTransition(bwt.bar.market, bwt.bar.symbol, changesAnomalyTradingStreamInitParameter.transitionInitParameter));
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
            ChangesAnomalyStateTransition stateTransition = getStateTransition(bwt);

            BarWithTimeSlidingWindow barWithTimeSlidingWindow = barWithTimeStream.keyedBarWithTimeSlidingWindows.get(BarWithTimeStream.bwtToKeyString(bwt));
            ChangesAnomalyStateTransition.HandleStateResult result = stateTransition.handleState(state, barWithTimeSlidingWindow);
            if (result.closedTrade != null) {
                onClosedTrade(result.closedTrade);
            }
        } finally {
            mutex.leave();
        }
    }
}
