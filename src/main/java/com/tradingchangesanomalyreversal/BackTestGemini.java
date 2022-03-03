package com.tradingchangesanomalyreversal;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.RangeRunParameter;
import com.trading.performance.ParameterPnls;
import com.tradingchangesanomaly.BackTest;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackTestGemini extends BackTest {
    private static final Logger log = LoggerFactory.getLogger(BackTestGemini.class);

    public static void main(String... args) {
        new BackTestGemini().selectAndRunRange();
    }

    private void selectAndRunRange() {
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2022)
                .monthBegin(3)
                .dayBegin(1)
                .yearEnd(2022)
                .monthEnd(3)
                .dayEnd(3)
                .build();

        runRange(rangeRunParameter);
    }

    private void runRange(RangeRunParameter rangeRunParameter) {
        BigQueryImport.ImportParam importParam = rangeRunParameter.getImportParam(QueryTemplates.Table.GEMINI_BAR_WITH_TIME);
        List<ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter> scanGrids = generateScanGrids();

        String runsExportDir = String.format("backtestdata/gemini/runs/reversal/backtest_runs_%s", rangeRunParameter.toFileNamePhrase());
        String pnlsExportFileName = String.format("backtestdata/gemini/pnls/reversal/backtest_%s.csv", rangeRunParameter.toFileNamePhrase());
        ParameterPnls.createNew(pnlsExportFileName);

        for (ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter : scanGrids) {
            runForParam(importParam, runsExportDir, pnlsExportFileName, changesAnomalyTradingStreamInitParameter);
        }
    }
}
