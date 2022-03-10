package com.tradingchangesanomaly.stream;

import com.google.common.base.MoreObjects;
import com.trading.state.States;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import lombok.Builder;

import java.util.Arrays;

@Builder
public class ChangesAnomalyTradingStreamUtil {
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

        static public ChangesAnomalyTradingStreamInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l1 = States.StatesInitParameter.toCsvHeader().split(",").length;
            int l2 = l1 + ChangesAnomalyStateTransition.TransitionInitParameter.toCsvHeader().split(",").length;
            String[] statesInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
            String[] transitionInitParameterColumns = Arrays.copyOfRange(columns, l1, l2);

            return ChangesAnomalyTradingStreamInitParameter.builder()
                    .statesInitParameter(States.StatesInitParameter.fromCsvLine(String.join(",", statesInitParameterColumns)))
                    .transitionInitParameter(ChangesAnomalyStateTransition.TransitionInitParameter.fromCsvLine(String.join(",", transitionInitParameterColumns)))
                    .build();
        }

        public String toFilenamePrefix() {
            return String.format("%s_", toCsvLine());
        }
    }
}
