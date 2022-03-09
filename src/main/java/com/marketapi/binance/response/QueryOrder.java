package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class QueryOrder {
    public String symbol;
    public long orderId;
    public long orderListId;
    public String clientOrderId;
    public String price;
    public String origQty;
    public String executedQty;
    public String cummulativeQuoteQty;
    public String status;
    public String timeInForce;
    public String type;
    public String side;
    public String stopPrice;
    public String icebergQty;
    public long time;
    public long updateTime;
    public boolean isWorking;
    public String origQuoteOrderQty;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NewOrder.class)
                .add("symbol", symbol)
                .add("orderId", orderId)
                .add("orderListId", orderListId)
                .add("clientOrderId", clientOrderId)
                .add("price", price)
                .add("origQty", origQty)
                .add("executedQty", executedQty)
                .add("cummulativeQuoteQty", cummulativeQuoteQty)
                .add("status", status)
                .add("timeInForce", timeInForce)
                .add("type", type)
                .add("side", side)
                .add("stopPrice", stopPrice)
                .add("icebergQty", icebergQty)
                .add("time", time)
                .add("updateTime", updateTime)
                .add("isWorking", isWorking)
                .add("origQuoteOrderQty", origQuoteOrderQty)
                .toString();
    }
}