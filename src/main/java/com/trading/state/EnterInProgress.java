package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Analyses;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import java.util.Arrays;

//@Builder
@SuperBuilder
public class EnterInProgress {
    public String market;
    public String symbol;

    public String orderID;

    Common.PriceSnapshot entryPriceSnapShot;
    public Common.PositionSideType positionSideType;
    public double targetPrice;
    public double targetVolume;

    @Builder.Default
    public TimeoutPlan timeoutPlan = new TimeoutPlan();

    public ExitPlan.ExitPlanInitParameter exitPlanInitParameter;

    @Builder
    public static class EnterInProgressInitParameter {
        public TimeoutPlan.TimeoutPlanInitParameter timeoutPlanInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(EnterInProgressInitParameter.class)
                    .add("timeoutPlanInitParameter", timeoutPlanInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            return String.format("%s",
                    TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader());
        }

        public String toCsvLine() {
            return String.format("%s",
                    timeoutPlanInitParameter.toCsvLine());
        }

        static public EnterInProgressInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l = TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader().split(",").length;
            String[] timeoutPlanInitParameter = Arrays.copyOfRange(columns, 0, l);

            return EnterInProgressInitParameter.builder()
                    .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.fromCsvLine(String.join(",", timeoutPlanInitParameter)))
                    .build();
        }
    }
    EnterInProgressInitParameter orderInProgressPlanInitParameter;

    public void init(String orderID, Common.PriceSnapshot entryPriceSnapShot, Common.PositionSideType positionSideType, double targetPrice, double targetVolume) {
        this.orderID = orderID;
        this.entryPriceSnapShot = entryPriceSnapShot;
        this.positionSideType = positionSideType;
        this.targetPrice = targetPrice;
        this.targetVolume = targetVolume;
        timeoutPlan.init(entryPriceSnapShot, orderInProgressPlanInitParameter.timeoutPlanInitParameter);
    }

    @Builder
    public static class EnterInProgressStatus {
        public enum Status {
            ORDER_IN_PROGRESS,
            TIMEOUT,
            ORDER_COMPLETE;
        }

        public ExitPlan exitPlan;
        public Position position;
        public Status status;
    }

    public EnterInProgressStatus getProgressStatus(Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        Position position = Position.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(positionSideType)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(targetPrice)
                        .epochSeconds(entryPriceSnapshot.epochSeconds)
                        .build())
                .volume(targetVolume)
                .analysesUponEnter(analysesUponEnter)
                .build();
        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .exitPlanInitParameter(exitPlanInitParameter)
                .position(position)
                .build();
        exitPlan.init(position);
        return EnterInProgressStatus.builder()
                .exitPlan(exitPlan)
                .position(position)
                .status(EnterInProgressStatus.Status.ORDER_COMPLETE).build();
    }
}
