package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

public class TakeProfitPlan {
    public Common.PriceSnapshot entryPriceSnapShot;
    public enum TakeProfitType {
        TAKE_PROFIT_FROM_ENTRY,
        NO_TAKE_PROFIT;
    }
    public TakeProfitType takeProfitType;

    public Seek seek = new Seek();

    @Builder
    public static class TakeProfitPlanInitParameter {
        public TakeProfitType takeProfitType;
        public double targetReturnFromEntry;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TakeProfitPlanInitParameter.class)
                    .add("takeProfitType", takeProfitType)
                    .add("targetReturnFromEntry", targetReturnFromEntry)
                    .toString();
        }
    }

    public void init(Position position, TakeProfitPlanInitParameter takeProfitPlanInitParameter) {
        takeProfitType = takeProfitPlanInitParameter.takeProfitType;
        if (takeProfitType == TakeProfitType.NO_TAKE_PROFIT) {
            return;
        }

        this.entryPriceSnapShot = position.entryPriceSnapshot;
        Common.ChangeType changeType = Common.ChangeType.JUMP;
        double sign = 1.0;
        switch (position.positionSideType) {
            case LONG:
                changeType = Common.ChangeType.JUMP;
                break;
            case SHORT:
                changeType = Common.ChangeType.DROP;
                sign = -1.0;
                break;
        }
        double referencePrice = position.entryPriceSnapshot.price;
        seek.init(changeType, referencePrice, sign * takeProfitPlanInitParameter.targetReturnFromEntry);
    }
}
