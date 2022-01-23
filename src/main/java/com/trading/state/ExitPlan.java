package com.trading.state;

import lombok.Builder;

@Builder
public class ExitPlan {
    public String market;
    public String symbol;

    public Position position;
    @Builder.Default
    public TakeProfitPlan takeProfitPlan = new TakeProfitPlan();
    @Builder.Default
    public StopLossPlan stopLossPlan = new StopLossPlan();

    @Builder
    public static class ExitPlanInitParameter {
        public TakeProfitPlan.TakeProfitPlanInitParameter takeProfitPlanInitParameter;
        public StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter;
    }

    public void init(Position position, ExitPlanInitParameter exitPlanInitParameter) {
        this.position = position;
        takeProfitPlan.init(position, exitPlanInitParameter.takeProfitPlanInitParameter);
        stopLossPlan.init(position, exitPlanInitParameter.stopLossPlanInitParameter);
    }

    public static class TakeProfitPlan {
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
        }

        public void init(Position position, TakeProfitPlanInitParameter takeProfitPlanInitParameter) {
            takeProfitType = takeProfitPlanInitParameter.takeProfitType;
            if (takeProfitType == TakeProfitType.NO_TAKE_PROFIT) {
                return;
            }

            this.entryPriceSnapShot = position.entryPriceSnapshot;
            Common.ChangeType changeType = Common.ChangeType.JUMP;
            switch (position.positionSideType) {
                case LONG:
                    changeType = Common.ChangeType.JUMP;
                    break;
                case SHORT:
                    changeType = Common.ChangeType.DROP;
                    break;
            }
            double referencePrice = position.entryPriceSnapshot.price;
            seek.init(changeType, referencePrice, takeProfitPlanInitParameter.targetReturnFromEntry);
        }
    }

    public static class StopLossPlan {
        public Common.PriceSnapshot entryPriceSnapShot;
        public double topPrice;

        public enum StopLossType {
            STOP_LOSS_FROM_TOP_PROFIT,
            STOP_LOSS_FROM_ENTRY;
        }
        public StopLossType stopLossType;

        public double stopLossPrice;
        public void setStopLoss(double loss) {
            switch (stopLossType) {
                case STOP_LOSS_FROM_ENTRY:
                    stopLossPrice = entryPriceSnapShot.price * (1.0 + loss);
                    break;
                case STOP_LOSS_FROM_TOP_PROFIT:
                    stopLossPrice = topPrice * (1.0 + loss);
                    break;
            }
        }

        public Seek seek = new Seek();

        @Builder
        public static class StopLossPlanInitParameter {
            public StopLossPlan.StopLossType stopLossType;
            public double targetStopLoss;
        }

        public void init(Position position, StopLossPlanInitParameter stopLossPlanInitParameter) {
            this.entryPriceSnapShot = position.entryPriceSnapshot;
            stopLossType = stopLossPlanInitParameter.stopLossType;
            double referencePrice = position.entryPriceSnapshot.price;
            switch (stopLossType) {
                case STOP_LOSS_FROM_TOP_PROFIT:
                    referencePrice = topPrice;
                    break;
                case STOP_LOSS_FROM_ENTRY:
                    break;
            }

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

}
