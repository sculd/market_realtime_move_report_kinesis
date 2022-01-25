package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.util.Time;
import lombok.Builder;

public class Common {

    public enum ChangeType {
        JUMP,
        DROP;
    }

    public enum PositionSideType {
        LONG,
        SHORT;
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
