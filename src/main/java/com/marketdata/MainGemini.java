package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Csv;
import com.marketdata.util.RangeRunParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MainGemini {
    private static final Logger log = LoggerFactory.getLogger(MainGemini.class);

    static void importSmallRange(RangeRunParameter rangeRunParameter)
    {
        BigQueryImport bqImport = new BigQueryImport();
        System.out.println(String.format("[importSmallRange] %s", rangeRunParameter.toFileNamePhrase()));
        BigQueryImport.ImportParam param = rangeRunParameter.getImportParam(QueryTemplates.Table.GEMINI_BAR_WITH_TIME);
        bqImport.importAsCSV(param);
    }

    static void importRange(RangeRunParameter rangeRunParameter)
    {
        List<String> smallRangeFileNames = new ArrayList<>();
        List<RangeRunParameter> rangeRunParameters = rangeRunParameter.split(Duration.ofDays(10));
        for (RangeRunParameter smallRangeRunParameter: rangeRunParameters) {
            // divide the import range by bucket of 10 days as too long range causes tiem out error.
            importSmallRange(smallRangeRunParameter);

            BigQueryImport.ImportParam importParam = smallRangeRunParameter.getImportParam(QueryTemplates.Table.GEMINI_BAR_WITH_TIME);
            smallRangeFileNames.add(BigQueryImport.getImportedFileName(importParam));
        }

        BigQueryImport.ImportParam importParam = rangeRunParameter.getImportParam(QueryTemplates.Table.GEMINI_BAR_WITH_TIME);
        try {
            Csv.mergeCsvFiles(smallRangeFileNames, BigQueryImport.getImportedFileName(importParam));
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    public static void main(String[] args)
    {
        System.out.println("ingesting");
        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(2022)
                .monthBegin(3)
                .dayBegin(1)
                .yearEnd(2022)
                .monthEnd(3)
                .dayEnd(2)
                .build();

        importRange(rangeRunParameter);

        System.out.println("done");
    }
}
