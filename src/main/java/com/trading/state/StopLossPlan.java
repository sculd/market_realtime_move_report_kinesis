package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StopLossPlan {
    public Common.PriceSnapshot entryPriceSnapShot;
    public double topPrice;

    public enum StopLossType {
        STOP_LOSS_FROM_TOP_PROFIT,
        STOP_LOSS_FROM_ENTRY;

        private static final Map<String, StopLossType> ENUM_MAP = Stream.of(StopLossType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static StopLossType of(final String name) {
            return ENUM_MAP.getOrDefault(name, STOP_LOSS_FROM_ENTRY);
        }
    }
    public StopLossType stopLossType;

    public Seek seek = new Seek();

    @Builder
    public static class StopLossPlanInitParameter {
        public StopLossPlan.StopLossType stopLossType;
        public double targetStopLoss;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(StopLossPlanInitParameter.class)
                    .add("stopLossType", stopLossType)
                    .add("targetStopLoss", targetStopLoss)
                    .toString();
        }

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add("stopLossType");
            headers.add("targetStopLoss");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%s", stopLossType));
            columns.add(String.format("%f", targetStopLoss));
            return String.join(",", columns);
        }

        static public StopLossPlanInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            return StopLossPlanInitParameter.builder()
                    .stopLossType(StopLossType.of(columns[0]))
                    .targetStopLoss(Double.parseDouble(columns[1]))
                    .build();
        }
    }

    public void init(Position position, StopLossPlan.StopLossPlanInitParameter stopLossPlanInitParameter) {
        this.entryPriceSnapShot = position.entryPriceSnapshot;
        stopLossType = stopLossPlanInitParameter.stopLossType;
        topPrice = position.entryPriceSnapshot.price;
        double referencePrice = topPrice;

        Common.ChangeType changeType = Common.ChangeType.JUMP;
        double sign = 1.0;
        switch (position.positionSideType) {
            case LONG:
                changeType = Common.ChangeType.DROP;
                break;
            case SHORT:
                changeType = Common.ChangeType.JUMP;
                sign = -1.0;
                break;
        }
        seek.init(changeType, referencePrice, sign * stopLossPlanInitParameter.targetStopLoss);
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StopLossPlan.class)
                .add("entryPriceSnapShot", entryPriceSnapShot)
                .add("topPrice", topPrice)
                .add("stopLossType", stopLossType)
                .add("seek", seek)
                .toString();
    }
}
