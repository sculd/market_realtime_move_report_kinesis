package com.tradingchangesanomaly.recordprocessor;

import com.marketsignal.marginasset.MarginAsset;
import com.marketsignal.orderbook.OrderbookFactoryTrivial;
import com.marketsignal.timeseries.BarWithTime;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;
import com.tradingchangesanomaly.stream.ChangesAnomalyReversalTradingStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarWithTimestampAnomalyReversalCSVProcessor extends BarWithTimestampCSVProcessor {
    private static final Logger log = LoggerFactory.getLogger(BarWithTimestampAnomalyReversalCSVProcessor.class);

    public BarWithTimestampAnomalyReversalCSVProcessor(MarginAsset marginAsset) {
        changesAnomalyTradingStream = new ChangesAnomalyReversalTradingStream(barWithTimeStream, new OrderbookFactoryTrivial(), marginAsset);
    }

    public ChangesAnomalyReversalTradingStream changesAnomalyTradingStream;

    public void run(String csvFileName, ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter) {
        changesAnomalyTradingStream.init(changesAnomalyTradingStreamInitParameter);
        super.run(csvFileName);
    }

    protected void onFinish() {
        changesAnomalyTradingStream.closedTrades.print();
        super.onFinish();
    }

    protected void processCsvLine(String[] csvLine) {
        if (csvLine[1].equals("symbol")) {
            // skip the header row.
            return;
        }
        BarWithTime bwt = csvLineToBarWithTime(csvLine);
        barWithTimeStream.onBarWithTime(bwt);
        changesAnomalyTradingStream.onBarWithTime(bwt);
    }
}
