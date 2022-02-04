package com.trading.state;

import com.google.common.base.MoreObjects;
import com.trading.performance.ClosedTrade;
import lombok.Builder;

import java.util.ArrayList;
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
            List<String> headers = new ArrayList<>();
            headers.add("enterPlanInitParameter.seekChangeAmplitude");
            headers.add("exitPlanInitParameter.takeProfitPlanInitParameter.takeProfitType");
            headers.add("exitPlanInitParameter.takeProfitPlanInitParameter.targetReturnFromEntry");
            headers.add("exitPlanInitParameter.stopLossPlanInitParameter.stopLossType");
            headers.add("exitPlanInitParameter.stopLossPlanInitParameter.targetStopLoss");
            headers.add("exitPlanInitParameter.timeoutPlanInitParameter.expirationDuration");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%f", enterPlanInitParameter.seekChangeAmplitude));
            columns.add(String.format("%s", exitPlanInitParameter.takeProfitPlanInitParameter.takeProfitType));
            columns.add(String.format("%f", exitPlanInitParameter.takeProfitPlanInitParameter.targetReturnFromEntry));
            columns.add(String.format("%s", exitPlanInitParameter.stopLossPlanInitParameter.stopLossType));
            columns.add(String.format("%f", exitPlanInitParameter.stopLossPlanInitParameter.targetStopLoss));
            columns.add(String.format("%d", exitPlanInitParameter.timeoutPlanInitParameter.expirationDuration.toMinutes()));
            return String.join(",", columns);
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
