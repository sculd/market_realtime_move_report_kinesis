package com.marketsignal.timeseries.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Analyses {
    public List<Analysis> analysisList = new ArrayList<>();

    public String toCsvHeader() {
        return String.join(",", analysisList.stream().map(al -> String.join(",", al.getCsvHeaderColumns())).collect(Collectors.toList()));
    }

    public String toCsvLine() {
        return String.join(",", analysisList.stream().map(al -> String.join(",", al.getCsvValueColumns())).collect(Collectors.toList()));
    }
}
