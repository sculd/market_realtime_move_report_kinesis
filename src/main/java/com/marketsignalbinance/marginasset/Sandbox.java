package com.marketsignalbinance.marginasset;

import com.marketsignal.marginasset.MarginAsset;

public class Sandbox {
    public static void main(String... args) {
        MarginAssetFactoryBinance m = new MarginAssetFactoryBinance();
        MarginAsset ma = m.create("binance");
        System.out.println(String.format("%s: %b", "BTC", ma.isMarginAsset("BTC")));
        System.out.println(String.format("%s: %b", "DUMMY", ma.isMarginAsset("DUMMY")));
    }
}
