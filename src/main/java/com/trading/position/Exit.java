package com.trading.position;

import com.trading.state.Common;
import lombok.Builder;

@Builder
public class Exit {
    public Common.ExitToggleType exitToggleType;
    public String market;
    public String symbol;

    public Common.PositionType positionType;
    public PositionCommon.PriceSnapshot exitPriceSnapshot;

    @Builder
    public static class ExecuteParameter {
        public String market;
        public String symbol;
        public Common.PositionType positionType;
        public double targetPrice;
    }

    static public Exit dryRun(ExecuteParameter executeParameter) {
        Exit exit = Exit.builder()
                .market(executeParameter.market)
                .symbol(executeParameter.symbol)
                .positionType(executeParameter.positionType)
                .exitPriceSnapshot(PositionCommon.PriceSnapshot.builder()
                        .price(executeParameter.targetPrice)
                        .epochSeconds(java.time.Instant.now().getEpochSecond())
                        .build())
                .build();
        return exit;
    }
}
