package com.changesanomalytrading.transition;

import com.marketsignal.timeseries.analysis.Changes;
import com.trading.state.States;
import com.trading.state.transition.TradingStateTransition;
import lombok.Builder;

public class ChangesAnomalyTradingStateTransition extends TradingStateTransition {
    @Builder
    static public class Parameter {
        public Parameter() {
        }
    }

    Parameter parameter;

    public ChangesAnomalyTradingStateTransition(Parameter parameter) {
        this.parameter = parameter;
    }

    /*
     * update `seek` of state.
     */
    public void seek(States state, Changes.AnalyzeResult analysis) {
        //if (analysis.)
    }

}
