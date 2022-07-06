package com.tradingbinance.state;

import com.trading.state.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinanceStates extends States{
    private static final Logger logger = LoggerFactory.getLogger(BinanceStates.class);
    public BinanceStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        super(market, symbol, statesInitParameter);

        if (!statesInitParameter.isDryRun) {
            logger.info("[BinanceStates] dry run is false: {} thus creating live objects", statesInitParameter);
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
        } else {
            logger.info("[BinanceStates] dry run is true: {} thus not creating live objects", statesInitParameter);
        }
    }
}
