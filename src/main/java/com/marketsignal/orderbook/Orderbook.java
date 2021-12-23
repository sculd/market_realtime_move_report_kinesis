package com.marketsignal.orderbook;

import com.google.common.base.MoreObjects;
import com.google.gson.*;
import com.marketsignal.util.Time;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;


public class Orderbook {
    private static final Gson GSON = new Gson();

    static class Quote {
        public double price;
        public double volume;

        public Quote(double price, double volume) {
            this.price = price;
            this.volume = volume;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Quote.class)
                    .add("price", price)
                    .add("volume", volume)
                    .toString();
        }
    }

    class Quotes {
        List<Quote> quotes = new ArrayList();

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Quotes.class)
                    .add("quotes", quotes)
                    .toString();
        }
    }

    public String market;
    public String symbol;
    public long epochSeconds;
    public Quotes bids = new Quotes();
    public Quotes asks = new Quotes();

    public Orderbook(String market, String symbol, long epochSeconds) {
        this.market = market;
        this.symbol = symbol;
        this.epochSeconds = epochSeconds;
    }

    public static Orderbook fromBytes(byte[] bytes) {
        try {
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = GSON.fromJson(jsonStr, JsonObject.class);
            final String market = jsonObject.getAsJsonPrimitive("market").getAsString();
            final String symbol = jsonObject.getAsJsonPrimitive("symbol").getAsString();
            final long epochSeconds = jsonObject.getAsJsonPrimitive("epoch_seconds").getAsLong();
            Orderbook orderbook = new Orderbook(market, symbol, epochSeconds);

            JsonArray bids = jsonObject.getAsJsonArray("bids");
            Iterator<JsonElement> bidsIter = bids.iterator();
            while (bidsIter.hasNext()) {
                JsonElement bid = bidsIter.next();
                JsonArray bidVal = bid.getAsJsonArray();
                double price = Double.parseDouble(bidVal.get(0).getAsString());
                double volume = Double.parseDouble(bidVal.get(1).getAsString());
                orderbook.bids.quotes.add(new Quote(price, volume));
            }

            JsonArray asks = jsonObject.getAsJsonArray("asks");
            Iterator<JsonElement> asksIter = asks.iterator();
            while (asksIter.hasNext()) {
                JsonElement ask = asksIter.next();
                JsonArray askVal = ask.getAsJsonArray();
                double price = Double.parseDouble(askVal.get(0).getAsString());
                double volume = Double.parseDouble(askVal.get(1).getAsString());
                orderbook.asks.quotes.add(new Quote(price, volume));
            }

            return orderbook;
        } catch (JsonSyntaxException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Orderbook.class)
                .add("market", market)
                .add("symbol", symbol)
                .add("bids", bids.toString())
                .add("asks", asks.toString())
                .add("datetime", Time.fromEpochSecondsToDateTimeStr(epochSeconds))
                .toString();
    }
}
