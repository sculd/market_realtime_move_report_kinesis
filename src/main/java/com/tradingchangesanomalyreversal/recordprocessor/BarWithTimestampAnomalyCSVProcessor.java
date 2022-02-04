package com.tradingchangesanomalyreversal.recordprocessor;

import com.marketsignal.timeseries.BarWithTime;
import com.tradingchangesanomalyreversal.stream.ChangesAnomalyReversalTradingStream;
import com.tradingchangesanomaly.recordprocessor.BarWithTimestampCSVProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarWithTimestampAnomalyCSVProcessor extends BarWithTimestampCSVProcessor {
    private static final Logger log = LoggerFactory.getLogger(BarWithTimestampAnomalyCSVProcessor.class);

    public ChangesAnomalyReversalTradingStream changesAnomalyReversalTradingStream = new ChangesAnomalyReversalTradingStream(barWithTimeStream);

    public void run(String csvFileName, ChangesAnomalyReversalTradingStream.ChangesAnomalyReversalTradingStreamInitParameter changesAnomalyReversalTradingStreamInitParameter) {
        changesAnomalyReversalTradingStream.init(changesAnomalyReversalTradingStreamInitParameter);
        super.run(csvFileName);
    }

    protected void onFinish() {
        changesAnomalyReversalTradingStream.closedTrades.print();
        super.onFinish();
    }

    protected void processCsvLine(String[] csvLine) {
        if (csvLine[1].equals("symbol")) {
            // skip the header row.
            return;
        }
        BarWithTime bwt = csvLineToBarWithTime(csvLine);
        barWithTimeStream.onBarWithTime(bwt);
        changesAnomalyReversalTradingStream.onBarWithTime(bwt);
    }
}