package com.marketsignal.timeseries;

import com.google.common.base.MoreObjects;

public class Bar {
    public String market;
    public String symbol;
    public OHLC ohlc;
    public double volume;

    public Bar(String market, String symbol, OHLC ohlc, double volume) {
        this.market = market;
        this.symbol = symbol;
        this.ohlc = ohlc;
        this.volume = volume;
    }

    public Bar(Bar bar) {
        this(bar.market, bar.symbol, new OHLC(bar.ohlc), bar.volume);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Bar.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("ohlc", ohlc)
                .add("volume", volume)
                .toString();
    }

    public void aggregate(Bar bar) {
        this.ohlc.aggregate(bar.ohlc);
        this.volume += bar.volume;
    }
}
