package com.marketsignal.stream;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;

public class Stream {
    Duration windowSize;
    BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution;
    Map<String, BarWithTimeSlidingWindow> KeyedBarWithTimeSlidingWindows = new HashMap<>();

    public Stream(Duration windowSize, BarWithTimeSlidingWindow.TimeSeriesResolution timeSeriesResolution) {
        this.windowSize = windowSize;
        this.timeSeriesResolution = timeSeriesResolution;
    }

    static String BwtToKeyString(BarWithTime bwt) {
        return String.format("%s.%s", bwt.bar.market, bwt.bar.symbol);
    }

    public void OnBarWithTime(BarWithTime bwt) {
        String key = BwtToKeyString(bwt);

        if (!KeyedBarWithTimeSlidingWindows.containsKey(key)) {
            KeyedBarWithTimeSlidingWindows.put(key, new BarWithTimeSlidingWindow(bwt.bar.market, bwt.bar.symbol, this.windowSize, this.timeSeriesResolution));
        }
        KeyedBarWithTimeSlidingWindows.get(key).AddBarWithTime(bwt);
    }
}
