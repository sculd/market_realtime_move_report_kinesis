package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Builder
public class ExitPlan {
    public String market;
    public String symbol;

    public Position position;
    @Builder.Default
    public TakeProfitPlan takeProfitPlan = new TakeProfitPlan();
    @Builder.Default
    public StopLossPlan stopLossPlan = new StopLossPlan();
    @Builder.Default
    public TimeoutPlan timeoutPlan = new TimeoutPlan();

    @Builder
    public static class ExitPlanInitParameter {
        public TakeProfitPlan.TakeProfitPlanInitParameter takeProfitPlanInitParameter;
        public StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter;
        public TimeoutPlan.TimeoutPlanInitParameter timeoutPlanInitParameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ExitPlanInitParameter.class)
                    .add("takeProfitPlanInitParameter", takeProfitPlanInitParameter)
                    .add("stopLossPlanInitParameter", stopLossPlanInitParameter)
                    .add("timeoutPlanInitParameter", timeoutPlanInitParameter)
                    .toString();
        }

        static public String toCsvHeader() {
            return String.format("%s,%s,%s",
                    TakeProfitPlan.TakeProfitPlanInitParameter.toCsvHeader(),
                    StopLossPlan.StopLossPlanInitParameter.toCsvHeader(),
                    TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader());
        }

        public String toCsvLine() {
            return String.format("%s,%s,%s",
                    takeProfitPlanInitParameter.toCsvLine(),
                    stopLossPlanInitParameter.toCsvLine(),
                    timeoutPlanInitParameter.toCsvLine());
        }

        static public ExitPlanInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            int l1 = TakeProfitPlan.TakeProfitPlanInitParameter.toCsvHeader().split(",").length;
            int l2 = l1 + StopLossPlan.StopLossPlanInitParameter.toCsvHeader().split(",").length;
            int l3 = l2 + TimeoutPlan.TimeoutPlanInitParameter.toCsvHeader().split(",").length;
            String[] takeProfitPlanInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
            String[] stopLossPlanInitParameter = Arrays.copyOfRange(columns, l1, l2);
            String[] timeoutPlanInitParameter = Arrays.copyOfRange(columns, l2, l3);

            return ExitPlanInitParameter.builder()
                    .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.fromCsvLine(String.join(",", takeProfitPlanInitParameterColumns)))
                    .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.fromCsvLine(String.join(",", stopLossPlanInitParameter)))
                    .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.fromCsvLine(String.join(",", timeoutPlanInitParameter)))
                    .build();
        }
    }
    ExitPlanInitParameter exitPlanInitParameter;

    public void init(Position position) {
        this.position = position;
        takeProfitPlan.init(position, exitPlanInitParameter.takeProfitPlanInitParameter);
        stopLossPlan.init(position, exitPlanInitParameter.stopLossPlanInitParameter);
        timeoutPlan.init(position.entryPriceSnapshot, exitPlanInitParameter.timeoutPlanInitParameter);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ExitPlan.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("position", position)
                .add("takeProfitPlan", takeProfitPlan)
                .add("stopLossPlan", stopLossPlan)
                .add("timeoutPlan", timeoutPlan)
                .toString();
    }
}
