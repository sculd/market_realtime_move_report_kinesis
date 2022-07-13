package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.NewOrder;
import com.marketapi.binance.response.MarginAccountNewOrder;
import com.marketapi.binance.response.QueryCrossMarginAccountDetails;
import com.marketapi.binance.response.ExchangeInformation;
import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookFactoryTrivial;
import com.trading.state.Exit;
import com.tradingbinance.state.BinanceUtil;
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
        String result = "";
        String orderId;
        ExecuteResult failResult = com.trading.state.Exit.ExecuteResult.builder()
                .result(Exit.ExecuteResult.Result.FAIL)
                .build();
        switch (position.positionSideType) {
            case LONG:
                parameters.put("symbol", position.symbol);
                parameters.put("side", "SELL");
                parameters.put("type", "MARKET");
                parameters.put("quantity", position.quantity);
                try {
                    result = BinanceUtil.client.createMargin().newOrder(parameters);
                } catch (Exception ex) {
                    logger.error("{} binance error creating binance new order: {}", position.symbol, ex);
                    return failResult;
                }
                MarginAccountNewOrder newOrder = gson.fromJson(result, MarginAccountNewOrder.class);
                orderId = String.valueOf(newOrder.orderId);
                break;
            case SHORT:
                String[] tokens = position.symbol.split("USD");
                String asset = tokens[0];
                try {
                    result = BinanceUtil.client.createMargin().account(parameters);
                } catch (Exception ex) {
                    logger.error("{} binance error checking binance account: {}", position.symbol, ex);
                    return failResult;
                }
                QueryCrossMarginAccountDetails marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                double borrowed = marginAccountDetail.getBorrowedAmount(asset);
                logger.info("{} had been borrowed, amount: {} repaying with buy position quantity: {}", asset, borrowed, position.quantity);

                parameters.put("symbol", position.symbol);
                parameters.put("side", "BUY");
                parameters.put("type", "MARKET");
                ExchangeInformation exchangeInformation = BinanceUtil.getExchangeInfo(position.symbol);
                double quantity = Math.max(borrowed, position.quantity);
                //quantity -= marginAccountDetail.getFreeAmount(asset);
                quantity = exchangeInformation.quantifyByStepSize(position.symbol, quantity);
                parameters.put("quantity", quantity);
                logger.info("buying {}, quantity: {}", position.symbol, quantity);
                try {
                    result = BinanceUtil.client.createMargin().newOrder(parameters);
                } catch (Exception ex) {
                    logger.error("{} binance error creating binance margin new order: {}", position.symbol, ex);
                    return failResult;
                }
                MarginAccountNewOrder marginAccountNewOrder = gson.fromJson(result, MarginAccountNewOrder.class);
                orderId = String.valueOf(marginAccountNewOrder.orderId);
                break;
            default:
                logger.warn("{} binance error Invalid position type for binance enter: {}", position.symbol, position.positionSideType);
                return failResult;
        }
        exitPriceSnapshot.price = targetPrice;
        exitPriceSnapshot.epochSeconds = position.entryPriceSnapshot.epochSeconds;
        return com.trading.state.Exit.ExecuteResult.builder()
                .orderID(orderId)
                .result(com.trading.state.Exit.ExecuteResult.Result.SUCCESS)
                .build();
    }
}
