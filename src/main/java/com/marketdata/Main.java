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
        System.out.println("ingesting");
        BigQueryImport.ImportParam param = BigQueryImport.ImportParam.builder()
                .baseDirPath("marketdata/")
                .table(QueryTemplates.Table.BINANCE_BAR_WITH_TIME)
                .symbols(Arrays.asList())
                .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 0, 0))
                .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 23, 59))
                .build();
        bqImport.importAsCSV(param);
        System.out.println("done");
    }
}
