package com.tradingchangesanomaly.stream;

import com.google.common.base.MoreObjects;
import com.trading.state.States;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import lombok.Builder;

public class ChangesAnomalyTradingStreamCommon {
    @Builder
    public static class ChangesAnomalyTradingStreamInitParameter {
        public States.StatesInitParameter statesInitParameter;
        public ChangesAnomalyStateTransition.TransitionInitParameter transitionInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ChangesAnomalyTradingStreamInitParameter.class)
                    .add("statesInitParameter", statesInitParameter)
                    .add("transitionInitParameter", transitionInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            return String.format("%s,%s", States.StatesInitParameter.toCsvHeader(), ChangesAnomalyStateTransition.TransitionInitParameter.toCsvHeader());
        }

        public String toCsvLine() {
            return String.format("%s,%s", statesInitParameter.toCsvLine(), transitionInitParameter.toCsvLine());
        }
    }
}
