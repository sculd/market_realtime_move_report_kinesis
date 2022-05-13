package com.trading.performance;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class ClosedTradesPnl {
    int closedTrades;
    double pnl;
    double pnlPerTrade;
    int closedTradesLong;
    double pnlLong;
    double pnlPerTradeLong;
    int closedTradesShort;
    double pnlShort;
    double pnlPerTradeShort;

    public ClosedTradesPnl ofClosedTrades(ClosedTrades of) {
        ClosedTradesPnl ranged = ClosedTradesPnl.builder()
                .closedTrades(of.closedTrades.size())
                .pnl(of.getPnL())
                .pnlPerTrade(of.getPnLPerTrade())
                .closedTradesLong(of.longOnly().closedTrades.size())
                .pnlLong(of.longOnly().getPnL())
                .pnlPerTradeLong(of.longOnly().getPnLPerTrade())
                .closedTradesShort(of.shortOnly().closedTrades.size())
                .pnlShort(of.shortOnly().getPnL())
                .pnlPerTradeShort(of.shortOnly().getPnLPerTrade())
                .build();
        return ranged;
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
        columns.add(String.format("%d", closedTrades));
        columns.add(String.format("%f", pnl));
        columns.add(String.format("%f", pnlPerTrade));
        columns.add(String.format("%d", closedTradesLong));
        columns.add(String.format("%f", pnlLong));
        columns.add(String.format("%f", pnlPerTradeLong));
        columns.add(String.format("%d", closedTradesShort));
        columns.add(String.format("%f", pnlShort));
        columns.add(String.format("%f", pnlPerTradeShort));
        return String.join(",", columns);
    }

    static public ClosedTradesPnl fromCsvLine(String csvLine) {
        String[] columns = csvLine.split(",");
        return ClosedTradesPnl.builder()
                .closedTrades(Integer.parseInt(columns[0]))
                .pnl(Double.parseDouble(columns[1]))
                .pnlPerTrade(Double.parseDouble(columns[2]))
                .closedTradesLong(Integer.parseInt(columns[3]))
                .pnlLong(Double.parseDouble(columns[4]))
                .pnlPerTradeLong(Double.parseDouble(columns[5]))
                .closedTradesShort(Integer.parseInt(columns[6]))
                .pnlShort(Double.parseDouble(columns[7]))
                .pnlPerTradeShort(Double.parseDouble(columns[8]))
                .build();
    }
}
