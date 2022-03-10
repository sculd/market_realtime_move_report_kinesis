package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.Common;
import com.trading.state.Enter;
import com.marketapi.binance.response.NewOrder;
import java.util.LinkedHashMap;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuperBuilder
public class BinanceEnter extends Enter {
    private static final Logger logger = LoggerFactory.getLogger(BinanceEnter.class);

    @Builder.Default
    Gson gson = new Gson();

    public ExecuteResult execute(Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", symbol);
        switch (positionSideType) {
            case LONG:
                parameters.put("side", "BUY");
                break;
            case SHORT:
                return com.trading.state.Enter.ExecuteResult.builder()
                        .result(ExecuteResult.Result.FAIL)
                        .build();
            default:
                logger.warn("Invalid position type for binance enter: {}", positionSideType);
                return com.trading.state.Enter.ExecuteResult.builder()
                        .result(ExecuteResult.Result.FAIL)
                        .build();
        }
        parameters.put("type", "MARKET");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", targetVolume);
        String result = BinanceUtil.client.createTrade().newOrder(parameters);
        NewOrder newOrder = gson.fromJson(result, NewOrder.class);

        return com.trading.state.Enter.ExecuteResult.builder()
                .orderID(String.valueOf(newOrder.orderId))
                .result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
