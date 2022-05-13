package com.marketdata.imports;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BigQueryImport {
    private static final Logger log = LoggerFactory.getLogger(BigQueryImport.class);

    String projectId = System.getenv("GCP_PROJECT_ID");

    BigQuery bigquery;

    public BigQueryImport() {
        File credentialsPath = new File("credential.json");

        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            bigquery =
                    BigQueryOptions.newBuilder()
                            .setCredentials(credentials)
                            .setProjectId(projectId)
                            .build()
                            .getService();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Builder
    public static class ImportParam {
        String baseDirPath;
        QueryTemplates.Table table;
        @Builder.Default
        public List<String> symbols = new ArrayList<>();
        public long startEpochSeconds;
        public long endEpochSeconds;
    }

    static public String getImportedFileName(ImportParam importParam) {
        String symbolsStr = "ALL";
        if (!importParam.symbols.isEmpty()) {
            symbolsStr = importParam.symbols.stream().collect(Collectors.joining(","));
        }
        return String.format("%s%s_%s_%s_%s.csv", importParam.baseDirPath, importParam.table.dataSetTableId(), symbolsStr, Instant.ofEpochSecond(importParam.startEpochSeconds).toString(), Instant.ofEpochSecond(importParam.endEpochSeconds).toString());
    }

    static public boolean getIfFileExist(ImportParam importParam) {
        String filename = getImportedFileName(importParam);
        return new File(filename).exists();
    }

    public static String formatNumber(Number n) {
        NumberFormat format = DecimalFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(8);
        format.setGroupingUsed(false);
        return format.format(n);
    }

    public void importAsCSV(ImportParam importParam) {
        String filename = getImportedFileName(importParam);
        if (new File(filename).exists()) {
            try {
                long fileSize = Files.size(Paths.get(filename));
                if (fileSize == 0) {
                    log.info(String.format("The import file %s is already present but size zero, thus preceeding", filename));
                } else {
                    log.info(String.format("The import file %s is already present and of non-zero size.", filename));
                    return;
                }
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }

        QueryTemplates templates = new QueryTemplates();
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration
                        .newBuilder(templates.getMinuteAggregationQuery(projectId, importParam.table, importParam.symbols, importParam.startEpochSeconds, importParam.endEpochSeconds))
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        try {
            log.info(String.format("running a query: \n%s", queryJob.getConfiguration().toString()));
            queryJob = queryJob.waitFor();

            if (queryJob == null) {
                throw new RuntimeException("Job no longer exists");
            } else if (queryJob.getStatus().getError() != null) {
                throw new RuntimeException(queryJob.getStatus().getError().toString());
            }

            TableResult result = queryJob.getQueryResults();
            try {
                FileWriter csvWriter = new FileWriter(filename);
                csvWriter.write("timestamp,symbol,open,high,low,close,volume\n");
                for (FieldValueList row : result.iterateAll()) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%f\n",
                            row.get("timestamp").getTimestampValue() / 1000000,
                            row.get("symbol").getStringValue(),
                            formatNumber(row.get("open").getDoubleValue()),
                            formatNumber(row.get("high").getDoubleValue()),
                            formatNumber(row.get("low").getDoubleValue()),
                            formatNumber(row.get("close").getDoubleValue()),
                            row.get("volume").getDoubleValue());
                    csvWriter.write(line);
                }
                csvWriter.close();
                System.out.println(String.format("Successfully wrote to the file: %s", filename));
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        } catch (InterruptedException ex) {
        }
    }
}
