package com.trading.binancestate;

import com.trading.state.States;

public class BinanceStates extends States{
    public BinanceStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        super(market, symbol, statesInitParameter);

        enter = BinanceEnter.builder().market(market).symbol(symbol).exitPlanInitParameter(statesInitParameter.exitPlanInitParameter).build();
        exit = new BinanceExit();
    }
}
