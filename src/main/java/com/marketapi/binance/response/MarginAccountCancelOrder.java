package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class MarginAccountCancelOrder {
    public String symbol;
    public boolean isIsolated;
    public long orderId;
    public String origClientOrderId;
    public String clientOrderId;
    public String price;
    public String origQty;
    public String executedQty;
    public String cummulativeQuoteQty;
    public String status;
    public String timeInForce;
    public String type;
    public String side;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountCancelOrder.class)
                .add("symbol", symbol)
                .add("isIsolated", isIsolated)
                .add("orderId", orderId)
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

