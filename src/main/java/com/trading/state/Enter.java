package com.trading.state;

import lombok.Builder;

@Builder
public class Enter {
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public double targetPrice;
    public double targetVolume;

    @Builder
    public static class ExecuteResult {
        public enum Result {
            SUCCESS,
            FAIL;
        }

        public Position position;
        public ExitPlan exitPlan;
        public Result result;
    }

    public ExecuteResult execute() {
        Position position = Position.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(positionSideType)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(targetPrice)
                        .epochSeconds(java.time.Instant.now().getEpochSecond())
                        .build())
                .volume(targetVolume)
                .build();
        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .position(position)
                .build();
        ExitPlan.ExitPlanInitParameter exitPlanInitParameter = ExitPlan.ExitPlanInitParameter.builder()
                .takeProfitPlanInitParameter(ExitPlan.TakeProfitPlan.TakeProfitPlanInitParameter.builder()
                        .takeProfitType(ExitPlan.TakeProfitPlan.TakeProfitType.TAKE_PROFIT_FROM_ENTRY)
                        .targetReturnFromEntry(0.03)
                        .build())
                .stopLossPlanInitParameter(ExitPlan.StopLossPlan.StopLossPlanInitParameter.builder()
                        .stopLossType(ExitPlan.StopLossPlan.StopLossType.STOP_LOSS_FROM_TOP_PROFIT)
                        .targetStopLoss(-0.02)
                        .build())
                .build();
        exitPlan.init(position, exitPlanInitParameter);
        return ExecuteResult.builder().result(ExecuteResult.Result.SUCCESS)
                .position(position)
                .exitPlan(exitPlan)
                .build();
    }
}
