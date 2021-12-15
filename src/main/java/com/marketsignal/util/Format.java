package com.marketsignal.util;

import org.decimal4j.util.DoubleRounder;

public class Format {
    static public double ratioToPercent(double ratio) {
        return DoubleRounder.round(ratio * 100, 1);
    }

    static public double truncatePrice(double price) {
        return DoubleRounder.round(price, 1);
    }
}
