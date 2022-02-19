package com.marketdata;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import com.marketdata.util.Csv;
import com.marketdata.util.Time;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MainBinance {
    private static final Logger log = LoggerFactory.getLogger(MainBinance.class);

    @Builder
    static public class RangeRunParameter {
        int yearBegin;
        int monthBegin;
        int dayBegin;
        int yearEnd;
        int monthEnd;
        int dayEnd;

        public String toFileNamePhrase() {
            return String.format("from_%d_%d_%d_to_%d_%d_%d", yearBegin, monthBegin, dayBegin, yearEnd, monthEnd, dayEnd);
        }

        BigQueryImport.ImportParam getImportParam() {
            return BigQueryImport.ImportParam.builder()
                    .baseDirPath("marketdata/")
                    .table(QueryTemplates.Table.BINANCE_BAR_WITH_TIME)
                    .symbols(Arrays.asList())
                    .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearBegin, monthBegin, dayBegin, 0, 0))
                    .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearEnd, monthEnd, dayEnd, 0, -1))
                    .build();
        }

        List<RangeRunParameter> split(Duration interval) {
            List<RangeRunParameter> res = new ArrayList<>();
            Instant t = Time.fromYearMonthDayHourMinuteToNewYorkDateTime(yearBegin, monthBegin, dayBegin, 0, 0).toInstant();
            Instant tEnd = Time.fromYearMonthDayHourMinuteToNewYorkDateTime(yearEnd, monthEnd, dayEnd, 0, -1).toInstant();
            while (t.isBefore(tEnd)) {
                Instant tSplitEnd = t.plus(interval);
                if (tSplitEnd.isAfter(tEnd)) {
                    tSplitEnd = tEnd;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                cal.setTime(Date.from(t));
                Calendar calSplitEnd = Calendar.getInstance();
                calSplitEnd.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                calSplitEnd.setTime(Date.from(tSplitEnd));

                RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                        .yearBegin(cal.get(Calendar.YEAR))
                        .monthBegin(cal.get(Calendar.MONTH)+1)
                        .dayBegin(cal.get(Calendar.DATE))
                        .yearEnd(calSplitEnd.get(Calendar.YEAR))
                        .monthEnd(calSplitEnd.get(Calendar.MONTH)+1)
                        .dayEnd(calSplitEnd.get(Calendar.DATE))
                        .build();

                res.add(rangeRunParameter);
                t = tSplitEnd;
            }
            return res;
        }
    }

    static void importSmallRange(RangeRunParameter rangeRunParameter)
    {
        BigQueryImport bqImport = new BigQueryImport();
        System.out.println(String.format("[importSmallRange] %s", rangeRunParameter.toFileNamePhrase()));
        BigQueryImport.ImportParam param = rangeRunParameter.getImportParam();
        bqImport.importAsCSV(param);
    }

    static void importRange(RangeRunParameter rangeRunParameter)
    {
        List<String> smallRangeFileNames = new ArrayList<>();
        List<RangeRunParameter> rangeRunParameters = rangeRunParameter.split(Duration.ofDays(10));
        for (RangeRunParameter smallRangeRunParameter: rangeRunParameters) {
            // divide the import range by bucket of 10 days as too long range causes tiem out error.
            importSmallRange(smallRangeRunParameter);

            BigQueryImport.ImportParam importParam = smallRangeRunParameter.getImportParam();
            smallRangeFileNames.add(BigQueryImport.getImportedFileName(importParam));
        }


        BigQueryImport.ImportParam importParam = rangeRunParameter.getImportParam();
        try {
            Csv.mergeCsvFiles(smallRangeFileNames, BigQueryImport.getImportedFileName(importParam));
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    public static void main(String[] args)
    {
        System.out.println("ingesting");
        int year = 2022;

        RangeRunParameter rangeRunParameter = RangeRunParameter.builder()
                .yearBegin(year)
                .monthBegin(1)
                .dayBegin(1)
                .yearEnd(year)
                .monthEnd(1)
                .dayEnd(31)
                .build();

        importRange(rangeRunParameter);

        System.out.println("done");
    }
}
