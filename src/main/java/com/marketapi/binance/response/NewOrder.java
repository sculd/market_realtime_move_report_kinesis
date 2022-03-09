package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class NewOrder {
    public String symbol;
    public int orderId;
    public int orderListId;
    public String clientOrderId;
    public long transactTime;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NewOrder.class)
                .add("symbol", symbol)
                .add("orderId", orderId)
                .add("orderListId", orderListId)
                .add("clientOrderId", clientOrderId)
                .add("transactTime", transactTime)
                .toString();
    }
}
