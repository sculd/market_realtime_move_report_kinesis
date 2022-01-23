package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Changes;
import com.trading.performance.ClosedTrade;

public class States {
    public String market;
    public String symbol;

    public enum StateType {
        IDLE,
        ENTER_PLAN,
        ENTER,
        IN_POSITION,
        EXIT;
    }
    public StateType stateType;

    public EnterPlan enterPlan;
    public Enter enter;
    public Position position;
    public ExitPlan exitPlan;
    public Exit exit;
    public ClosedTrade closedTrade;

    public States(String market, String symbol) {
        this.market = market;
        this.symbol = symbol;

        stateType = StateType.IDLE;

        enterPlan = new EnterPlan();
        enter = Enter.builder().market(market).symbol(symbol).build();
        position = Position.builder().market(market).symbol(symbol).build();
        exitPlan = ExitPlan.builder().market(market).symbol(symbol).build();
        exit = new Exit();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Changes.AnalyzeResult.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("stateType", stateType)
                .toString();
    }
}
