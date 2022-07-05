package com.trading.performance;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Analyses;
import com.marketsignal.util.Time;
import com.trading.state.Common;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Builder
public class ClosedTrade {
    private static final Logger log = LoggerFactory.getLogger(ClosedTrade.class);
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public double entryTargetPrice;
    public Common.PriceSnapshot entryPriceSnapshot;
    public double volume;
    public double exitTargetPrice;
    public Common.PriceSnapshot exitPriceSnapshot;
    public Analyses analysesUponEnter;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ClosedTrade.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("positionSideType", positionSideType)
                .add("PnL", String.format("%.3f", getPnL()))
                .add("GainLossFiat", getGainLossFiat())
                .add("entryTargetPrice", entryTargetPrice)
                .add("entryPriceSnapshot", entryPriceSnapshot.toString())
                .add("exitTargetPrice", exitTargetPrice)
                .add("exitPriceSnapshot", exitPriceSnapshot.toString())
                .toString();
    }

    public double getPnL() {
        switch (positionSideType) {
            case SHORT:
                return (entryPriceSnapshot.price - exitPriceSnapshot.price) / entryPriceSnapshot.price;
            case LONG:
                return (exitPriceSnapshot.price - entryPriceSnapshot.price) / entryPriceSnapshot.price;
        }
        return 0.0;
    }

    public double getGainLossFiat() {
        return getPnL() * volume * entryPriceSnapshot.price;
    }

    public void print() {
        log.info(String.format("%s", toString()));
    }

    static public String toCsvHeaderWithoutAnalysis() {
        List<String> headers = new ArrayList<>();
        headers.add("market");
        headers.add("symbol");
        headers.add("positionSideType");
        headers.add("entryPriceSnapshot.price");
        headers.add("entryPriceSnapshot.epochSeconds");
        headers.add("volume");
        headers.add("exitPriceSnapshot.price");
        headers.add("exitPriceSnapshot.epochSeconds");
        headers.add("pnl");
        return String.join(",", headers);
    }

    public String toCsvHeader() {
        List<String> headers = new ArrayList<>();
        headers.add(toCsvHeaderWithoutAnalysis());
        String analysisHeader = analysesUponEnter.toCsvHeader();
        if (!analysisHeader.isEmpty()) {
            headers.add(analysisHeader);
        }
        return String.join(",", headers);
    }

    public String toCsvLine() {
        List<String> columns = new ArrayList<>();
        columns.add(String.format("%s", market));
        columns.add(String.format("%s", symbol));
        columns.add(String.format("%s", positionSideType));
        columns.add(String.format("%f", entryPriceSnapshot.price));
        columns.add(String.format("%s", Time.fromEpochSecondsToDateTimeStr(entryPriceSnapshot.epochSeconds)));
        columns.add(String.format("%f", volume));
        columns.add(String.format("%f", exitPriceSnapshot.price));
        columns.add(String.format("%s", Time.fromEpochSecondsToDateTimeStr(exitPriceSnapshot.epochSeconds)));
        columns.add(String.format("%f", getPnL()));
        String analysisLine = analysesUponEnter.toCsvLine();
        if (!analysisLine.isEmpty()) {
            columns.add(analysisLine);
        }
        return String.join(",", columns);
    }

    static public ClosedTrade fromCsvLine(String csvLine) {
        String[] columns = csvLine.split(",");
        return ClosedTrade.builder()
                .market(columns[0])
                .symbol(columns[1])
                .positionSideType(Common.PositionSideType.of(columns[2]))
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(Double.parseDouble(columns[3]))
                        .epochSeconds(Long.parseLong(columns[4]))
                        .build())
                .volume(Double.parseDouble(columns[5]))
                .exitPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(Double.parseDouble(columns[6]))
                        .epochSeconds(Long.parseLong(columns[7]))
                        .build())
                .build();
    }
}
