package com.marketsignal.stream;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.google.common.util.concurrent.Monitor;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;

public class BarWithTimeStream {
    Duration windowSize;
    BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution;
    public Map<String, BarWithTimeSlidingWindow> keyedBarWithTimeSlidingWindows = new HashMap<>();
    Monitor mutex = new Monitor();

    public BarWithTimeStream(Duration windowSize, BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution) {
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
    }

    public static String bwtToKeyString(BarWithTime bwt) {
        return String.format("%s.%s", bwt.bar.market, bwt.bar.symbol);
    }

    public void onBarWithTime(BarWithTime bwt) {
        String key = bwtToKeyString(bwt);
        mutex.enter();
        try {
            if (!keyedBarWithTimeSlidingWindows.containsKey(key)) {
                keyedBarWithTimeSlidingWindows.put(key, new BarWithTimeSlidingWindow(bwt.bar.market, bwt.bar.symbol, this.windowSize, this.timeSeriesResolution));
            }
            keyedBarWithTimeSlidingWindows.get(key).addBarWithTime(bwt);
        } finally {
            mutex.leave();
        }
    }
}
