package com.tradingchangesanomaly.performance;

import com.trading.performance.ClosedTradesPnl;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import lombok.Builder;

import java.util.Arrays;


@Builder
public class ParameterPnl {
    public ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;
    public ClosedTradesPnl closedTradesPnl;

    static public String toCsvHeader() {
        return String.format("%s,%s\n",
                ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter.toCsvHeader(),
                ClosedTradesPnl.toCsvHeader());
    }

    public String toCsvLine() {
        return String.format("%s,%s\n",
                changesAnomalyTradingStreamInitParameter.toCsvLine(),
                closedTradesPnl.toCsvLine());
    }

    static public ParameterPnl fromCsvLine(String csvLine) {
        String[] columns = csvLine.split(",");
        int l1 = ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter.toCsvHeader().split(",").length;
        int l2 = l1 + ClosedTradesPnl.toCsvHeader().split(",").length;
        String[] changesAnomalyTradingStreamInitParameterColumns = Arrays.copyOfRange(columns, 0, l1);
        String[] closedTradesPnlParameter = Arrays.copyOfRange(columns, l1, l2);

        return ParameterPnl.builder()
                .changesAnomalyTradingStreamInitParameter(ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter.fromCsvLine(String.join(",", changesAnomalyTradingStreamInitParameterColumns)))
                .closedTradesPnl(ClosedTradesPnl.fromCsvLine(String.join(",", closedTradesPnlParameter)))
                .build();
    }
}
