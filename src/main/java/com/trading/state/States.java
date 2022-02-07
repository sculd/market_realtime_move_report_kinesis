package com.trading.state;

import com.google.common.base.MoreObjects;
import com.trading.performance.ClosedTrade;
import com.tradingchangesanomaly.state.transition.ChangesAnomalyStateTransition;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class States {
    public String market;
    public String symbol;

    public enum StateType {
        IDLE,
        ENTER_PLAN,
        ENTER,
        IN_POSITION,
        EXIT,
        TRADE_CLOSED;
    }
    public StateType stateType;

    public EnterPlan enterPlan;
    public Enter enter;
    public Position position;
    public ExitPlan exitPlan;
    public Exit exit;
    public ClosedTrade closedTrade;

    @Builder
    public static class StatesInitParameter {
        public EnterPlan.EnterPlanInitParameter enterPlanInitParameter;
        public ExitPlan.ExitPlanInitParameter exitPlanInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(StatesInitParameter.class)
                    .add("enterPlanInitParameter", enterPlanInitParameter)
                    .add("exitPlanInitParameter", exitPlanInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            return String.format("%s,%s",
                    EnterPlan.EnterPlanInitParameter.toCsvHeader(),
                    ExitPlan.ExitPlanInitParameter.toCsvHeader());
        }

        public String toCsvLine() {
            return String.format("%s,%s",
                    enterPlanInitParameter.toCsvLine(),
                    exitPlanInitParameter.toCsvLine());
        }

        static public StatesInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l1 = EnterPlan.EnterPlanInitParameter.toCsvHeader().split(",").length;
            int l2 = l1 + ExitPlan.ExitPlanInitParameter.toCsvHeader().split(",").length;
            String[] enterPlanInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
            String[] exitPlanInitParameterColumns = Arrays.copyOfRange(columns, l1, l2);

            return StatesInitParameter.builder()
                    .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.fromCsvLine(String.join(",", enterPlanInitParameterColumns)))
                    .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.fromCsvLine(String.join(",", exitPlanInitParameterColumns)))
                    .build();
        }

    }
    StatesInitParameter statesInitParameter;

    public States(String market, String symbol, StatesInitParameter statesInitParameter) {
        this.market = market;
        this.symbol = symbol;
        this.statesInitParameter = statesInitParameter;

        stateType = StateType.IDLE;

        enterPlan = EnterPlan.builder().enterPlanInitParameter(statesInitParameter.enterPlanInitParameter).build();
        enter = Enter.builder().market(market).symbol(symbol).exitPlanInitParameter(statesInitParameter.exitPlanInitParameter).build();
        // position, exitPlan are returned from enter.execute
        exit = new Exit();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(States.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("stateType", stateType)
                .toString();
    }
}
