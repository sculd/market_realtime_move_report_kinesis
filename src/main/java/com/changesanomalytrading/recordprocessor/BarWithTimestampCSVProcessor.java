package com.changesanomalytrading.recordprocessor;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import com.trading.recordprocessor.CSVProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BarWithTimestampCSVProcessor extends CSVProcessor {
    private static final Logger log = LoggerFactory.getLogger(BarWithTimestampCSVProcessor.class);

    protected BarWithTimeStream barWithTimeStream = new BarWithTimeStream(Duration.ofHours(6), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);

    public BarWithTime csvLineToBarWithTime(String[] csvLine) {
        BarWithTime bwt = new BarWithTime(
                new Bar(/*market=*/"binance",
                        /*symbol=*/csvLine[1],
                        /*ohlc=*/new OHLC(Double.valueOf(csvLine[2]), Double.valueOf(csvLine[3]), Double.valueOf(csvLine[4]), Double.valueOf(csvLine[5])),
                        /*volume=*/Double.valueOf(csvLine[6])),
                /*epochSeconds=*/Long.valueOf(csvLine[0])
        );
        return bwt;
    }

    protected boolean ifProcessLine(String[] csvLine, int shardId, int shardSize) {
        if (csvLine[1].equals("symbol")) {
            // skip the header row.
            return false;
        }
        BarWithTime bwt = csvLineToBarWithTime(csvLine);
        int hashCode = String.format("%s.%s", bwt.bar.market, bwt.bar.symbol).hashCode();
        if (hashCode < 0) {
            hashCode *= -1;
        }
        return hashCode % shardSize == shardId;
    }
}
