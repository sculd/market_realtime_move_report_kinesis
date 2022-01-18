package com.trading.position;

import lombok.Builder;

public class PositionCommon {
    @Builder
    public static class PriceSnapshot {
        public double price;
        public long epochSeconds;
    }
}
