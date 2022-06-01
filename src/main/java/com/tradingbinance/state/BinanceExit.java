package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.NewOrder;
import com.marketapi.binance.response.MarginAccountNewOrder;
import com.trading.state.Exit;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

@SuperBuilder
public class BinanceExit extends Exit {
    private static final Logger logger = LoggerFactory.getLogger(BinanceExit.class);

    @Builder.Default
    Gson gson = new Gson();

    public ExecuteResult execute() {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        String result;
        String orderId;
        switch (position.positionSideType) {
            case LONG:
                parameters.put("symbol", position.symbol);
                parameters.put("side", "SELL");
                parameters.put("type", "MARKET");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", position.quantity);
                result = BinanceUtil.client.createTrade().newOrder(parameters);
                NewOrder newOrder = gson.fromJson(result, NewOrder.class);
                orderId = String.valueOf(newOrder.orderId);
                break;
            case SHORT:
                parameters.put("symbol", position.symbol);
                parameters.put("side", "BUY");
                parameters.put("type", "MARKET");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", position.quantity);
                result = BinanceUtil.client.createMargin().newOrder(parameters);
                MarginAccountNewOrder marginAccountNewOrder = gson.fromJson(result, MarginAccountNewOrder.class);
                orderId = String.valueOf(marginAccountNewOrder.orderId);
            default:
                logger.warn("Invalid position type for binance enter: {}", position.positionSideType);
                return com.trading.state.Exit.ExecuteResult.builder()
                        .result(Exit.ExecuteResult.Result.FAIL)
                        .build();
        }
        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return com.trading.state.Exit.ExecuteResult.builder()
                .orderID(orderId)
                .result(com.trading.state.Exit.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
