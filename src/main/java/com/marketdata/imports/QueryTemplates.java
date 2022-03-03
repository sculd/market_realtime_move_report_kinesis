package com.marketdata.imports;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringSubstitutor;

public class QueryTemplates {
    final String QUERY_TEMPLATE_FILE_MINUTE_AGGREGATION = "query_template_minute_aggregation.txt";

    String query_template_minute_aggregation;

    public QueryTemplates() {
        try {
            Stream<String> lines = Files.lines(
                    Paths.get(ClassLoader.getSystemResource(QUERY_TEMPLATE_FILE_MINUTE_AGGREGATION).toURI())
            );
            query_template_minute_aggregation = lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public enum Table {
        POLYGON_BAR_WITH_TIME ("market_data.by_minute"),
        BINANCE_BAR_WITH_TIME ("market_data_binance.by_minute"),
        GEMINI_BAR_WITH_TIME ("market_data_gemini.by_minute");

        private final String dataSetTableId;
        Table(String dataSetTableId) {
            this.dataSetTableId = dataSetTableId;
        }
        public String dataSetTableId() { return dataSetTableId; }

        public String getFullTableId(String projectId) {
            return String.format("%s.%s", projectId, dataSetTableId);
        }
    }

    public String getMinuteAggregationQuery(String gcpProjectId, Table table, List<String> symbols, long startEpochSeconds, long endEpochSeconds) {
        String symbolClause = "AND TRUE";
        if (!symbols.isEmpty()) {
            symbolClause = String.format("AND (%s)", symbols.stream().map(s -> "symbol = \"" + s + "\"").collect(Collectors.joining(" OR ")));
        }
        Map<String, String> values = new HashMap<>();
        values.put("table_name", table.getFullTableId(gcpProjectId));
        values.put("timestamp_clause", String.format("AND timestamp >= TIMESTAMP_SECONDS(%d) AND timestamp < TIMESTAMP_SECONDS(%d)", startEpochSeconds, endEpochSeconds));
        values.put("symbol_clause", symbolClause);
        values.put("timewindow_seconds", "60");
        StringSubstitutor sub = new StringSubstitutor(values);
        return sub.replace(query_template_minute_aggregation);
    }
}
