package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.MarginAccountRepay;
import com.marketapi.binance.response.QueryCrossMarginAccountDetails;
import com.marketapi.binance.response.QueryOrder;
import com.marketapi.binance.response.QueryMarginAccountOrder;
import com.trading.state.Common;
import com.trading.state.ExitInProgress;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

@SuperBuilder
public class BinanceExitInProgress  extends ExitInProgress {
    private static final Logger logger = LoggerFactory.getLogger(BinanceExitInProgress.class);

    @Builder.Default
    Gson gson = new Gson();

    @Override
    public ExitInProgressStatus getProgressStatus(Common.PositionSideType positionSideType) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();

        String result;
        ExitInProgressStatus.Status status = ExitInProgressStatus.Status.ORDER_IN_PROGRESS;

        switch (positionSideType) {
            case LONG:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                result = BinanceUtil.client.createTrade().getOrder(parameters);
                QueryOrder queryOrder = gson.fromJson(result, QueryOrder.class);

                switch (queryOrder.status.toLowerCase()) {
                    case "new":
                        break;
                    case "success":
                        status = ExitInProgressStatus.Status.ORDER_COMPLETE;
                        break;
                    case "fail":
                        status = ExitInProgressStatus.Status.ORDER_FAILED;
                        break;
                }
                break;
            case SHORT:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                result = BinanceUtil.client.createMargin().getOrder(parameters);
                QueryMarginAccountOrder queryMarginAccountOrder = gson.fromJson(result, QueryMarginAccountOrder.class);

                switch (queryMarginAccountOrder.status.toLowerCase()) {
                    case "new":
                        logger.info("{} buy order for exit yet new", symbol);
                        break;
                    case "success":
                    case "filled":
                        logger.info("{} buy order for exit is filled", symbol);
                        status = ExitInProgressStatus.Status.ORDER_COMPLETE;
                        String[] tokens = symbol.split("USD");
                        String asset = tokens[0];

                        result = BinanceUtil.client.createMargin().account(new LinkedHashMap<String,Object>());
                        QueryCrossMarginAccountDetails marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
                        double borrowed = marginAccountDetail.getBorrowedAmount(asset);
                        double freeAmount = marginAccountDetail.getFreeAmount(asset);
                        logger.info("{} was borrowed, amount: {} repaying with freeAmount: {}", asset, borrowed, freeAmount);

                        parameters.clear();
                        parameters.put("asset", asset);
                        parameters.put("amount", Math.min(borrowed, freeAmount));
                        result = BinanceUtil.client.createMargin().repay(parameters);
                        MarginAccountRepay repay = gson.fromJson(result, MarginAccountRepay.class);
                        logger.info("{} is repaid, repay: {}", asset, repay);
                        break;
                    case "fail":
                    case "failed":
                        logger.info("{} buy order for exit has failed", symbol);
                        status = ExitInProgressStatus.Status.ORDER_FAILED;
                        break;
                }
                break;
        }

        return ExitInProgressStatus.builder()
                .status(status).build();
    }
}
