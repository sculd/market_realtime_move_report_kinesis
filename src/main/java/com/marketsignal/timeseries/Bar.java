package com.marketsignal.timeseries;

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

    public void Aggregate(Bar bar) {
        this.ohlc.Aggregate(bar.ohlc);
        this.volume += bar.volume;
    }
}
