package com.trading.stream;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.Changes;

import com.trading.state.Action;
import com.trading.state.Common;
import com.trading.state.States;

import com.trading.state.transition.TradingStateTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    TradingStateTransition tradingStateTransition = new TradingStateTransition(TradingStateTransition.Parameter.builder().build());
    public Map<String, States> keyedStates = new HashMap<>();

    public ChangesAnomalyTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    public void onBarWithTime(BarWithTime bwt) {
        Changes.AnalyzeParameter parameter = Changes.AnalyzeParameter.builder()
                .windowSize(Duration.ofMinutes(20))
                .build();;

        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key,
                    States.builder()
                            .market(bwt.bar.market)
                            .symbol(bwt.bar.symbol)
                            .action(Action.builder().actionType(Common.ActionType.IDLE).build())
                            .build());
        }

        Changes.AnalyzeResult analysis = Changes.analyze(barWithTimeStream.keyedBarWithTimeSlidingWindows.get(key), parameter);
        States state = keyedStates.get(key);
        tradingStateTransition.seek(state);
        tradingStateTransition.seekToAction(state, analysis.priceAtAnalysis);
        tradingStateTransition.actionToPositionAndExit(state, analysis.market, analysis.symbol);
        tradingStateTransition.recapClosedTrade(state, analysis.market, analysis.symbol);
    }
}
