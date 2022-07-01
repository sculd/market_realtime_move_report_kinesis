package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.QueryOrder;
import com.marketapi.binance.response.QueryMarginAccountOrder;
import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.*;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

@SuperBuilder
public class BinanceEnterInProgress extends EnterInProgress {
    private static final Logger logger = LoggerFactory.getLogger(BinanceEnterInProgress.class);
    @Builder.Default
    Gson gson = new Gson();

    @Override
    public EnterInProgress.EnterInProgressStatus getProgressStatus(Common.PositionSideType positionSideType, Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();

        Position position = null;
        String result = "";
        EnterInProgress.EnterInProgressStatus.Status status = EnterInProgress.EnterInProgressStatus.Status.ORDER_IN_PROGRESS;

        switch (positionSideType) {
            case LONG:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                try {
                    result = BinanceUtil.client.createTrade().getOrder(parameters);
                } catch (Exception ex) {
                    logger.error("binance error getting an order", ex);
                    status = EnterInProgress.EnterInProgressStatus.Status.ORDER_FAILED;
                    break;
                }
                QueryOrder queryOrder = gson.fromJson(result, QueryOrder.class);

                position = Position.builder()
                        .market(market)
                        .symbol(symbol)
                        .positionSideType(positionSideType)
                        .entryPriceSnapshot(Common.PriceSnapshot.builder()
                                .price(Double.parseDouble(queryOrder.price))
                                .epochSeconds(entryPriceSnapshot.epochSeconds)
                                .build())
                        .quantity(Double.parseDouble(queryOrder.executedQty))
                        .analysesUponEnter(analysesUponEnter)
                        .build();

                String statusLower = queryOrder.status.toLowerCase();
                switch (statusLower) {
                    case "new":
                        break;
                    case "success":
                    case "filled":
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_COMPLETE;
                        break;
                    case "fail":
                    case "failed":
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_FAILED;
                        break;
                }
                break;
            case SHORT:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                try {
                    result = BinanceUtil.client.createMargin().getOrder(parameters);
                } catch (Exception ex) {
                    logger.error("binance error getting a margin order", ex);
                    status = EnterInProgress.EnterInProgressStatus.Status.ORDER_FAILED;
                    break;
                }
                QueryMarginAccountOrder queryMarginAccountOrder = gson.fromJson(result, QueryMarginAccountOrder.class);

                position = Position.builder()
                        .market(market)
                        .symbol(symbol)
                        .positionSideType(positionSideType)
                        .entryPriceSnapshot(Common.PriceSnapshot.builder()
                                .price(Double.parseDouble(queryMarginAccountOrder.price))
                                .epochSeconds(entryPriceSnapshot.epochSeconds)
                                .build())
                        .quantity(Double.parseDouble(queryMarginAccountOrder.executedQty))
                        .analysesUponEnter(analysesUponEnter)
                        .build();

                statusLower = queryMarginAccountOrder.status.toLowerCase();
                switch (statusLower) {
                    case "new":
                        break;
                    case "success":
                    case "filled":
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_COMPLETE;
                        break;
                    case "fail":
                    case "failed":
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_FAILED;
                        break;
                }
                break;
        }

        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .exitPlanInitParameter(exitPlanInitParameter)
                .position(position)
                .build();
        exitPlan.init(position);

        return EnterInProgress.EnterInProgressStatus.builder()
                .exitPlan(exitPlan)
                .position(position)
                .status(status).build();
    }
}
