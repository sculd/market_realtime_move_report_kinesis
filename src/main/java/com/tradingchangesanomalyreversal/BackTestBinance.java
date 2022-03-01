package com.tradingchangesanomalyreversal;

import com.trading.performance.*;
import com.tradingchangesanomaly.BackTest;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.RangeRunParameter;
import com.marketsignal.App;
import com.marketsignal.AppOption;
import com.marketsignal.OptionParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class BackTestBinance extends BackTest {
    private static final Logger log = LoggerFactory.getLogger(BackTestBinance.class);

    public static void main(String... args) {
        final CommandLineParser parser = new OptionParser(true);
        Options options = AppOption.create();
        try {
            CommandLine commandLine = parser.parse(options, args);

            String envVarFile = commandLine.getOptionValue(AppOption.KEY_ENV_FILE);
            if (envVarFile == null || envVarFile.isEmpty()) {
                log.warn("the option envfile is null (or empty string)");
            } else {
                try (Stream<String> lines = Files.lines(Paths.get(envVarFile), Charset.defaultCharset())) {
                    lines.forEachOrdered(line -> App.setEnv(line.split("=")[0], line.split("=")[1]));
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }

            new BackTestBinance().doRunRange();
        } catch (ParseException ex) {
            log.error(ex.getMessage());
        }
    }

    private void doRunRange() {
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2021)
                .monthBegin(10)
                .dayBegin(1)
                .yearEnd(2021)
                .monthEnd(10)
                .dayEnd(31)
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
