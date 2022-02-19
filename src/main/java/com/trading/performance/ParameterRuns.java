package com.trading.performance;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParameterRuns {
    List<ParameterRun> parameterRuns = new ArrayList<>();

    public void addParameterRun(ParameterRun parameterRun) {
        parameterRuns.add(parameterRun);
    }

    void createDirIfNotPresent(String runExportDir) {
        File dir = new File(runExportDir);
        dir.mkdirs();
    }

    public void appendRunToCsv(String runExportDir, ParameterRun parameterRun) {
        createDirIfNotPresent(runExportDir);
        Path filename =  Paths.get(runExportDir).resolve(String.format("%s.csv", parameterRun.changesAnomalyTradingStreamInitParameter.toCsvLine()));
        parameterRun.exportToCsv(filename.toString());
    }
}
