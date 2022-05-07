package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;

public class TakeProfitPlan {
    public Common.PriceSnapshot entryPriceSnapShot;
    public enum TakeProfitType {
        TAKE_PROFIT_FROM_ENTRY,
        NO_TAKE_PROFIT;

        private static final Map<String, TakeProfitType> ENUM_MAP = Stream.of(TakeProfitType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static TakeProfitType of(final String name) {
            return ENUM_MAP.getOrDefault(name, TAKE_PROFIT_FROM_ENTRY);
        }
    }
    public TakeProfitType takeProfitType;

    public SeekPrice seekPrice = new SeekPrice();

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

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add("takeProfitType");
            headers.add("targetReturnFromEntry");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%s", takeProfitType));
            columns.add(String.format("%f", targetReturnFromEntry));
            return String.join(",", columns);
        }

        static public TakeProfitPlanInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            return TakeProfitPlanInitParameter.builder()
                    .takeProfitType(TakeProfitType.of(columns[0]))
                    .targetReturnFromEntry(Double.parseDouble(columns[1]))
                    .build();
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
        seekPrice.init(changeType, referencePrice, sign * takeProfitPlanInitParameter.targetReturnFromEntry);
    }

    public boolean getIfTriggered(double price) {
        if (takeProfitType == TakeProfitType.NO_TAKE_PROFIT) {
            return false;
        }

        return seekPrice.getIfTriggered(price);
    }
}
