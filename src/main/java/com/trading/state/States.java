package com.trading.state;

import com.google.common.base.MoreObjects;
import com.trading.performance.ClosedTrade;
import lombok.Builder;

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
