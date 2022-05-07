package com.marketsignal.orderbook;

import com.google.common.base.MoreObjects;
import com.google.gson.*;
import com.marketsignal.util.Time;
import lombok.Builder;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;


public class Orderbook {
    private static final Gson GSON = new Gson();

    @Builder
    public static class Quote {
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

    public class Quotes {
        public List<Quote> quotes = new ArrayList();

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

    public long anchorEpochSeconds(long intervalSeconds) {
        return epochSeconds - (epochSeconds % intervalSeconds);
    }

    public Quote getBid(int depth) {
        if (bids.quotes.size() < depth + 1) {
            return null;
        }
        return bids.quotes.get(depth);
    }

    public Quote getAsk(int depth) {
        if (asks.quotes.size() < depth + 1) {
            return null;
        }
        return asks.quotes.get(depth);
    }

    public Quote getTopBid() {
        return getBid(0);
    }

    public Quote getTopAsk() {
        return getAsk(0);
    }

    public double getTopBidPrice() {
        Quote top = getTopBid();
        if (top == null) {
            return 0;
        }
        return top.price;
    }

    public double getTopAskPrice() {
        Quote top = getTopAsk();
        if (top == null) {
            return 0;
        }
        return top.price;
    }

    public double getMidPrice() {
        return (getTopAskPrice() + getTopBidPrice()) / 2.0;
    }

    public double getSpread() {
        return getTopAskPrice() - getTopBidPrice();
    }

    public double getSpreadToMidRatio() {
        double mid = getMidPrice();
        if (mid == 0) {
            return 0.;
        }
        return getSpread() / mid;
    }

    public double getCummulativeBidVolume(int depth) {
        return bids.quotes.subList(0, depth + 1).stream().mapToDouble(q -> q.volume).sum();
    }

    public double getCummulativeAskVolume(int depth) {
        return asks.quotes.subList(0, depth + 1).stream().mapToDouble(q -> q.volume).sum();
    }

    public double getCummulativeBidVolumeAbovePrice(double price) {
        List<Double> volumes = new ArrayList<>();
        for (Quote q : bids.quotes) {
            if (q.price < price) {
                break;
            }
            volumes.add(q.volume);
        }
        return volumes.stream().mapToDouble(p -> p).sum();
    }

    public double getCummulativeAskVolumeBelowPrice(double price) {
        List<Double> volumes = new ArrayList<>();
        for (Quote q : asks.quotes) {
            if (q.price > price) {
                break;
            }
            volumes.add(q.volume);
        }
        return volumes.stream().mapToDouble(p -> p).sum();
    }
}
