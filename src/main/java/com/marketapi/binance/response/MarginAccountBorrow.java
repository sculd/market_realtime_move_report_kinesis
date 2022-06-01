package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class MarginAccountBorrow {
    public long tranId;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountBorrow.class)
                .add("tranId", tranId)
                .toString();
    }
}
