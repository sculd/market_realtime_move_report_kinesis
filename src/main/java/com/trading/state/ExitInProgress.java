package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Analyses;
import lombok.Builder;

import java.util.Arrays;

@Builder
public class ExitInProgress {
    public String market;
    public String symbol;

    public String orderID;

    Common.PriceSnapshot entryPriceSnapShot;
    public Common.PositionSideType positionSideType;
    public double targetPrice;
    public double targetVolume;

    @Builder.Default
    public TimeoutPlan timeoutPlan = new TimeoutPlan();

    @Builder
    public static class ExitInProgressInitParameter {
        public TimeoutPlan.TimeoutPlanInitParameter timeoutPlanInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ExitInProgressInitParameter.class)
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

        static public ExitInProgressInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l = TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader().split(",").length;
            String[] timeoutPlanInitParameter = Arrays.copyOfRange(columns, 0, l);

            return ExitInProgressInitParameter.builder()
                    .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.fromCsvLine(String.join(",", timeoutPlanInitParameter)))
                    .build();
        }
    }
    ExitInProgressInitParameter orderInProgressPlanInitParameter;

    public void init(String orderID, Common.PriceSnapshot entryPriceSnapShot, Common.PositionSideType positionSideType, double targetPrice, double targetVolume) {
        this.orderID = orderID;
        this.entryPriceSnapShot = entryPriceSnapShot;
        this.positionSideType = positionSideType;
        this.targetPrice = targetPrice;
        this.targetVolume = targetVolume;
        timeoutPlan.init(entryPriceSnapShot, orderInProgressPlanInitParameter.timeoutPlanInitParameter);
    }

    @Builder
    public static class ExitInProgressStatus {
        public enum Status {
            ORDER_IN_PROGRESS,
            TIMEOUT,
            ORDER_COMPLETE;
        }

        public Status status;
    }

    public ExitInProgressStatus getProgressStatus() {
        return ExitInProgressStatus.builder()
                .status(ExitInProgressStatus.Status.ORDER_COMPLETE).build();
    }
}
