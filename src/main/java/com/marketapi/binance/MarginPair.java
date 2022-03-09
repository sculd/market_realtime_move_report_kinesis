package com.marketapi.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.marketapi.binance.response.Pair;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarginPair {
    private static final Logger logger = LoggerFactory.getLogger(MarginPair.class);

    static Gson gson = new Gson(); // Or use new GsonBuilder().create();

    public static List<Pair> getAllPairs(boolean isMarginTradeOnly) {
        SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));

        List<Pair> allPairs = new ArrayList<>();
        try {
            String result = client.createMargin().allPairs();
            allPairs = gson.fromJson(result, Pair.getListType());
            if (isMarginTradeOnly) {
                allPairs = allPairs.stream().filter(p -> p.isMarginTrade).collect(Collectors.toList());
            }
        }
        catch (Exception e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        }

        return allPairs;
    }
}
