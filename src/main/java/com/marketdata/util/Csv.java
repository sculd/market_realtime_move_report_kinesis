package com.marketdata.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class Csv {
    private static final Logger log = LoggerFactory.getLogger(Csv.class);

    public static void mergeCsvFiles(List<String> fileNames, String outFileName) throws IOException {
        if (fileNames.size() < 2) {
            return;
        }

        File fileOutput = new File(outFileName);
        if (fileOutput.exists()) {
            fileOutput.delete();
        }
        try {
            fileOutput.createNewFile();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // Header is the first line
        String header = null;
        Scanner scanner = new Scanner(new File(fileNames.get(0)));
        if (scanner.hasNextLine()) {
            header = scanner.nextLine();
        }
        scanner.close();

        ArrayList<File> files = new ArrayList<>();
        for (String fileName : fileNames) {
            files.add(new File(fileName));
        }

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outFileName, true));
        fileWriter.write(header);
        fileWriter.newLine();

        for (File file : files) {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));

            // first line
            String line = fileReader.readLine();

            while ((line = fileReader.readLine()) != null) {
                fileWriter.write(line);
                fileWriter.newLine();
            }
            fileReader.close();
        }

        fileWriter.close();
    }
}
