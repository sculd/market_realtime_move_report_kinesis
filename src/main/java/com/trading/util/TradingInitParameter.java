package com.trading.util;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class TradingInitParameter {
    abstract public String toJson();
    abstract public void initFromJson(String jsonStr);
}
