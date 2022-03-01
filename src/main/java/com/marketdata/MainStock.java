package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.RangeRunParameter;

public class MainStock {
    static void importSmallRange(RangeRunParameter rangeRunParameter)
    {
        BigQueryImport bqImport = new BigQueryImport();
        System.out.println(String.format("[importSmallRange] %s", rangeRunParameter.toFileNamePhrase()));
        BigQueryImport.ImportParam param = rangeRunParameter.getImportParam(QueryTemplates.Table.POLYGON_BAR_WITH_TIME);
        bqImport.importAsCSV(param);
    }

    public static void main(String[] args)
    {
        System.out.println("ingesting");
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2022)
                .monthBegin(2)
                .dayBegin(28)
                .yearEnd(2022)
                .monthEnd(3)
                .dayEnd(1)
                .build();

        importSmallRange(rangeRunParameter);

        System.out.println("done");
    }
}
