package com.trading.state;

public class Exit {
    public Position position;
    public double targetPrice;
    public Common.PriceSnapshot exitPriceSnapshot = Common.PriceSnapshot.builder().build();

    public enum ExecuteResult {
        SUCCESS,
        FAIL;
    }

    public void init(Position position, double targetPrice) {
        this.position = position;
        this.targetPrice = targetPrice;
    }

    public ExecuteResult execute() {
        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return ExecuteResult.SUCCESS;
    }
}
