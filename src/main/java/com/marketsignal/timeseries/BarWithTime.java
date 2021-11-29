package com.marketsignal.timeseries;

public class BarWithTime {
    public Bar bar;
    public long epochSeconds;

    public BarWithTime(Bar bar, long epochSeconds) {
        this.bar = new Bar(bar);
        this.epochSeconds = epochSeconds;
    }
}
