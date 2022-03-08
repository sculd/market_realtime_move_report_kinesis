package com.trading.state;

import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.ExitPlan.ExitPlanInitParameter;

import lombok.Builder;

@Builder
public class Enter {
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public double targetPrice;
    public double targetVolume;

    ExitPlanInitParameter exitPlanInitParameter;

    @Builder
    public static class ExecuteResult {
        public enum Result {
            SUCCESS,
            FAIL;
        }

        public Position position;
        public ExitPlan exitPlan;
        public String orderID;
        public Result result;
    }

    public ExecuteResult execute(Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        Position position = Position.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(positionSideType)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(targetPrice)
                        .epochSeconds(priceSnapshot.epochSeconds)
                        .build())
                .volume(targetVolume)
                .analysesUponEnter(analyses)
                .build();
        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .exitPlanInitParameter(exitPlanInitParameter)
                .position(position)
                .build();
        exitPlan.init(position);
        return ExecuteResult.builder().result(ExecuteResult.Result.SUCCESS)
                .position(position)
                .exitPlan(exitPlan)
                .build();
    }
}
