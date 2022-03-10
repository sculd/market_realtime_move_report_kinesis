package com.trading.state;

import com.marketsignal.timeseries.analysis.Analyses;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class Exit {
    public Position position;
    public double targetPrice;
    @Builder.Default
    public Common.PriceSnapshot exitPriceSnapshot = Common.PriceSnapshot.builder().build();
    public Analyses analysesUponExit;

    @Builder
    public static class ExecuteResult {
        public enum Result {
            SUCCESS,
            FAIL;
        }

        public String orderID;
        public Result result;
    }

    public void init(Position position, double targetPrice) {
        this.position = position;
        this.targetPrice = targetPrice;
    }

    public ExecuteResult execute(Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        exitPriceSnapshot = priceSnapshot;
        analysesUponExit = analyses;
        return ExecuteResult.builder()
                .orderID("dummy-id")
                .result(ExecuteResult.Result.SUCCESS)
                .build();
    }
}
