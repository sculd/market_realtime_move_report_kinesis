package com.marketdata.imports;

import java.util.Arrays;
import static org.junit.Assert.assertThat;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class QueryTemplatesTest {
    @Test
    public void testGetMinuteAggregationQuery() {
        QueryTemplates templates = new QueryTemplates();
        String template = templates.getMinuteAggregationQuery("dummy_project_id", QueryTemplates.Table.BINANCE_BAR_WITH_TIME, Arrays.asList("dummy_symbol1", "dummy_symbol2"), 0, 600);

        assertThat(template, StringContains.containsString("symbol = \"dummy_symbol1\" OR symbol = \"dummy_symbol2\""));
    }
}
