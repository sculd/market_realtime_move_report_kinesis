package com.trading;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import java.util.LinkedHashMap;
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

        parameters = new LinkedHashMap<String,Object>();
        parameters.put("type", "SPOT");
        System.out.println(client.createWallet().accountSnapshot(parameters));

        System.out.println("done");
    }
}
