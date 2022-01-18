package com.trading.position;

import com.trading.state.Common;
import lombok.Builder;

@Builder
public class Position {
    public Common.PositionToggleType positionToggleType;
    public String market;
    public String symbol;

    public Common.PositionType positionType;
    public PositionCommon.PriceSnapshot entryPriceSnapshot;
    public double volume;

    @Builder
    public static class ExecuteParameter {
        public String market;
        public String symbol;
        public Common.PositionType positionType;
        public double targetPrice;
        public double targetVolume;
    }

    static public Position dryRun(ExecuteParameter executeParameter) {
        Position position = Position.builder()
                .positionToggleType(Common.PositionToggleType.IN_POSITION)
                .market(executeParameter.market)
                .symbol(executeParameter.symbol)
                .positionType(executeParameter.positionType)
                .entryPriceSnapshot(PositionCommon.PriceSnapshot.builder()
                        .price(executeParameter.targetPrice)
                        .epochSeconds(java.time.Instant.now().getEpochSecond())
                        .build())
                .volume(executeParameter.targetVolume)
                .build();
        return position;
    }

    public double getPnL(PositionCommon.PriceSnapshot currentPriceSnapshot) {
        switch (positionType) {
            case SHORT:
                return (entryPriceSnapshot.price - currentPriceSnapshot.price) / entryPriceSnapshot.price;
            case LONG:
                return (currentPriceSnapshot.price - entryPriceSnapshot.price) / entryPriceSnapshot.price;
        }
        return 0.0;
    }

    public double getGainLossFiat(PositionCommon.PriceSnapshot currentPriceSnapshot) {
        return getPnL(currentPriceSnapshot) * volume;
    }

    public static class ExitPlan {
    }

    ExitPlan exitPlan;
}
