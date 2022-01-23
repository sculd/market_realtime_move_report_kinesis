package com.trading.state;

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
    }
}
