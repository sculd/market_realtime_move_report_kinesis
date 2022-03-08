package com.trading.state;

import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.ExitPlan.ExitPlanInitParameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

//@AllArgsConstructor
@SuperBuilder
public class Enter {
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public double targetPrice;
    public double targetVolume;
    public Common.PriceSnapshot entryPriceSnapshot;
    public Analyses analysesUponEnter;

    @Builder
    public static class ExecuteResult {
        public enum Result {
            SUCCESS,
            FAIL;
        }

        public String orderID;
        public Result result;
    }

    public ExecuteResult execute(Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        this.entryPriceSnapshot = entryPriceSnapshot;
        this.analysesUponEnter = analysesUponEnter;
        return ExecuteResult.builder()
                .orderID("dummy-id")
                .result(ExecuteResult.Result.SUCCESS)
                .build();
    }
}
