package com.trading.state;

import lombok.Builder;

public class StopLossPlan {
    public Common.PriceSnapshot entryPriceSnapShot;
    public double topPrice;

    public enum StopLossType {
        STOP_LOSS_FROM_TOP_PROFIT,
        STOP_LOSS_FROM_ENTRY;
    }
    public StopLossType stopLossType;

    public Seek seek = new Seek();

    @Builder
    public static class StopLossPlanInitParameter {
        public StopLossPlan.StopLossType stopLossType;
        public double targetStopLoss;
    }

    public void init(Position position, StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter) {
        this.entryPriceSnapShot = position.entryPriceSnapshot;
        stopLossType = stopLossPlanInitParameter.stopLossType;
        topPrice = position.entryPriceSnapshot.price;
        double referencePrice = topPrice;

        Common.ChangeType changeType = Common.ChangeType.JUMP;
        switch (position.positionSideType) {
            case LONG:
                changeType = Common.ChangeType.DROP;
                break;
            case SHORT:
                changeType = Common.ChangeType.JUMP;
                break;
        }
        seek.init(changeType, referencePrice, stopLossPlanInitParameter.targetStopLoss);
    }

    public void onPriceUpdate(double price) {
        topPrice = Math.max(price, topPrice);
        switch (stopLossType) {
            case STOP_LOSS_FROM_TOP_PROFIT:
                seek.updateReferencePrice(topPrice);
                break;
            case STOP_LOSS_FROM_ENTRY:
                break;
        }
    }
}
