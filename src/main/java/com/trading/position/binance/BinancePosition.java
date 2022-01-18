package com.trading.position.binance;

import com.trading.position.Position;

public class BinancePosition {
    static public BinancePosition execute(Position.ExecuteParameter executeParameter) {
        BinancePosition binancePosition = new BinancePosition();
        binancePosition.position = Position.dryRun(executeParameter);
        return binancePosition;
    }

    Position position;
}
