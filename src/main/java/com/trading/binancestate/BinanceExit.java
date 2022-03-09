package com.trading.binancestate;

import com.google.gson.Gson;
import com.marketapi.binance.response.NewOrder;
import com.trading.state.Exit;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class BinanceExit extends Exit {
    private static final Logger logger = LoggerFactory.getLogger(BinanceExit.class);

    @Builder.Default
    Gson gson = new Gson();

    public ExecuteResult execute() {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", position.symbol);
        switch (position.positionSideType) {
            case LONG:
                parameters.put("side", "SELL");
                break;
            case SHORT:
                return com.trading.state.Exit.ExecuteResult.builder()
                        .result(Exit.ExecuteResult.Result.FAIL)
                        .build();
            default:
                logger.warn("Invalid position type for binance enter: {}", position.positionSideType);
                return com.trading.state.Exit.ExecuteResult.builder()
                        .result(Exit.ExecuteResult.Result.FAIL)
                        .build();
        }
        parameters.put("type", "MARKET");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", position.quantity);
        String result = BinanceUtil.client.createTrade().newOrder(parameters);
        NewOrder newOrder = gson.fromJson(result, NewOrder.class);

        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return com.trading.state.Exit.ExecuteResult.builder()
                .orderID(String.valueOf(newOrder.orderId))
                .result(com.trading.state.Exit.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
