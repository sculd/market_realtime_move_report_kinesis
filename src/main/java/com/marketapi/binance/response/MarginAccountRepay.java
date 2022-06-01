package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class MarginAccountRepay {
    public long tranId;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountRepay.class)
                .add("tranId", tranId)
                .toString();
    }
}
