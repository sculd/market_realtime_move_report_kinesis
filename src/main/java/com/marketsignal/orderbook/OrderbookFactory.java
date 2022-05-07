package com.marketsignal.orderbook;

public interface OrderbookFactory {
    public Orderbook create(double currentPrice);
}
