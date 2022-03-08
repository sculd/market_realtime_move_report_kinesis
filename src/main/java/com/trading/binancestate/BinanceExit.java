package com.trading.binancestate;

import com.trading.state.Exit;
import com.trading.state.Common;
import com.trading.state.Position;
import lombok.Builder;

public class BinanceExit extends Exit {
    public ExecuteResult execute() {
        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return com.trading.state.Exit.ExecuteResult.builder().result(com.trading.state.Exit.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
