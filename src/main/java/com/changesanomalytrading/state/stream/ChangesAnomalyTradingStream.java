package com.changesanomalytrading.state.stream;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.Changes;

import com.trading.state.States;

import com.changesanomalytrading.transition.ChangesAnomalyTradingStateTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ChangesAnomalyTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyTradingStream.class);

    BarWithTimeStream barWithTimeStream;
    ChangesAnomalyTradingStateTransition tradingStateTransition = new ChangesAnomalyTradingStateTransition(ChangesAnomalyTradingStateTransition.Parameter.builder().build());
    public Map<String, States> keyedStates = new HashMap<>();

    public ChangesAnomalyTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    public void onBarWithTime(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key, new States(bwt.bar.market, bwt.bar.symbol));
        }

        Changes.AnalyzeParameter parameter = Changes.AnalyzeParameter.builder()
                .windowSize(Duration.ofMinutes(20))
                .build();
        Changes.AnalyzeResult analysis = Changes.analyze(barWithTimeStream.keyedBarWithTimeSlidingWindows.get(key), parameter);

        States state = keyedStates.get(key);
        tradingStateTransition.seek(state, analysis);
        tradingStateTransition.seekToAction(state, bwt.bar.ohlc.close);
        tradingStateTransition.actionToPositionAndExit(state, bwt.bar.market, bwt.bar.symbol);
        tradingStateTransition.recapClosedTrade(state, bwt.bar.market, bwt.bar.symbol);
    }
}
