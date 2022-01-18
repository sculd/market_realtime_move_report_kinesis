package com.trading.performance;

import com.trading.state.Common;
import lombok.Builder;

@Builder
public class ClosedTrade {
    public String market;
    public String symbol;

    @Builder
    public static class PriceSnapshot {
        public double price;
        public long epochSeconds;
    }

    public Common.PositionType positionType;
    public PriceSnapshot entryPriceSnapshot;
    public double volume;
    public PriceSnapshot exitPriceSnapshot;

    public double getPnL() {
        switch (positionType) {
            case SHORT:
                return (entryPriceSnapshot.price - exitPriceSnapshot.price) / entryPriceSnapshot.price;
            case LONG:
                return (exitPriceSnapshot.price - entryPriceSnapshot.price) / entryPriceSnapshot.price;
        }
        return 0.0;
    }

    public double getGainLossFiat() {
        return getPnL() * volume;
    }
}
