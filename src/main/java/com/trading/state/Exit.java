package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketapi.binance.response.MarginAccountBorrow;
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

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ExecuteResult.class)
                    .add("orderID", orderID)
                    .add("result", result)
                    .toString();
        }
    }

    public void init(Position position, double targetPrice) {
        this.position = position;
        this.targetPrice = targetPrice;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Exit.class)
                .add("position", position)
                .add("targetPrice", targetPrice)
                .add("exitPriceSnapshot", exitPriceSnapshot)
                .add("analysesUponExit", analysesUponExit)
                .toString();
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
