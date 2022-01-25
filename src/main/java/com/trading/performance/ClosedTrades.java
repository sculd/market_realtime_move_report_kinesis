package com.trading.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClosedTrades {
    private static final Logger log = LoggerFactory.getLogger(ClosedTrades.class);
    List<ClosedTrade> closedTrades = new ArrayList<>();

    public void addClosedTrades(ClosedTrade closedTrade) {
        closedTrades.add(closedTrade);
    }

    public double getPnL() {
        return closedTrades.stream().map(ct -> ct.getPnL()).mapToDouble(f -> f).sum();
    }

    public double getGainLossFiat() {
        return closedTrades.stream().map(ct -> ct.getGainLossFiat()).mapToDouble(f -> f).sum();
    }

    public void print() {
        log.info(String.format("PnL: %f", getPnL()));
        log.info(String.format("GainLossFiat: %f", getGainLossFiat()));
        for (ClosedTrade ct : closedTrades) {
            ct.print();
        }
    }
}
