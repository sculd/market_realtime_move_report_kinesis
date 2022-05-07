package com.trading.state;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeekSpread {
    public enum SpreadSeekType {
        LARGER,
        SMALLER;

        private static final Map<String, Common.ChangeType> ENUM_MAP = Stream.of(Common.ChangeType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static Common.ChangeType of(final String name) {
            return ENUM_MAP.getOrDefault(name, SMALLER);
        }
    }

    public SpreadSeekType spreadSeekType;

    public double seekSpreadToMidRatio;

    public void init(SpreadSeekType spreadSeekType, double seekSpreadToMidRatio) {
        this.spreadSeekType = spreadSeekType;
        this.seekSpreadToMidRatio = seekSpreadToMidRatio;
    }

    public boolean getIfTriggered(double price) {
        /*
        switch (changeType) {
            case JUMP:
                if (price >= seekPrice) {
                    return true;
                }
                break;
            case DROP:
                if (price <= seekPrice) {
                    return true;
                }
                break;
        }

         */
        return false;
    }
}
