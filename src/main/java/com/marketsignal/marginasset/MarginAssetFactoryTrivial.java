package com.marketsignal.marginasset;

public class MarginAssetFactoryTrivial implements MarginAssetFactory {
    public MarginAsset create(String market) {
        return new MarginAssetTrivial();
    }
}
