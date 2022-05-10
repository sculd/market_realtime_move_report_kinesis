package com.tradingbinancechangesanomalyreversal.stream;

import com.trading.state.States;
import com.marketsignalbinance.orderbook.OrderbookFactoryBinance;
import com.tradingchangesanomaly.stream.ChangesAnomalyReversalTradingStream;
import com.tradingbinance.state.BinanceStates;

import com.marketsignal.stream.BarWithTimeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangesAnomalyReversalTradingBinanceStream extends ChangesAnomalyReversalTradingStream {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyReversalTradingBinanceStream.class);

    public ChangesAnomalyReversalTradingBinanceStream(BarWithTimeStream barWithTimeStream) {
        super(barWithTimeStream, new OrderbookFactoryBinance());
    }

    protected States createNewStates(String market, String symbol, States.StatesInitParameter statesInitParameter) {
        return new BinanceStates(market, symbol, statesInitParameter);
    }
}
