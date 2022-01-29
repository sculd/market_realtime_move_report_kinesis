package com.trading.state;

import com.marketdata.util.Time;
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
    @Builder.Default
    public TimeoutPlan timeoutPlan = new TimeoutPlan();

    @Builder
    public static class ExitPlanInitParameter {
        public TakeProfitPlan.TakeProfitPlanInitParameter takeProfitPlanInitParameter;
        public StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter;
        public TimeoutPlan.TimeoutPlanInitParameter timeoutPlanInitParameter;
    }
    ExitPlanInitParameter exitPlanInitParameter;

    public void init(Position position) {
        this.position = position;
        takeProfitPlan.init(position, exitPlanInitParameter.takeProfitPlanInitParameter);
        stopLossPlan.init(position, exitPlanInitParameter.stopLossPlanInitParameter);
        timeoutPlan.init(position.entryPriceSnapshot, exitPlanInitParameter.timeoutPlanInitParameter);
    }
}
