package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

import java.util.List;

public class ExchangeInformation {
    public String timezone;
    public long serverTime;

    public class Filter {        public String filterType;

        /*
        {
          "filterType": "PRICE_FILTER",
          "minPrice": "0.00010000",
          "maxPrice": "1000.00000000",
          "tickSize": "0.00010000"
        },
         */
        public String minPrice;
        public String maxPrice;
        public String tickSize;
        /*
        {
          "filterType": "LOT_SIZE",
          "minQty": "0.10000000",
          "maxQty": "900000.00000000",
          "stepSize": "0.10000000"
        },
         */
        public String minQty;
        public String maxQty;
        public String stepSize;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Filter.class)
                    .add("filterType", filterType)
                    .add("minPrice", minPrice)
                    .add("maxPrice", maxPrice)
                    .add("tickSize", tickSize)

                    .add("minQty", minQty)
                    .add("maxQty", maxQty)
                    .add("stepSize", stepSize)
                    .toString();
        }
    }

    public class Symbol {
        public String symbol;
        public int baseAssetPrecision;
        public int quoteAssetPrecision;
        public int quotePrecision;
        public boolean isSpotTradingAllowed;
        public boolean isMarginTradingAllowed;
        public List<Filter> filters;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Symbol.class)
                    .add("symbol", symbol)
                    .add("baseAssetPrecision", baseAssetPrecision)
                    .add("quoteAssetPrecision", quoteAssetPrecision)
                    .add("quotePrecision", quotePrecision)
                    .add("isSpotTradingAllowed", isSpotTradingAllowed)
                    .add("isMarginTradingAllowed", isMarginTradingAllowed)
                    .add("filters", filters)
                    .toString();
        }
    }
    public List<Symbol> symbols;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MarginAccountBorrow.class)
                .add("timezone", timezone)
                .add("serverTime", serverTime)
                .add("symbols", symbols)
                .toString();
    }

    Filter getFilter(String symbol, String filterType) {
        for (Symbol s : symbols) {
            if (s.symbol.equals(symbol)) {
                for (Filter filter: s.filters) {
                    if (filter.filterType.equals(filterType)) {
                        return filter;
                    }
                }
                break;
            }
        }
        return null;
    }

    double getPriceTickSize(String symbol) {
        Filter filter = getFilter(symbol, "PRICE_FILTER");
        return Double.valueOf(filter.tickSize);
    }

    double getLotStepSize(String symbol) {
        Filter filter = getFilter(symbol, "LOT_SIZE");
        return Double.valueOf(filter.stepSize);
    }

    public double quantifyByStepSize(String symbol, double quantity) {
        double stepSize = getLotStepSize(symbol);
        return (long)(quantity / stepSize) * stepSize;
    }
}
