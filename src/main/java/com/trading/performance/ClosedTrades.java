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
    List<ClosedTrade> closedTrades = new ArrayList<>();

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

    static public String toCsvHeader() {
        List<String> headers = new ArrayList<>();
        headers.add("closed_trades");
        headers.add("pnl");
        headers.add("pnl_per_trade");
        headers.add("closed_trades_long");
        headers.add("pnl_long");
        headers.add("pnl_per_trade_long");
        headers.add("closed_trades_short");
        headers.add("pnl_short");
        headers.add("pnl_per_trade_short");
        return String.join(",", headers);
    }

    public String toCsvLine() {
        List<String> columns = new ArrayList<>();
        columns.add(String.format("%d", closedTrades.size()));
        columns.add(String.format("%f", getPnL(closedTrades)));
        columns.add(String.format("%f", getPnL(closedTrades) / closedTrades.size()));
        List<ClosedTrade> closedLongTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.LONG)
                .collect(Collectors.toList());
        columns.add(String.format("%d", closedLongTrades.size()));
        columns.add(String.format("%f", getPnL(closedLongTrades)));
        columns.add(String.format("%f", getPnL(closedLongTrades) / closedTrades.size()));
        List<ClosedTrade> closedShortTrades = closedTrades.stream().filter(ct -> ct.positionSideType == Common.PositionSideType.SHORT)
                .collect(Collectors.toList());
        columns.add(String.format("%d", closedShortTrades.size()));
        columns.add(String.format("%f", getPnL(closedShortTrades)));
        columns.add(String.format("%f", getPnL(closedShortTrades) / closedShortTrades.size()));
        return String.join(",", columns);
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
