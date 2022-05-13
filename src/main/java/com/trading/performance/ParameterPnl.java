package com.trading.performance;

import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;
import lombok.Builder;

import java.util.Arrays;


@Builder
public class ParameterPnl {
    public ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;
    public ClosedTradesPnl closedTradesPnl;

    public ParameterPnl ofClosedTrades(ClosedTrades closedTrades) {
        ParameterPnl ranged = ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(changesAnomalyTradingStreamInitParameter)
                .closedTradesPnl(closedTradesPnl.ofClosedTrades(closedTrades))
                .build();
        return ranged;
    }

    static public String toCsvHeader() {
        return String.format("%s,%s",
                ChangesAnomalyTradingStreamInitParameter.toCsvHeader(),
                ClosedTradesPnl.toCsvHeader());
    }

    public String toCsvLine() {
        return String.format("%s,%s\n",
                changesAnomalyTradingStreamInitParameter.toCsvLine(),
                closedTradesPnl.toCsvLine());
    }

    static public ParameterPnl fromCsvLine(String csvLine) {
        String[] columns = csvLine.split(",");
        int l1 = ChangesAnomalyTradingStreamInitParameter.toCsvHeader().split(",").length;
        int l2 = l1 + ClosedTradesPnl.toCsvHeader().split(",").length;
        String[] changesAnomalyTradingStreamInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
        String[] closedTradesPnlParameter = Arrays.copyOfRange(columns, l1, l2);

        return ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(ChangesAnomalyTradingStreamInitParameter.fromCsvLine(String.join(",", changesAnomalyTradingStreamInitParameterColumns)))
                .closedTradesPnl(ClosedTradesPnl.fromCsvLine(String.join(",", closedTradesPnlParameter)))
                .build();
    }
}
