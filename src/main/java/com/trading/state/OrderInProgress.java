package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;
import java.util.Arrays;

@Builder
public class OrderInProgress {
    public String orderID;
    Common.PriceSnapshot entryPriceSnapShot;

    @Builder.Default
    public TimeoutPlan timeoutPlan = new TimeoutPlan();

    @Builder
    public static class OrderInProgressInitParameter {
        public TimeoutPlan.TimeoutPlanInitParameter timeoutPlanInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderInProgressInitParameter.class)
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

        static public OrderInProgressInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l = TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader().split(",").length;
            String[] timeoutPlanInitParameter = Arrays.copyOfRange(columns, 0, l);

            return OrderInProgressInitParameter.builder()
                    .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.fromCsvLine(String.join(",", timeoutPlanInitParameter)))
                    .build();
        }
    }
    OrderInProgressInitParameter orderInProgressPlanInitParameter;

    public void init(String orderID, Common.PriceSnapshot entryPriceSnapShot) {
        this.orderID = orderID;
        this.entryPriceSnapShot = entryPriceSnapShot;
        timeoutPlan.init(entryPriceSnapShot, orderInProgressPlanInitParameter.timeoutPlanInitParameter);
    }

    public enum OrderInProgressStatus {
        ORDER_IN_PROGRESS,
        TIMEOUT,
        ORDER_COMPLETE;
    }

    public OrderInProgressStatus getProgressStatus() {
        return OrderInProgressStatus.ORDER_COMPLETE;
    }
}
