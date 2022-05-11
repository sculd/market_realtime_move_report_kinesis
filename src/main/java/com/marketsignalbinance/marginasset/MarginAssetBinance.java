package com.marketsignalbinance.marginasset;

import com.google.gson.Gson;
import com.marketapi.binance.response.MarginAssetResponse;
import com.marketsignal.marginasset.MarginAsset;
import com.tradingbinance.state.BinanceUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MarginAssetBinance implements MarginAsset {
    Gson gson = new Gson();

    public boolean isMarginAsset(String pair) {
        String[] tokens = pair.split("USD");
        String symbol = tokens[0];
        String result = BinanceUtil.client.createMargin().allAssets();
        MarginAssetResponse[] marginAssets = gson.fromJson(result, MarginAssetResponse[].class);
        Set<String> assets = Arrays.stream(marginAssets).map(mr -> mr.assetName).collect(Collectors.toSet());
        return assets.contains(symbol);
    }
}
