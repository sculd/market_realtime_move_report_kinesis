package com.trading.binancestate;

import com.google.gson.Gson;
import com.marketapi.binance.response.QueryOrder;
import com.trading.state.ExitInProgress;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;

@SuperBuilder
public class BinanceExitInProgress  extends ExitInProgress {
    @Builder.Default
    Gson gson = new Gson();

    public ExitInProgressStatus getProgressStatus() {
        LinkedHashMap<String,Object> parameters;
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", Long.valueOf(orderID));
        String result = BinanceUtil.client.createTrade().newOrder(parameters);
        QueryOrder queryOrder = gson.fromJson(result, QueryOrder.class);

        ExitInProgressStatus.Status status = ExitInProgressStatus.Status.ORDER_IN_PROGRESS;
        String statusLower = queryOrder.status.toLowerCase();
        switch (statusLower) {
            case "new":
                break;
            case "success":
                status = ExitInProgressStatus.Status.ORDER_COMPLETE;
                break;
            case "fail":
                status = ExitInProgressStatus.Status.ORDER_FAILED;
                break;
        }

        return ExitInProgressStatus.builder()
                .status(status).build();
    }
}
