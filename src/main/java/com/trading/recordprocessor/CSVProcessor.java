package com.trading.recordprocessor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVProcessor {

    public void run(String csvFileName) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] csvLine;
            while ((csvLine = reader.readNext()) != null) {
                processCsvLine(csvLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    void processCsvLine(String[] csvLine) {
    }
}
