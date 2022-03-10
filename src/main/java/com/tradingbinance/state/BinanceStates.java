package com.tradingbinance.state;

import com.trading.state.States;

public class BinanceStates extends States{
    public BinanceStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        super(market, symbol, statesInitParameter);

        enter = BinanceEnter.builder().market(market).symbol(symbol).build();
        enterInProgress = BinanceEnterInProgress.builder()
                .market(market).symbol(symbol)
                .exitPlanInitParameter(statesInitParameter.exitPlanInitParameter)
                .orderInProgressPlanInitParameter(statesInitParameter.enterInProgressInitParameter)
                .build();
        exit = BinanceExit.builder().build();
        exitInProgress = BinanceExitInProgress.builder()
                .market(market).symbol(symbol)
                .build();
    }
}
