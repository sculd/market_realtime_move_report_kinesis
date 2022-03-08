package com.trading.binancestate;

import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.Common;
import com.trading.state.Enter;
import com.trading.state.ExitPlan;
import com.trading.state.Position;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import java.util.LinkedHashMap;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BinanceEnter extends Enter {
    @Builder.Default
    SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));

    /*
    @Builder
    public BinanceEnter(String market, String symbol, Common.PositionSideType positionSideType, double targetPrice, double targetVolume, ExitPlan.ExitPlanInitParameter exitPlanInitParameter) {
        super(market, symbol, positionSideType, targetPrice, targetVolume, exitPlanInitParameter);
    }
    //*/

    public ExecuteResult execute(Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", symbol);
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", targetVolume);
        parameters.put("price", 9500);
        String result = client.createTrade().testNewOrder(parameters);

        Position position = Position.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(positionSideType)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(targetPrice)
                        .epochSeconds(priceSnapshot.epochSeconds)
                        .build())
                .volume(targetVolume)
                .analysesUponEnter(analyses)
                .build();
        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .exitPlanInitParameter(exitPlanInitParameter)
                .position(position)
                .build();
        exitPlan.init(position);

        return com.trading.state.Enter.ExecuteResult.builder().result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                .position(position)
                .exitPlan(exitPlan)
                .build();
    }
}
