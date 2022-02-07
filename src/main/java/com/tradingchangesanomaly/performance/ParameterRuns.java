package com.tradingchangesanomaly.performance;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParameterRuns {
    String runExportDir;
    List<ParameterRun> parameterRuns = new ArrayList<>();

    public ParameterRuns(String runExportDir) {
        this.runExportDir = runExportDir;
        File dir = new File(runExportDir);
        dir.mkdirs();
    }

    public void addParameterRun(ParameterRun parameterRun) {
        parameterRuns.add(parameterRun);
        appendRunToCsv(parameterRun);
    }

    void appendRunToCsv(ParameterRun parameterRun) {
        Path filename =  Paths.get(runExportDir).resolve(String.format("%s.cvs", parameterRun.changesAnomalyTradingStreamInitParameter.toCsvLine()));
        parameterRun.exportToCsv(filename.toString());
    }
}
