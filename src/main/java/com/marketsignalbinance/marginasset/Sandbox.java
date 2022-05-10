package com.marketsignalbinance.marginasset;

import com.marketsignal.marginasset.MarginAsset;

public class Sandbox {
    public static void main(String... args) {
        MarginAsset ma = new MarginAssetBinance();
        System.out.println(String.format("%s: %b", "BTC", ma.isMarginAsset("BTC")));
        System.out.println(String.format("%s: %b", "DUMMY", ma.isMarginAsset("DUMMY")));
    }
}
