package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Pair {
    public String id;
    public String symbol;
    public String base;
    public String quote;
    public boolean isMarginTrade;
    public boolean isBuyAllowed;
    public boolean isSellAllowed;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(AccountSnapshot.Balance.class)
                .add("id", id)
                .add("symbol", symbol)
                .add("base", base)
                .add("quote", quote)
                .add("isMarginTrade", isMarginTrade)
                .add("isBuyAllowed", isBuyAllowed)
                .add("isSellAllowed", isSellAllowed)
                .toString();
    }

    static Type pairsType = new TypeToken<List<Pair>>() {}.getType();
    public static Type getListType() {
        return pairsType;
    }
}

