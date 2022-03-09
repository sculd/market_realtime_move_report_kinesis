package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class QueryCrossMarginPair {
    public long id;
    public String symbol;
    public String base;
    public String quote;
    public boolean isMarginTrade;
    public boolean isBuyAllowed;
    public boolean isSellAllowed;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NewOrder.class)
                .add("id", id)
                .add("symbol", symbol)
                .add("base", base)
                .add("quote", quote)
                .add("isMarginTrade", isMarginTrade)
                .add("isBuyAllowed", isBuyAllowed)
                .add("isSellAllowed", isSellAllowed)
                .toString();
    }
}
