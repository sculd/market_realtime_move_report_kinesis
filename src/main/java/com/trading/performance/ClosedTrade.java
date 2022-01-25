package com.trading.performance;

import com.google.common.base.MoreObjects;
import com.trading.state.Common;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
public class ClosedTrade {
    private static final Logger log = LoggerFactory.getLogger(ClosedTrade.class);
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public Common.PriceSnapshot entryPriceSnapshot;
    public double volume;
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
        return getPnL() * volume;
    }

    public void print() {
        log.info(String.format("%s", toString()));
    }
}
