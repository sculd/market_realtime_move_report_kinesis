package com.marketsignal.orderbook;

public interface OrderbookFactory {
    public Orderbook create(String market, String symbol, double currentPrice);
}
