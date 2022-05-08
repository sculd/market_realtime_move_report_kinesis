package com.marketsignal.orderbook;

public class OrderbookFactoryTrivial implements OrderbookFactory {
    public Orderbook create(String market, String symbol, double currentPrice) {
        Orderbook orderbook =  new Orderbook(market, symbol, java.time.Instant.now().getEpochSecond());

        orderbook.asks.quotes.add(
                Orderbook.Quote.builder()
                        .price(Double.valueOf(currentPrice))
                        .volume(Double.valueOf(0))
                        .build());
        orderbook.bids.quotes.add(
                Orderbook.Quote.builder()
                        .price(Double.valueOf(currentPrice))
                        .volume(Double.valueOf(0))
                        .build());

        return orderbook;
    }
}
