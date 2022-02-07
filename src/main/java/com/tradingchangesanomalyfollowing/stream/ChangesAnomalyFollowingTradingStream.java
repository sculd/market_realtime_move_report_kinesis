package com.tradingchangesanomalyfollowing.stream;

import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import com.tradingchangesanomalyfollowing.state.transition.ChangesAnomalyFollowingStateTransition;
import com.google.common.util.concurrent.Monitor;
import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.trading.performance.ClosedTrade;
import com.trading.performance.ClosedTrades;
import com.trading.state.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyFollowingTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyFollowingTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    public Map<String, States> keyedStates = new HashMap<>();
    public Map<String, ChangesAnomalyFollowingStateTransition> keyedStateTransition = new HashMap<>();
    public ClosedTrades closedTrades = new ClosedTrades();
    Monitor mutex = new Monitor();

    public ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;

    public void init(ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        this.changesAnomalyTradingStreamInitParameter = changesAnomalyTradingStreamInitParameter;
    }

    public ChangesAnomalyFollowingTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    States getState(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key, new States(bwt.bar.market, bwt.bar.symbol, changesAnomalyTradingStreamInitParameter.statesInitParameter));
        }
        return keyedStates.get(key);
    }

    ChangesAnomalyFollowingStateTransition getStateTransition(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStateTransition.containsKey(key)) {
            keyedStateTransition.put(key,
                    new ChangesAnomalyFollowingStateTransition(bwt.bar.market, bwt.bar.symbol, changesAnomalyTradingStreamInitParameter.transitionInitParameter));
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
            ChangesAnomalyFollowingStateTransition stateTransition = getStateTransition(bwt);

            BarWithTimeSlidingWindow barWithTimeSlidingWindow = barWithTimeStream.keyedBarWithTimeSlidingWindows.get(BarWithTimeStream.bwtToKeyString(bwt));
            ChangesAnomalyFollowingStateTransition.HandleStateResult result = stateTransition.handleState(state, barWithTimeSlidingWindow);
            if (result.closedTrade != null) {
                onClosedTrade(result.closedTrade);
            }
        } finally {
            mutex.leave();
        }
    }
}
