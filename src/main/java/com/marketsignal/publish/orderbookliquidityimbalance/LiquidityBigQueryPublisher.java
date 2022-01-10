package com.marketsignal.publish.orderbookliquidityimbalance;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.marketsignal.orderbook.analysis.LiquidityImbalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LiquidityBigQueryPublisher {
    private static final Logger log = LoggerFactory.getLogger(LiquidityBigQueryPublisher.class);

    List<LiquidityImbalance.Analysis> orderFlowImbalanceAnalysisWriteBuffer = new ArrayList<>();
    private final int WRITE_BUFFER_SIZE = 50;

    public Map<String, Object> analysisToRow(LiquidityImbalance.Analysis analysis) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("timestamp", analysis.epochSeconds);
        ret.put("symbol", analysis.symbol);
        ret.put("bidPrice", analysis.bidPrice);
        ret.put("askPrice", analysis.askPrice);
        ret.put("liquidityImbalance5", analysis.liquidityImbalance5);
        ret.put("liquidityImbalance10", analysis.liquidityImbalance10);
        ret.put("liquidityImbalance15", analysis.liquidityImbalance15);
        ret.put("liquidityImbalance20", analysis.liquidityImbalance20);
        return ret;
    }

    public void publish(List<LiquidityImbalance.Analysis> liquidityImbalanceAnalysisList) {
        String projectId = System.getenv("GCP_PROJECT_ID");
        String datasetName = System.getenv("GCP_BIGQUERY_DATASET_ID");
        String tableName = System.getenv("GCP_BIGQUERY_TABLE_ID_ORDERBOOK_LIQUIDITY_IMBALANCE");

        try {
            // Initialize client that will be used to send requests. This client only needs to be created
            // once, and can be reused for multiple requests.
            BigQueryOptions options = BigQueryOptions.newBuilder().setProjectId(projectId).build();
            BigQuery bigquery = options.getService();
            TableId tableId = TableId.of(datasetName, tableName);

            InsertAllRequest.Builder requestBuilder = InsertAllRequest.newBuilder(tableId);
            for (LiquidityImbalance.Analysis analysis : liquidityImbalanceAnalysisList) {
                requestBuilder.addRow(analysisToRow(analysis));
            }

            // Inserts rowContent into datasetName:tableId.
            InsertAllResponse response =
                    bigquery.insertAll(requestBuilder.build());

            if (response.hasErrors()) {
                // If any of the insertions failed, this lets you inspect the errors
                for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                    log.error("Response error: " + entry.getValue());
                }
            }
            log.info("Rows successfully inserted into table");
        } catch (BigQueryException e) {
            log.info("Insert operation not performed \n" + e.toString());
        }
    }

    public void publish(LiquidityImbalance.Analysis orderFlowImbalanceAnalysis) {
        orderFlowImbalanceAnalysisWriteBuffer.add(orderFlowImbalanceAnalysis);
        if (orderFlowImbalanceAnalysisWriteBuffer.size() >= WRITE_BUFFER_SIZE) {
            List<LiquidityImbalance.Analysis> writeItems = new ArrayList<>(orderFlowImbalanceAnalysisWriteBuffer);
            orderFlowImbalanceAnalysisWriteBuffer.clear();
            publish(writeItems);
        }
    }
}
