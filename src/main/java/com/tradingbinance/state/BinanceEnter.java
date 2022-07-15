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
                parameters.put("quantity", quantity);
                try {

                    result = BinanceUtil.client.createMargin().newOrder(parameters);
                    logger.error("{} long position new order result: {}", symbol, result);
                } catch (Exception ex) {
                    logger.error("{} binance error creating binance margin new order: {}", symbol, ex);
                    return failResult;
                }
                MarginAccountNewOrder newOrder = gson.fromJson(result, MarginAccountNewOrder.class);

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
                    logger.error("{} short position new account result: {}", symbol, result);
                } catch (Exception ex) {
                    logger.error("{} binance error checking out binance margin account: {}", symbol, ex);
                    return failResult;
                }
                QueryCrossMarginAccountDetails marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                double freeAmount = marginAccountDetail.getFreeAmount(asset);
                double borrowQuantity = quantity - freeAmount;
                logger.info("Will borrow {} for {}, quantity: {}, freeAmount: {}, borrowQuantity: {}", asset, symbol, quantity, freeAmount, borrowQuantity);
                if (borrowQuantity <= 0) {
                    logger.info("skip borrowing {} for {} as freeAmount {} is enough for quantity: {}", asset, symbol, freeAmount, quantity);
                } else {
                    parameters.put("asset", asset);
                    parameters.put("amount", borrowQuantity);
                    try {
                        result = binanceMargin.borrow(parameters);
                        logger.error("{} short position borrow result: {}", symbol, result);
                    } catch (Exception ex) {
                        logger.error("{} binance error borrowing: {}", symbol, ex);
                        return failResult;
                    }
                    MarginAccountBorrow borrow = gson.fromJson(result, MarginAccountBorrow.class);
                    logger.info("borrow request for {} for {}, borrowQuantity: {} is made, borrow: {}", asset, symbol, borrowQuantity, borrow);
                }

                parameters.clear();
                try {
                    result = binanceMargin.account(parameters);
                    logger.error("{} short position new account result: {}", symbol, result);
                } catch (Exception ex) {
                    logger.error("{} binance error checking margin account: {}", symbol, ex);
                    return failResult;
                }
                marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                double borrowed = marginAccountDetail.getBorrowedAmount(asset);
                logger.info("{} for {} is borrowed, amount: {}", asset, symbol, borrowed);

                logger.info("selling {}, quantity: {}", symbol, quantity);
                parameters.clear();
                parameters.put("symbol", symbol);
                parameters.put("side", "SELL");
                parameters.put("type", "MARKET");
                parameters.put("quantity", quantity);
                try {
                    result = BinanceUtil.client.createMargin().newOrder(parameters);
                    logger.error("{} short position new margin order result: {}", symbol, result);
                } catch (Exception ex) {
                    logger.error("{} binance error making new margin order: {}", symbol, ex);
                    return failResult;
                }
                MarginAccountNewOrder marginAccountNewOrder = gson.fromJson(result, MarginAccountNewOrder.class);

                return com.trading.state.Enter.ExecuteResult.builder()
                        .orderID(String.valueOf(marginAccountNewOrder.orderId))
                        .result(com.trading.state.Enter.ExecuteResult.Result.SUCCESS)
                        .build();
            default:
                logger.warn("{} binance error Invalid position type for binance enter: {}", symbol, positionSideType);
                return com.trading.state.Enter.ExecuteResult.builder()
                        .result(ExecuteResult.Result.FAIL)
                        .build();
        }
    }
}
