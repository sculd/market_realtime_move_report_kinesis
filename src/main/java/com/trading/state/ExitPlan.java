package com.trading.state;

import lombok.Builder;

@Builder
public class ExitPlan {
    public String market;
    public String symbol;

    public Position position;
    @Builder.Default
    public TakeProfitPlan takeProfitPlan = new TakeProfitPlan();
    @Builder.Default
    public StopLossPlan stopLossPlan = new StopLossPlan();

    @Builder
    public static class ExitPlanInitParameter {
        public TakeProfitPlan.TakeProfitPlanInitParameter takeProfitPlanInitParameter;
        public StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter;
    }
    ExitPlanInitParameter exitPlanInitParameter;

    public void init(Position position) {
        this.position = position;
        takeProfitPlan.init(position, exitPlanInitParameter.takeProfitPlanInitParameter);
        stopLossPlan.init(position, exitPlanInitParameter.stopLossPlanInitParameter);
    }
}
