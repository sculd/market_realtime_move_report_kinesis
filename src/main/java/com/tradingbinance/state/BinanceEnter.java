package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.*;
import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.Common;
import com.trading.state.Enter;
import com.binance.connector.client.impl.spot.Margin;
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
        String result;
        ExchangeInformation exchangeInformation = BinanceUtil.getExchangeInfo(symbol);
        double quantity = exchangeInformation.quantifyByStepSize(symbol, targetVolume);
        ExecuteResult failResult = com.trading.state.Enter.ExecuteResult.builder()
                .result(ExecuteResult.Result.FAIL)
                .build();
        switch (positionSideType) {
            case LONG:
                parameters.put("side", "BUY");
                parameters.put("type", "MARKET");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", quantity);
                try {
                    result = BinanceUtil.client.createTrade().newOrder(parameters);
                } catch (Exception ex) {
                    logger.error("binance error creating binance new order", ex);
                    return failResult;
                }
                NewOrder newOrder = gson.fromJson(result, NewOrder.class);

                return com.trading.state.Enter.ExecuteResult.builder()
                        .orderID(String.valueOf(newOrder.orderId))
                        .result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                        .build();
            case SHORT:
                String[] tokens = symbol.split("USD");
                String asset = tokens[0];

                Margin binanceMargin = BinanceUtil.client.createMargin();

                try {
                    result = binanceMargin.account(parameters);
                } catch (Exception ex) {
                    logger.error("binance error checking out binance margin account", ex);
                    return failResult;
                }
                QueryCrossMarginAccountDetails marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                double freeAmount = marginAccountDetail.getFreeAmount(asset);
                double borrowQuantity = quantity - freeAmount;
                logger.info("Will borrow {}, quantity: {}, freeAmount: {}, borrowQuantity: {}", asset, quantity, freeAmount, borrowQuantity);
                if (borrowQuantity <= 0) {
                    logger.info("skip borrowing {} as freeAmount {} is enough for quantity: {}", asset, freeAmount, quantity);
                } else {
                    parameters.put("asset", asset);
                    parameters.put("amount", borrowQuantity);
                    try {
                        result = binanceMargin.borrow(parameters);
                    } catch (Exception ex) {
                        logger.error("error borrowing", ex);
                        return failResult;
                    }
                    MarginAccountBorrow borrow = gson.fromJson(result, MarginAccountBorrow.class);
                    logger.info("borrow request for {}, borrowQuantity: {} is made, borrow: {}", asset, borrowQuantity, borrow);
                }

                parameters.clear();
                try {
                    result = binanceMargin.account(parameters);
                } catch (Exception ex) {
                    logger.error("binance error checking margin account", ex);
                    return failResult;
                }
                marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                double borrowed = marginAccountDetail.getBorrowedAmount(asset);
                logger.info("{} is borrowed, amount: {}", asset, borrowed);

                logger.info("selling {}, quantity: {}", symbol, quantity);
                parameters.clear();
                parameters.put("symbol", symbol);
                parameters.put("side", "SELL");
                parameters.put("type", "MARKET");
                parameters.put("quantity", quantity);
                try {
                    result = BinanceUtil.client.createMargin().newOrder(parameters);
                } catch (Exception ex) {
                    logger.error("binance error making new margin order", ex);
                    return failResult;
                }
                MarginAccountNewOrder marginAccountNewOrder = gson.fromJson(result, MarginAccountNewOrder.class);

                return com.trading.state.Enter.ExecuteResult.builder()
                        .orderID(String.valueOf(marginAccountNewOrder.orderId))
                        .result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                        .build();
            default:
                logger.warn("Invalid position type for binance enter: {}", positionSideType);
                return com.trading.state.Enter.ExecuteResult.builder()
                        .result(ExecuteResult.Result.FAIL)
                        .build();
        }
    }
}
