package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class MarginAccountNewOrder {
    public String symbol;
    public int orderId;
    public String clientOrderId;
    public boolean isIsolated;
    public long transactTime;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountNewOrder.class)
                .add("symbol", symbol)
                .add("orderId", orderId)
                .add("clientOrderId", clientOrderId)
                .add("isIsolated", isIsolated)
                .add("transactTime", transactTime)
                .toString();
    }
}
