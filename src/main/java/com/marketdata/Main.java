package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;

import java.util.Arrays;

public class Main {
    public static void main(String[] args)
    {
        BigQueryImport bqImport = new BigQueryImport();
        int year = 2022;
        int month = 1;
        int day = 23;
        bqImport.importAsCSV("marketdata/", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList(),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 9, 0),
                Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 16, 0));
        System.out.println("done");
    }
}