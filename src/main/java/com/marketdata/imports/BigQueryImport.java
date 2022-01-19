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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

    static public String getImportedFileName(String baseDirPath, QueryTemplates.Table table, List<String> symbols, long startEpochSeconds, long endEpochSeconds) {
        String symbolsStr = "ALL";
        if (!symbols.isEmpty()) {
            symbolsStr = symbols.stream().collect(Collectors.joining(","));
        }
        return String.format("%s%s_%s_%s_%s.csv", baseDirPath, table.dataSetTableId(), symbolsStr, Instant.ofEpochSecond(startEpochSeconds).toString(), Instant.ofEpochSecond(endEpochSeconds).toString());
    }

    public void importAsCSV(String baseDirPath, QueryTemplates.Table table, List<String> symbols, long startEpochSeconds, long endEpochSeconds) {
        String filename = getImportedFileName(baseDirPath, table, symbols, startEpochSeconds, endEpochSeconds);
        if (new File(filename).exists()) {
            log.info(String.format("The import file %s is already present.", filename));
            return;
        }

        QueryTemplates templates = new QueryTemplates();
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration
                        .newBuilder(templates.getMinuteAggregationQuery(projectId, table, symbols, startEpochSeconds, endEpochSeconds))
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        try {
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
                    String line = String.format("%s,%s,%f,%f,%f,%f,%f\n",
                            row.get("timestamp").getTimestampValue() / 1000000,
                            row.get("symbol").getStringValue(),
                            row.get("open").getDoubleValue(),
                            row.get("high").getDoubleValue(),
                            row.get("low").getDoubleValue(),
                            row.get("close").getDoubleValue(),
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
