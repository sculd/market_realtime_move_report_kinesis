package com.marketsignalbinance.orderbook;

import com.google.gson.Gson;
import com.marketapi.binance.response.Depth;
import com.marketsignal.orderbook.Orderbook;
import com.marketsignal.orderbook.OrderbookFactory;
import com.marketsignal.orderbook.OrderbookFactoryTrivial;
import com.tradingbinance.state.BinanceUtil;
import com.tradingchangesanomalyreversal.BackTestBinance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

public class OrderbookFactoryBinance implements OrderbookFactory {
    private static final Logger log = LoggerFactory.getLogger(BackTestBinance.class);

    Gson gson = new Gson();

    public Orderbook create(String market, String symbol, double currentPrice) {
        if (!market.equals("binance")) {
            log.error("market {} for binance does not match", market);
            return new OrderbookFactoryTrivial().create(market, symbol, currentPrice);
        }

        LinkedHashMap<String,Object> parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol", symbol);
        parameters.put("limit", 5);
        String result = BinanceUtil.client.createMarket().depth(parameters);
        Depth depth = gson.fromJson(result, Depth.class);
        Orderbook orderbook = new Orderbook(market, symbol, java.time.Instant.now().getEpochSecond());

        for (List<String> quote : depth.asks) {
            orderbook.asks.quotes.add(
                    Orderbook.Quote.builder()
                            .price(Double.valueOf(quote.get(0)))
                            .volume(Double.valueOf(quote.get(1)))
                            .build()
            );
        }

        for (List<String> quote : depth.bids) {
            orderbook.bids.quotes.add(
                    Orderbook.Quote.builder()
                            .price(Double.valueOf(quote.get(0)))
                            .volume(Double.valueOf(quote.get(1)))
                            .build()
            );
        }
        return orderbook;
    }
}
