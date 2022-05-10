package com.marketsignal.marginasset;

public class MarginAssetTrivial implements MarginAsset {
    public boolean isMarginAsset(String symbol) {
        return true;
    }
}
