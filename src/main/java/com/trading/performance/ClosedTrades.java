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

    public ClosedTrades ofRange(long epochSecondsBegin, long epochSecondsEnd) {
        ClosedTrades ranged = new ClosedTrades();
        ranged.closedTrades = closedTrades.stream().filter(ct -> ct.entryPriceSnapshot.epochSeconds >= epochSecondsBegin && ct.entryPriceSnapshot.epochSeconds < epochSecondsEnd).collect(Collectors.toList());
        return ranged;
    }

    public void addClosedTrades(ClosedTrade closedTrade) {
        closedTrades.add(closedTrade);
    }

    public double getPnL(List<ClosedTrade> closedTrades) {
        return closedTrades.stream().map(ct -> ct.getPnL()).mapToDouble(f -> f).sum();
    }

    public double getPnLPerTrade(List<ClosedTrade> closedTrades) {
        return closedTrades.isEmpty()? 0 : getPnL(closedTrades) / closedTrades.size();
    }

    public double getGainLossFiat(List<ClosedTrade> closedTrades) {
        return closedTrades.stream().map(ct -> ct.getGainLossFiat()).mapToDouble(f -> f).sum();
    }

    public void printForList(List<ClosedTrade> closedTrades, boolean printEntries) {
        log.info(String.format("Closed trades: %d", closedTrades.size()));
        double pnl = getPnL(closedTrades);
        log.info(String.format("PnL: %f (per trade: %f)", pnl, getPnLPerTrade(closedTrades)));
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

        double pnl = getPnL(closedTrades);
        double pnlPerTrade = closedTrades.isEmpty()? 0 : pnl / closedTrades.size();
        double pnlLong = getPnL(closedLongTrades);
        double pnlPerTradeLong = closedLongTrades.isEmpty()? 0 : pnlLong / closedLongTrades.size();
        double pnlShort = getPnL(closedShortTrades);
        double pnlPerTradeShort = closedShortTrades.isEmpty()? 0 : pnlShort / closedShortTrades.size();

        return ClosedTradesPnl.builder()
                .closedTrades(closedTrades.size())
                .pnl(pnl)
                .pnlPerTrade(pnlPerTrade)
                .closedTradesLong(closedLongTrades.size())
                .pnlLong(pnlLong)
                .pnlPerTradeLong(pnlPerTradeLong)
                .closedTradesShort(closedShortTrades.size())
                .pnlShort(pnlShort)
                .pnlPerTradeShort(pnlPerTradeShort)
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

    public String toCsvHeader() {
        if (closedTrades.isEmpty()) {
            return ClosedTrade.toCsvHeaderWithoutAnalysis();
        }
        return closedTrades.get(0).toCsvHeader();
    }

}
