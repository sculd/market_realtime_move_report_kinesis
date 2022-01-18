package com.trading.performance;

import java.util.ArrayList;
import java.util.List;

public class ClosedTrades {
    List<ClosedTrade> closedTrades = new ArrayList<>();

    public void addClosedTrades(ClosedTrade closedTrade) {
        closedTrades.add(closedTrade);
    }

    public double getPnL() {
        return 0.0;
    }

    public double getGainLossFiat() {
        return 0.0;
    }
}
