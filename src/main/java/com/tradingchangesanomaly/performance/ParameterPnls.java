package com.tradingchangesanomaly.performance;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ParameterPnls {
    List<ParameterPnl> ParameterPnls = new ArrayList<>();

    public void addParameterPnl(ParameterPnl parameterPnl) {
        ParameterPnls.add(parameterPnl);
    }

    void createIfNotPresent(String pnlExportFileName) {
        try {
            FileWriter exportFileWriter = new FileWriter(pnlExportFileName);
            exportFileWriter.write(String.format("%s\n", ParameterPnl.toCsvHeader()));
            exportFileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void appendPnlToCsv(String pnlExportFileName, ParameterPnl parameterPnl) {
        createIfNotPresent(pnlExportFileName);
        String line = parameterPnl.toCsvLine();
        try {
            Files.write(Paths.get(pnlExportFileName), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    public void exportToCsv(String pnlExportFileName) {
        FileWriter exportFileWriter = null;
        try {
            exportFileWriter = new FileWriter(pnlExportFileName);
            exportFileWriter.write(String.format("%s\n", ParameterPnl.toCsvHeader()));
            for (ParameterPnl pnl : ParameterPnls) {
                exportFileWriter.write(String.format("%s\n", pnl.toCsvLine()));
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            if (exportFileWriter != null) {
                exportFileWriter.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    static public ParameterPnls fromCsvFile(String csvFileName) {
        ParameterPnls parameterPnls = new ParameterPnls();
        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] csvLine;
            while ((csvLine = reader.readNext()) != null) {
                ParameterPnl parameterPnl = ParameterPnl.fromCsvLine(String.join(",", csvLine));
                parameterPnls.addParameterPnl(parameterPnl);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        return parameterPnls;
    }
}
