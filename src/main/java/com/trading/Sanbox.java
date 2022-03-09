package com.trading;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.marketapi.binance.response.SystemStatus;
import com.marketapi.binance.response.AccountSnapshot;
import com.marketapi.binance.response.Pair;
import com.marketapi.binance.MarginPair;
import com.google.gson.Gson;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sanbox {
    private static final Logger logger = LoggerFactory.getLogger(Sanbox.class);

    public static void main(String... args) {
        SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
        LinkedHashMap<String,Object> parameters;
        String result;

        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol","BTCUSDT");
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", 0.01);
        parameters.put("price", 9500);
        result = client.createTrade().testNewOrder(parameters);
        System.out.println(result);

        try {
            parameters.put("symbol","dummy");
            result = client.createTrade().testNewOrder(parameters);
            logger.info(result);
        }
        catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        }
        catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("type", "SPOT");
        result = client.createWallet().accountSnapshot(parameters);
        System.out.println(result);
        AccountSnapshot accountSnapshot = gson.fromJson(result, AccountSnapshot.class);
        System.out.println(accountSnapshot);

        result = client.createWallet().systemStatus();
        System.out.println(result);
        SystemStatus systemStatus = gson.fromJson(result, SystemStatus.class);
        System.out.println(systemStatus);

        result = client.createMargin().allPairs();
        System.out.println(result);
        List<Pair> pairs = gson.fromJson(result, Pair.getListType());
        System.out.println(pairs);

        System.out.println(MarginPair.getAllPairs(true));

        try {
            parameters.put("symbol","dummy");
            parameters.put("side", "SELL");
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", 0.01);
            parameters.put("price", 9500);
            result = client.createMargin().newOrder(parameters);
            logger.info(result);
        }
        catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        }
        catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        System.out.println("done");
    }
}
