package com.trading.recordprocessor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.Bar;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.OHLC;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class BarWithTimestampCSVProcessor {
    BarWithTimeStream barWithTimeStream = new BarWithTimeStream(Duration.ofHours(6), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);

    public void run(String csvFileName) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] csvLine;
            while ((csvLine = reader.readNext()) != null) {
                processCsvLine(csvLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    void processCsvLine(String[] csvLine) {
        if (csvLine[1].equals("symbol")) {
            // header
            return;
        }
        BarWithTime bwt = new BarWithTime(
                new Bar(/*market=*/"binance",
                        /*symbol=*/csvLine[1],
                        /*ohlc=*/new OHLC(Double.valueOf(csvLine[2]), Double.valueOf(csvLine[3]), Double.valueOf(csvLine[4]), Double.valueOf(csvLine[5])),
                        /*volume=*/Double.valueOf(csvLine[6])),
                /*epochSeconds=*/Long.valueOf(csvLine[0])
                );
        System.out.println(String.format("%s,%s,...: %s", csvLine[0], csvLine[1], bwt.toString()));
        barWithTimeStream.onBarWithTime(bwt);
    }
}
