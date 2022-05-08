package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

import java.util.List;

public class Depth {
    public long lastUpdateId;

    public List<List<String>> bids;
    public List<List<String>> asks;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Depth.class)
                .add("lastUpdateId", lastUpdateId)
                .add("bids", bids)
                .add("asks", asks)
                .toString();
    }
}
