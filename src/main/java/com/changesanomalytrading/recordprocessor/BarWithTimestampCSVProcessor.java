package com.changesanomalytrading.recordprocessor;

import java.time.Duration;

import com.trading.recordprocessor.CSVProcessor;
import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import com.changesanomalytrading.state.stream.ChangesAnomalyTradingStream;

public class BarWithTimestampCSVProcessor extends CSVProcessor {
    BarWithTimeStream barWithTimeStream = new BarWithTimeStream(Duration.ofHours(6), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);
    ChangesAnomalyTradingStream changesAnomalyTradingStream = new ChangesAnomalyTradingStream(barWithTimeStream);

    void processCsvLine(String[] csvLine) {
        if (csvLine[1].equals("symbol")) {
            // header
            return;
        }
        BarWithTime bwt = new BarWithTime(
                new Bar(/*market=*/"binance",
                        /*symbol=*/csvLine[1],
                        /*ohlc=*/new OHLC(Double.valueOf(csvLine[2]), Double.valueOf(csvLine[3]), Double.valueOf(csvLine[4]), Double.valueOf(csvLine[5])),
                        /*volume=*/Double.valueOf(csvLine[6])),
                /*epochSeconds=*/Long.valueOf(csvLine[0])
                );
        barWithTimeStream.onBarWithTime(bwt);
        changesAnomalyTradingStream.onBarWithTime(bwt);
    }
}
