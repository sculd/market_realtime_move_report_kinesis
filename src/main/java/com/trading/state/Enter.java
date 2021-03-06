package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketapi.binance.response.MarginAccountNewOrder;
import com.marketsignal.timeseries.analysis.Analyses;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Enter.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("positionSideType", positionSideType)
                .add("targetPrice", targetPrice)
                .add("targetVolume", targetVolume)
                .add("entryPriceSnapshot", entryPriceSnapshot)
                .add("analysesUponEnter", analysesUponEnter)
                .toString();
    }

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

    public ExecuteResult execute(Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        this.entryPriceSnapshot = entryPriceSnapshot;
        this.analysesUponEnter = analysesUponEnter;
        return ExecuteResult.builder()
                .orderID("dummy-id")
                .result(ExecuteResult.Result.SUCCESS)
                .build();
    }
}
