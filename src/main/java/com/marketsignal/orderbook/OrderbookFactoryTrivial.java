package com.marketsignal.orderbook;

public class OrderbookFactoryTrivial implements OrderbookFactory {
    public Orderbook create(String market, String symbol, double currentPrice) {
        return new Orderbook(market, symbol, java.time.Instant.now().getEpochSecond());
    }
}
