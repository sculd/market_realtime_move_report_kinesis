package com.trading.state;

import com.google.common.base.MoreObjects;
import com.trading.performance.ClosedTrade;
import lombok.Builder;

import java.util.Arrays;

public class States {
    public String market;
    public String symbol;

    public enum StateType {
        IDLE,
        ENTER_PLAN,
        ENTER,
        ENTER_ORDER_IN_PROGRESS,
        IN_POSITION,
        EXIT,
        EXIT_ORDER_IN_PROGRESS,
        TRADE_CLOSED;
    }
    public StateType stateType;

    public EnterPlan enterPlan;
    public Enter enter;
    public OrderInProgress enterInProgress;
    public Position position;
    public ExitPlan exitPlan;
    public Exit exit;
    public OrderInProgress exitInProgress;
    public ClosedTrade closedTrade;

    @Builder
    public static class StatesInitParameter {
        public EnterPlan.EnterPlanInitParameter enterPlanInitParameter;
        public OrderInProgress.OrderInProgressInitParameter enterInProgressInitParameter;
        public ExitPlan.ExitPlanInitParameter exitPlanInitParameter;
        public OrderInProgress.OrderInProgressInitParameter exitInProgressInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(StatesInitParameter.class)
                    .add("enterPlanInitParameter", enterPlanInitParameter)
                    .add("enterPlanInitParameter", enterInProgressInitParameter)
                    .add("exitPlanInitParameter", exitPlanInitParameter)
                    .add("exitInProgressInitParameter", exitInProgressInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            return String.format("%s,%s",
                    EnterPlan.EnterPlanInitParameter.toCsvHeader(),
                    OrderInProgress.OrderInProgressInitParameter.toCsvHeader(),
                    ExitPlan.ExitPlanInitParameter.toCsvHeader(),
                    OrderInProgress.OrderInProgressInitParameter.toCsvHeader());
        }

        public String toCsvLine() {
            return String.format("%s,%s",
                    enterPlanInitParameter.toCsvLine(),
                    enterInProgressInitParameter.toCsvLine(),
                    exitPlanInitParameter.toCsvLine(),
                    exitInProgressInitParameter.toCsvLine());
        }

        static public StatesInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l1 = EnterPlan.EnterPlanInitParameter.toCsvHeader().split(",").length;
            int l2 = l1 + OrderInProgress.OrderInProgressInitParameter.toCsvHeader().split(",").length;
            int l3 = l2 + ExitPlan.ExitPlanInitParameter.toCsvHeader().split(",").length;
            int l4 = l3 + OrderInProgress.OrderInProgressInitParameter.toCsvHeader().split(",").length;
            String[] enterPlanInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
            String[] enterInProgressInitParameterColumns = Arrays.copyOfRange(columns, l1, l2);
            String[] exitPlanInitParameterColumns = Arrays.copyOfRange(columns, l2, l3);
            String[] exitInProgressInitParameterColumns = Arrays.copyOfRange(columns, l3, l4);

            return StatesInitParameter.builder()
                    .enterPlanInitParameter(EnterPlan.EnterPlanInitParameter.fromCsvLine(String.join(",", enterPlanInitParameterColumns)))
                    .enterInProgressInitParameter(OrderInProgress.OrderInProgressInitParameter.fromCsvLine(String.join(",", enterInProgressInitParameterColumns)))
                    .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.fromCsvLine(String.join(",", exitPlanInitParameterColumns)))
                    .enterInProgressInitParameter(OrderInProgress.OrderInProgressInitParameter.fromCsvLine(String.join(",", exitInProgressInitParameterColumns)))
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
        enterInProgress = OrderInProgress.builder().orderInProgressPlanInitParameter(statesInitParameter.enterInProgressInitParameter).build();
        // position, exitPlan are returned from enter.execute
        exit = new Exit();
        exitInProgress = OrderInProgress.builder().orderInProgressPlanInitParameter(statesInitParameter.exitInProgressInitParameter).build();
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
