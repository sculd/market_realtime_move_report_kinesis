package com.tradingbinance.state;

import com.google.gson.Gson;
import com.marketapi.binance.response.QueryOrder;
import com.marketapi.binance.response.QueryMarginAccountOrder;
import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.*;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;

@SuperBuilder
public class BinanceEnterInProgress extends EnterInProgress {
    @Builder.Default
    Gson gson = new Gson();

    @Override
    public EnterInProgress.EnterInProgressStatus getProgressStatus(Common.PositionSideType positionSideType, Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();

        Position position = null;
        String result;
        EnterInProgress.EnterInProgressStatus.Status status = EnterInProgress.EnterInProgressStatus.Status.ORDER_IN_PROGRESS;

        switch (positionSideType) {
            case LONG:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                result = BinanceUtil.client.createTrade().getOrder(parameters);
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
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_COMPLETE;
                        break;
                    case "fail":
                        status = EnterInProgress.EnterInProgressStatus.Status.ORDER_FAILED;
                        break;
                }
                break;
            case SHORT:
                parameters.put("symbol", symbol);
                parameters.put("orderId", Long.valueOf(orderID));
                result = BinanceUtil.client.createMargin().getOrder(parameters);
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
