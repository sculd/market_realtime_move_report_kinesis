package com.changesanomalytrading.state.stream;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.analysis.Changes;

import com.trading.state.States;

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


    public ChangesAnomalyTradingStream(BarWithTimeStream barWithTimeStream) {
        this.barWithTimeStream = barWithTimeStream;
    }

    States lookupState(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStates.containsKey(key)) {
            keyedStates.put(key, new States(bwt.bar.market, bwt.bar.symbol));
        }
        return keyedStates.get(key);
    }

    ChangesAnomalyStateTransition lookupStateTransition(BarWithTime bwt) {
        String key = BarWithTimeStream.bwtToKeyString(bwt);
        if (!keyedStateTransition.containsKey(key)) {
            keyedStateTransition.put(key,
                    new ChangesAnomalyStateTransition(bwt.bar.market, bwt.bar.symbol, ChangesAnomalyStateTransition.Parameter.builder().build()));
        }
        return keyedStateTransition.get(key);
    }

    public void onBarWithTime(BarWithTime bwt) {
        States state = lookupState(bwt);
        ChangesAnomalyStateTransition stateTransition = lookupStateTransition(bwt);

        Changes.AnalyzeParameter parameter = Changes.AnalyzeParameter.builder()
                .windowSize(Duration.ofMinutes(20))
                .build();
        Changes.AnalyzeResult analysis = Changes.analyze(barWithTimeStream.keyedBarWithTimeSlidingWindows.get(BarWithTimeStream.bwtToKeyString(bwt)), parameter);
        stateTransition.handleState(state, analysis);
    }
}
