package com.tradingchangesanomaly.util;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.trading.util.TradingInitParameter;
import com.tradingchangesanomaly.stream.ChangesAnomalyTradingStreamInitParameter;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ChangesAnomalyTradingInitParameter extends TradingInitParameter {
    @Builder.Default
    Gson gson = new Gson();
    ChangesAnomalyTradingStreamInitParameter changesAnomalyTradingStreamInitParameter;

    public String toJson() {
        return gson.toJson(changesAnomalyTradingStreamInitParameter);
    }

    public void initFromJson(String jsonStr) {
        changesAnomalyTradingStreamInitParameter = gson.fromJson(jsonStr, ChangesAnomalyTradingStreamInitParameter.class);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ChangesAnomalyTradingInitParameter.class)
                .add("changesAnomalyTradingStreamInitParameter", changesAnomalyTradingStreamInitParameter)
                .toString();
    }
}
