package com.trading.binancestate;

import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.Common;
import com.trading.state.Enter;

import com.binance.connector.client.impl.SpotClientImpl;
import java.util.LinkedHashMap;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BinanceEnter extends Enter {
    @Builder.Default
    SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));

    public ExecuteResult execute(Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", symbol);
        parameters.put("side", "SELL");
        parameters.put("type", "MARKET");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", targetVolume);
        String result = client.createTrade().newOrder(parameters);

        return com.trading.state.Enter.ExecuteResult.builder()
                .result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
