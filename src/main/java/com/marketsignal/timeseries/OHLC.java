package com.marketsignal.timeseries;

import java.lang.Math;

public class OHLC {
    public double open;
    public double high;
    public double low;
    public double close;

    public OHLC(double open, double high, double low, double close) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public OHLC(OHLC olhc) {
        this(olhc.open, olhc.high, olhc.low, olhc.close);
    }

    public void aggregate(OHLC ohlc) {
        this.high = Math.max(this.high, ohlc.high);
        this.low = Math.min(this.low, ohlc.low);
        this.close = ohlc.close;
    }
}
