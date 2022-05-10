package com.marketsignalbinance.marginasset;

import com.marketsignal.marginasset.MarginAsset;
import com.marketsignal.marginasset.MarginAssetFactory;

public class MarginAssetFactoryBinance implements MarginAssetFactory {
    public MarginAsset create(String market) {
        return new MarginAssetBinance();
    }
}
