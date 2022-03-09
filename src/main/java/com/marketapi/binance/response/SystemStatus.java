package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

public class SystemStatus {
    public int status;
    public String msg;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SystemStatus.class)
                .add("status", status)
                .add("msg", msg)
                .toString();
    }
}
