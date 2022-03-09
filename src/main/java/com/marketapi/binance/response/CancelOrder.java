package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class CancelOrder {
    String symbol;
    String origClientOrderId;
    long orderId;
    long orderListId;
    String clientOrderId;
    String price;
    String origQty;
    String executedQty;
    String cummulativeQuoteQty;
    String status;
    String timeInForce;
    String type;
    String side;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountCancelOrder.class)
                .add("symbol", symbol)
                .add("origClientOrderId", origClientOrderId)
                .add("orderId", orderId)
                .add("orderListId", orderListId)
                .add("origClientOrderId", origClientOrderId)
                .add("clientOrderId", clientOrderId)
                .add("price", price)
                .add("origQty", origQty)
                .add("executedQty", executedQty)
                .add("cummulativeQuoteQty", cummulativeQuoteQty)
                .add("status", status)
                .add("timeInForce", timeInForce)
                .add("type", type)
                .add("side", side)
                .toString();
    }
}
