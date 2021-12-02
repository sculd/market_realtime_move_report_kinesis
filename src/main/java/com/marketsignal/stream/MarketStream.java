package com.marketsignal.stream;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;

public class MarketStream {
    Duration windowSize;
    BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution;
    Map<String, BarWithTimeSlidingWindow> KeyedBarWithTimeSlidingWindows = new HashMap<>();

    public MarketStream(Duration windowSize, BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution) {
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
    }

    static String bwtToKeyString(BarWithTime bwt) {
        return String.format("%s.%s", bwt.bar.market, bwt.bar.symbol);
    }

    public void onBarWithTime(BarWithTime bwt) {
        String key = bwtToKeyString(bwt);
        if (!KeyedBarWithTimeSlidingWindows.containsKey(key)) {
            KeyedBarWithTimeSlidingWindows.put(key, new BarWithTimeSlidingWindow(bwt.bar.market, bwt.bar.symbol, this.windowSize, this.timeSeriesResolution));
        }
        KeyedBarWithTimeSlidingWindows.get(key).addBarWithTime(bwt);
    }
}
