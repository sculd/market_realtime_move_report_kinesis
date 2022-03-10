package com.trading.performance;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamUtil;
import lombok.Builder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


@Builder
public class ParameterRun {
    public ChangesAnomalyTradingStreamUtil.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;
    public ClosedTrades closedTrades;

    void exportToCsv(String exportFileName) {
        FileWriter exportFileWriter = null;
        try {
            exportFileWriter = new FileWriter(exportFileName);
            exportFileWriter.write(String.format("%s\n", closedTrades.toCsvHeader()));
            for (ClosedTrade closedTrade : closedTrades.closedTrades) {
                exportFileWriter.write(String.format("%s\n", closedTrade.toCsvLine()));
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            if (exportFileWriter != null) {
                exportFileWriter.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static public ParameterRun fromCsvFile(String csvFileName) {
        ParameterRun parameterRun = ParameterRun.builder().build();
        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] csvLine;
            while ((csvLine = reader.readNext()) != null) {
                ClosedTrade closedTrade = ClosedTrade.fromCsvLine(String.join(",", csvLine));
                if (closedTrade == null) {
                    continue;
                }
                parameterRun.closedTrades.addClosedTrades(closedTrade);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        return parameterRun;
    }
}
