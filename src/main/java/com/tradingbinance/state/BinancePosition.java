package com.tradingbinance.state;

import com.trading.state.Position;

public class BinancePosition {
    static public BinancePosition execute() {
        BinancePosition binancePosition = new BinancePosition();
        //binancePosition.position = Position.dryRun(executeParameter);
        return binancePosition;
    }

    Position position;
}
