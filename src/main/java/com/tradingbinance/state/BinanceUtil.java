package com.tradingbinance.state;

import com.binance.connector.client.impl.SpotClientImpl;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.marketapi.binance.response.ExchangeInformation;

import java.io.IOException;

public class BinanceUtil {
    static final String EXCHANGE_INFO_URL_FORMAT = "https://api.binance.com/api/v3/exchangeInfo?symbol=%s";

    static public SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
    static Gson gson = new Gson();

    static public ExchangeInformation getExchangeInfo(String symbol) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format(EXCHANGE_INFO_URL_FORMAT, symbol))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();

            ExchangeInformation exchangeInformation = gson.fromJson(responseStr, ExchangeInformation.class);
            return exchangeInformation;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
