package com.tradingbinance.state;

import com.binance.connector.client.impl.SpotClientImpl;

public class BinanceUtil {
    static public SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
}
