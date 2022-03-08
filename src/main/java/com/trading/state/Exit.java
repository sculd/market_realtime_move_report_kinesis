package com.trading.state;

import lombok.Builder;

public class Exit {
    public Position position;
    public double targetPrice;
    public Common.PriceSnapshot exitPriceSnapshot = Common.PriceSnapshot.builder().build();

    /*
    public enum ExecuteResult {
        SUCCESS,
        FAIL;
    }

     */

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

    public ExecuteResult execute() {
        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return ExecuteResult.builder().result(ExecuteResult.Result.SUCCESS)
                .build();
    }
}
