package com.marketdata.util;

import com.marketdata.imports.BigQueryImport;
import com.marketdata.imports.QueryTemplates;
import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Builder
public class RangeRunParameter {
    int yearBegin;
    int monthBegin;
    int dayBegin;
    int yearEnd;
    int monthEnd;
    int dayEnd;

    public String toFileNamePhrase() {
        return String.format("from_%d_%d_%d_to_%d_%d_%d", yearBegin, monthBegin, dayBegin, yearEnd, monthEnd, dayEnd);
    }

    public BigQueryImport.ImportParam getImportParam(QueryTemplates.Table table) {
        return BigQueryImport.ImportParam.builder()
                .baseDirPath("marketdata/")
                .table(table)
                .symbols(Arrays.asList())
                .startEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearBegin, monthBegin, dayBegin, 0, 0))
                .endEpochSeconds(Time.fromNewYorkDateTimeInfoToEpochSeconds(yearEnd, monthEnd, dayEnd, 0, -1))
                .build();
    }

    public List<RangeRunParameter> split(Duration interval) {
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
