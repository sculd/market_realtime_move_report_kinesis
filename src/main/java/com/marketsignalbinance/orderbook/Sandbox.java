package com.marketsignalbinance.orderbook;

import com.marketsignal.orderbook.Orderbook;

public class Sandbox {

    public static void main(String... args) {
        OrderbookFactoryBinance f = new OrderbookFactoryBinance();
        Orderbook o = f.create("binance", "BTCUSDT", 123);
        System.out.println(o.toString());
    }
}
