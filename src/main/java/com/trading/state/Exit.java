package com.trading.state;

public class Exit {
    public Position position;
    public double targetPrice;
    public Common.PriceSnapshot exitPriceSnapshot;

    public enum ExecuteResult {
        SUCCESS,
        FAIL;
    }

    public void init(Position position, double targetPrice) {
        this.position = position;
        this.targetPrice = targetPrice;
    }

    public ExecuteResult execute() {
        return ExecuteResult.SUCCESS;
    }
}
