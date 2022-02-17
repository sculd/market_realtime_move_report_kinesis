package com.trading.state;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Analyses;

import lombok.Builder;

@Builder
public class Position {
    public String market;
    public String symbol;

    public Common.PositionSideType positionSideType;
    public Common.PriceSnapshot entryPriceSnapshot;
    public double volume;
    @Builder.Default
    public Analyses analysesUponEnter = new Analyses();

    public double getPnL(Common.PriceSnapshot currentPriceSnapshot) {
        switch (positionSideType) {
            case SHORT:
                return (entryPriceSnapshot.price - currentPriceSnapshot.price) / entryPriceSnapshot.price;
            case LONG:
                return (currentPriceSnapshot.price - entryPriceSnapshot.price) / entryPriceSnapshot.price;
        }
        return 0.0;
    }

    public double getGainLossFiat(Common.PriceSnapshot currentPriceSnapshot) {
        return getPnL(currentPriceSnapshot) * volume;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Position.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("positionSideType", positionSideType)
                .add("entryPriceSnapshot", entryPriceSnapshot)
                .add("volume", volume)
                .toString();
    }
}
