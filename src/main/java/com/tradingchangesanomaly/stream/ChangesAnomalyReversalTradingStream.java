package com.tradingchangesanomaly.stream;

import com.google.common.util.concurrent.Monitor;
import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.trading.performance.ClosedTrade;
import com.trading.performance.ClosedTrades;

import com.trading.state.*;

import com.tradingchangesanomaly.state.transition.ChangesAnomalyReversalStateTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyReversalTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyReversalTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    public Map<String, States> keyedStates = new HashMap<>();
    public Map<String, ChangesAnomalyReversalStateTransition> keyedStateTransition = new HashMap<>();
    public ClosedTrades closedTrades = new ClosedTrades();
    Monitor mutex = new Monitor();

    public ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;

    public void init(ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        this.changesAnomalyTradingStreamInitParameter = changesAnomalyTradingStreamInitParameter;
    }

    public ChangesAnomalyReversalTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    protected States createNewStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        return new States(market, symbol, statesInitParameter);
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
