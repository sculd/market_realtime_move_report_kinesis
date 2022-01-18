package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;

import java.util.Arrays;

public class Main {
    public static void main(String[] args)
    {
        BigQueryImport bqImport = new BigQueryImport();
        bqImport.importAsCSV("marketdata/", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList("ETHUSDT", "BTCUSDT"), 1642382284, 1642382384);
        System.out.println("done");
    }
}
