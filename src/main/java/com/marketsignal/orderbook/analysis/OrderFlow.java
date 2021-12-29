package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.Orderbook;

public class OrderFlow {
    static public double getBidOrderFlow(Orderbook former, Orderbook later) {
        Orderbook.Quote formerBid = former.getTopBid();
        Orderbook.Quote laterBid = later.getTopBid();
        if (laterBid == null && formerBid == null) {
            return 0.0;
        }
        if (laterBid == null) {
            return -formerBid.volume;
        }
        if (formerBid == null) {
            return laterBid.volume;
        }
        if (laterBid.price == formerBid.price) {
            return laterBid.volume - formerBid.volume;
        }
        if (laterBid.price > formerBid.price) {
            return later.getCummulativeBidVolumeAbovePrice(formerBid.price);
        }
        if (laterBid.price < formerBid.price) {
            return -former.getCummulativeBidVolumeAbovePrice(laterBid.price);
        }
        return 0.0;
    }

    static public double getAskOrderFlow(Orderbook former, Orderbook later) {
        Orderbook.Quote formerAsk = former.getTopAsk();
        Orderbook.Quote laterAsk = later.getTopAsk();
        if (laterAsk == null && formerAsk == null) {
            return 0.0;
        }
        if (laterAsk == null) {
            return -formerAsk.volume;
        }
        if (formerAsk == null) {
            return laterAsk.volume;
        }
        if (laterAsk.price == formerAsk.price ) {
            return laterAsk.volume - formerAsk.volume;
        }
        if (laterAsk.price > formerAsk.price) {
            return -former.getCummulativeAskVolumeBelowPrice(laterAsk.price);
        }
        if (laterAsk.price < formerAsk.price) {
            return later.getCummulativeAskVolumeBelowPrice(formerAsk.price);
        }
        return 0.0;
    }
}
