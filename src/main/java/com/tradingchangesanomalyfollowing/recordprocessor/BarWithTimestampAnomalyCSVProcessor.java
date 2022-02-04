package com.tradingchangesanomalyfollowing.recordprocessor;

import com.marketsignal.timeseries.BarWithTime;
import com.tradingchangesanomalyfollowing.stream.ChangesAnomalyFollowingTradingStream;
import com.tradingchangesanomaly.recordprocessor.BarWithTimestampCSVProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarWithTimestampAnomalyCSVProcessor extends BarWithTimestampCSVProcessor {
    private static final Logger log = LoggerFactory.getLogger(BarWithTimestampAnomalyCSVProcessor.class);

    public ChangesAnomalyFollowingTradingStream changesAnomalyFollowingTradingStream = new ChangesAnomalyFollowingTradingStream(barWithTimeStream);

    public void run(String csvFileName, ChangesAnomalyFollowingTradingStream.ChangesAnomalyFollowingTradingStreamInitParameter ChangesAnomalyFollowingTradingStreamInitParameter) {
        changesAnomalyFollowingTradingStream.init(ChangesAnomalyFollowingTradingStreamInitParameter);
        super.run(csvFileName);
    }

    protected void onFinish() {
        changesAnomalyFollowingTradingStream.closedTrades.print();
        super.onFinish();
    }

    protected void processCsvLine(String[] csvLine) {
        if (csvLine[1].equals("symbol")) {
            // skip the header row.
            return;
        }
        BarWithTime bwt = csvLineToBarWithTime(csvLine);
        barWithTimeStream.onBarWithTime(bwt);
        changesAnomalyFollowingTradingStream.onBarWithTime(bwt);
    }
}
