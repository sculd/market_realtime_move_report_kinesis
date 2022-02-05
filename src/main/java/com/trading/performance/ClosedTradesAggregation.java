package com.trading.performance;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class ClosedTradesAggregation {
    int closedTrades;
    double pnl;
    double pnlPerTrade;
    int closedTradesLong;
    double pnlLong;
    double pnlPerTradeLong;
    int closedTradesShort;
    double pnlShort;
    double pnlPerTradeShort;

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
}
