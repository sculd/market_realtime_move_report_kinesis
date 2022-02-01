package com.trading.recordprocessor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CSVProcessor {
    private static final Logger log = LoggerFactory.getLogger(CSVProcessor.class);
    long runStartEpochSeconds;

    class ProcessThread extends Thread {
        String csvFileName;
        int shardId;
        int shardSize;

        public ProcessThread(String csvFileName, int shardId, int shardSize) {
            this.csvFileName = csvFileName;
            this.shardId = shardId;
            this.shardSize = shardSize;
        }

        public void run()
        {
            try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
                String[] csvLine;
                while ((csvLine = reader.readNext()) != null) {
                    if (!ifProcessLine(csvLine, shardId, shardSize)) {
                        continue;
                    }
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
    }

    protected boolean ifProcessLine(String[] csvLine, int shardId, int shardSize) {
        return false;
    }

    public void run(String csvFileName) {
        runStartEpochSeconds = Instant.now().toEpochMilli() / 1000;
        int shardSize = 1;
        List<ProcessThread> processThreads = new ArrayList<>();
        for (int shardId = 0; shardId < shardSize; shardId++) {
            ProcessThread processThread = new ProcessThread(csvFileName, shardId, shardSize);
            processThreads.add(processThread);
            processThread.run();
        }
        try {
            for (ProcessThread processThread : processThreads) {
                processThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onFinish();
    }

    protected void onFinish() {
        long runFinishEpochSeconds = Instant.now().toEpochMilli() / 1000;
        long durationSedonds = runFinishEpochSeconds - runStartEpochSeconds;

        log.info(String.format("Run duration seconds: %d", durationSedonds));
    }

    protected void processCsvLine(String[] csvLine) {
    }
}
