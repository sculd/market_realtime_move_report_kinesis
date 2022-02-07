package com.trading.performance;

import com.google.common.base.MoreObjects;
import com.trading.state.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClosedTrades {
    private static final Logger log = LoggerFactory.getLogger(ClosedTrades.class);
    public List<ClosedTrade> closedTrades = new ArrayList<>();

    public void addClosedTrades(ClosedTrade closedTrade) {
        closedTrades.add(closedTrade);
    }

    public double getPnL(List<ClosedTrade> closedTrades) {
        return closedTrades.stream().map(ct -> ct.getPnL()).mapToDouble(f -> f).sum();
    }

    public double getGainLossFiat(List<ClosedTrade> closedTrades) {
        return closedTrades.stream().map(ct -> ct.getGainLossFiat()).mapToDouble(f -> f).sum();
    }

    public void printForList(List<ClosedTrade> closedTrades, boolean printEntries) {
        log.info(String.format("Closed trades: %d", closedTrades.size()));
        double pnl = getPnL(closedTrades);
        log.info(String.format("PnL: %f (per trade: %f)", pnl, pnl / closedTrades.size()));
        log.info(String.format("GainLossFiat: %f", getGainLossFiat(closedTrades)));
        if (printEntries) {
            for (ClosedTrade ct : closedTrades) {
                ct.print();
            }
        }
    }

    public void print() {
        printForList(closedTrades, true);
        List<ClosedTrade> closedLongTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.LONG)
                .collect(Collectors.toList());
        List<ClosedTrade> closedShortTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.SHORT)
                .collect(Collectors.toList());
        log.info("\nLong trades");
        printForList(closedLongTrades, false);
        log.info("\nShort trades");
        printForList(closedShortTrades, false);
    }

    public ClosedTradesPnl getClosedTradesPnl() {
        List<ClosedTrade> closedLongTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.LONG)
                .collect(Collectors.toList());
        List<ClosedTrade> closedShortTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.SHORT)
                .collect(Collectors.toList());
        return ClosedTradesPnl.builder()
                .closedTrades(closedTrades.size())
                .pnl(getPnL(closedTrades))
                .pnlPerTrade(getPnL(closedTrades) / closedTrades.size())
                .closedTradesLong(closedLongTrades.size())
                .pnlLong(getPnL(closedLongTrades))
                .pnlPerTrade(getPnL(closedLongTrades) / closedLongTrades.size())
                .closedTradesShort(closedShortTrades.size())
                .pnlShort(getPnL(closedShortTrades))
                .pnlPerTradeShort(getPnL(closedShortTrades) / closedShortTrades.size())
                .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ClosedTrades.class)
                .add("Closed trades", closedTrades.size())
                .add("PnL", getPnL(closedTrades))
                .add("PnL per trade", getPnL(closedTrades) / closedTrades.size())
                .add("GainLossFiat", getGainLossFiat(closedTrades))
                .toString();
    }
}
