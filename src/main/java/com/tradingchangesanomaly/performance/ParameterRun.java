package com.tradingchangesanomaly.performance;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.trading.performance.ClosedTrades;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamCommon;
import lombok.Builder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


@Builder
public class ParameterRun {
    public ChangesAnomalyTradingStreamCommon.ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;
    public ClosedTrades closedTrades;

    public String toAggregationCsvLine() {
        return String.format("%s,%s\n",
                changesAnomalyTradingStreamInitParameter.toCsvLine(),
                closedTrades.getClosedTradesAggregation().toCsvLine());
    }

    static public ParameterRun fromAggregateCsvFile(String csvFileName) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] csvLine;
            while ((csvLine = reader.readNext()) != null) {
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
