package com.marketsignal.timeseries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class BarWithTime {
    public Bar bar;
    public long epochSeconds;

    private static final Gson GSON = new Gson();

    public BarWithTime(Bar bar, long epochSeconds) {
        this.bar = new Bar(bar);
        this.epochSeconds = epochSeconds;
    }

    public static BarWithTime fromBytes(byte[] bytes) {
        try {
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = GSON.fromJson(jsonStr, JsonObject.class);
            final String market = jsonObject.getAsJsonPrimitive("market").getAsString();
            final String symbol = jsonObject.getAsJsonPrimitive("symbol").getAsString();
            final double open = jsonObject.getAsJsonPrimitive("open").getAsDouble();
            final double high = jsonObject.getAsJsonPrimitive("high").getAsDouble();
            final double low = jsonObject.getAsJsonPrimitive("low").getAsDouble();
            final double close = jsonObject.getAsJsonPrimitive("close").getAsDouble();
            final double volume = jsonObject.getAsJsonPrimitive("volume").getAsDouble();
            final long epochSeconds = jsonObject.getAsJsonPrimitive("epoch_seconds").getAsLong();

            return new BarWithTime(new Bar(market, symbol, new OHLC(open, high, low, close), volume), epochSeconds);
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }
}
