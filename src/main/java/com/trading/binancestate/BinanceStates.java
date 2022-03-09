package com.trading.binancestate;

import com.trading.state.EnterInProgress;
import com.trading.state.States;

public class BinanceStates extends States{
    public BinanceStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        super(market, symbol, statesInitParameter);

        enterInProgress = EnterInProgress.builder()
                .market(market).symbol(symbol)
                .exitPlanInitParameter(statesInitParameter.exitPlanInitParameter)
                .orderInProgressPlanInitParameter(statesInitParameter.enterInProgressInitParameter).build();
        exit = new BinanceExit();
    }
}
