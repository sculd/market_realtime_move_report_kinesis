package com.marketsignal.util;

import org.decimal4j.util.DoubleRounder;

public class Format {
    static public String ratioToPercent(double ratio) {
        return String.format("%s%%", String.valueOf(DoubleRounder.round(ratio * 100, 1)));
    }

    static public double truncatePrice(double price) {
        return DoubleRounder.round(price, 1);
    }
}
