package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.util.Time;
import lombok.Builder;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Common {

    public enum ChangeType {
        JUMP,
        DROP;

        private static final Map<String, ChangeType> ENUM_MAP = Stream.of(ChangeType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static ChangeType of(final String name) {
            return ENUM_MAP.getOrDefault(name, JUMP);
        }
    }

    public enum PositionSideType {
        LONG,
        SHORT;
        
        private static final Map<String, PositionSideType> ENUM_MAP = Stream.of(PositionSideType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static PositionSideType of(final String name) {
            return ENUM_MAP.getOrDefault(name, LONG);
        }
    }

    @Builder
    public static class PriceSnapshot {
        public double price;
        public long epochSeconds;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(PriceSnapshot.class)
                    .add("price", price)
                    .add("epochSeconds", Time.fromEpochSecondsToDateTimeStr(epochSeconds))
                    .toString();
        }
    }
}
