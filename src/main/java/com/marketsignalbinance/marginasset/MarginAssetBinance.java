package com.marketsignalbinance.marginasset;

import com.google.gson.Gson;
import com.marketapi.binance.response.MarginAssetResponse;
import com.marketsignal.marginasset.MarginAsset;
import com.tradingbinance.state.BinanceUtil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MarginAssetBinance implements MarginAsset {
    Gson gson = new Gson();
    LoadingCache<String, Boolean> cached;
    LoadingCache<String, String> cachedAPIResult;

    final String API_RESULT_KEY = "all";

    public MarginAssetBinance() {
        cached = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, Boolean>() {
                            public Boolean load(String pair) { // no checked exception
                                return queryIsMarginAsset(pair);
                            }
                        });
        cachedAPIResult = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, String>() {
                            public String load(String key) { // no checked exception
                                return requestAllAssets();
                            }
                        });
    }

    String requestAllAssets() {
        return BinanceUtil.client.createMargin().allAssets();
    }

    boolean queryIsMarginAsset(String pair) {
        String result = getAllMarginAssetsResponse();
        String[] tokens = pair.split("USD");
        String symbol = tokens[0];
        MarginAssetResponse[] marginAssets = gson.fromJson(result, MarginAssetResponse[].class);
        Set<String> assets = Arrays.stream(marginAssets).map(mr -> mr.assetName).collect(Collectors.toSet());
        return assets.contains(symbol);
    }

    String getAllMarginAssetsResponse() {
        try {
            return cachedAPIResult.get(API_RESULT_KEY);
        } catch (ExecutionException e) {
            // retry
            return requestAllAssets();
        }
    }

    public boolean isMarginAsset(String pair) {
        try {
            return cached.get(pair);
        } catch (ExecutionException e) {
            // retry
            return queryIsMarginAsset(pair);
        }
    }
}



















