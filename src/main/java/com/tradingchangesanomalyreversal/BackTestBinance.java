package com.tradingchangesanomalyreversal;

import com.trading.performance.*;
import com.tradingchangesanomaly.BackTestBase;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.RangeRunParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class BackTestBinance extends BackTestBase {
    private static final Logger log = LoggerFactory.getLogger(BackTestBinance.class);

    public static void main(String... args) {
        new BackTestBinance().selectAndRunRange();
    }

    private void selectAndRunRange() {
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2022)
                .monthBegin(2)
                .dayBegin(22)
                .yearEnd(2022)
                .monthEnd(2)
                .dayEnd(23)
                .build();

        runRange(rangeRunParameter);
    }

    private void runRange(RangeRunParameter rangeRunParameter) {
        BigQueryImport.ImportParam importParam = rangeRunParameter.getImportParam(QueryTemplates.Table.BINANCE_BAR_WITH_TIME);
        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> scanGrids = generateScanGrids();

        String runsExportDir = String.format("backtestdata/binance/runs/reversal/backtest_runs_%s", rangeRunParameter.toFileNamePhrase());
        String pnlsExportFileName = String.format("backtestdata/binance/pnls/reversal/backtest_%s.csv", rangeRunParameter.toFileNamePhrase());
        ParameterPnls.createNew(pnlsExportFileName);

        for (ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            runForParam(importParam, runsExportDir, pnlsExportFileName, changesAnomalyTradingStreamInitParameter);
        }
    }
}
