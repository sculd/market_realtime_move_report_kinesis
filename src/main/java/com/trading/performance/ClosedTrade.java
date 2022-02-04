package com.trading.performance;

import com.google.common.base.MoreObjects;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ClosedTrade.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("positionSideType", positionSideType)
                .add("PnL", getPnL())
                .add("GainLossFiat", getGainLossFiat())
                .add("entryPriceSnapshot", entryPriceSnapshot.toString())
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

    static public String toCsvHeader() {
        List<String> headers = new ArrayList<>();
        headers.add("market");
        headers.add("symbol");
        headers.add("positionSideType");
        headers.add("entryPriceSnapshot.price");
        headers.add("entryPriceSnapshot.epochSeconds");
        headers.add("volume");
        headers.add("exitPriceSnapshot.price");
        headers.add("exitPriceSnapshot.epochSeconds");
        return String.join(",", headers);
    }

    public String toCsvLine() {
        List<String> columns = new ArrayList<>();
        columns.add(String.format("%s", market));
        columns.add(String.format("%s", symbol));
        columns.add(String.format("%s", positionSideType));
        columns.add(String.format("%f", entryPriceSnapshot.price));
        columns.add(String.format("%d", entryPriceSnapshot.epochSeconds));
        columns.add(String.format("%f", volume));
        columns.add(String.format("%f", exitPriceSnapshot.price));
        columns.add(String.format("%d", exitPriceSnapshot.epochSeconds));
        return String.join(",", columns);
    }
}
