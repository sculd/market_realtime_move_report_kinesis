package com.marketsignal.timeseries.analysis;

import java.util.List;

public abstract class Analysis {
    abstract public List<String> getCsvHeaderColumns();

    abstract public List<String> getCsvValueColumns();
}
