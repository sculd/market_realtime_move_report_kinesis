package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Time;

import java.util.Arrays;

public class MainStock {
    public static void main(String[] args)
    {
        BigQueryImport bqImport = new BigQueryImport();
        int year = 2022;
        int month = 1;
        int day = 24;
        System.out.println("ingesting");
        BigQueryImport.ImportParam param = BigQueryImport.ImportParam.builder()
                .baseDirPath("marketdata/")
                .table(QueryTemplates.Table.POLYGON_BAR_WITH_TIME)
                .symbols(Arrays.asList())
                .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 9, 30))
                .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(year, month, day, 16, 00))
                .build();
        bqImport.importAsCSV(param);
        System.out.println("done");
    }
}
