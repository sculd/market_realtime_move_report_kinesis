package com.marketsignal.orderbook.analysis;

import com.google.common.base.MoreObjects;
import com.marketsignal.orderbook.Orderbook;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

public class LiquidityImbalance {
    @Builder
    static public class Parameter {
        public Parameter() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(LiquidityImbalance.Parameter.class)
                    .toString();
        }
    }

    @Builder
    static public class Analysis {
        public String market;
        public String symbol;
        public long epochSeconds;
        public double bidPrice;
        public double askPrice;
        public double liquidityImbalance5;
        public double liquidityImbalance10;
        public double liquidityImbalance15;
        public double liquidityImbalance20;
        public LiquidityImbalance.Parameter parameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderFlowImbalance.Parameter.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("epochSeconds", epochSeconds)
                    .add("bidPrice", bidPrice)
                    .add("askPrice", askPrice)
                    .add("liquidityImbalance5", liquidityImbalance5)
                    .add("liquidityImbalance10", liquidityImbalance10)
                    .add("liquidityImbalance15", liquidityImbalance15)
                    .add("liquidityImbalance20", liquidityImbalance20)
                    .add("parameter", parameter.toString())
                    .toString();
        }
    }

    static double getLiquidityImbalance(Orderbook orderbook, final int depth) {
        List<Double> volumes = new ArrayList<>();
        for (int d = 0; d < depth; d++) {
            Orderbook.Quote bid = orderbook.getBid(d);
            if (bid != null) {
                volumes.add(bid.volume);
            }
            Orderbook.Quote ask = orderbook.getAsk(d);
            if (ask != null) {
                volumes.add(ask.volume);
            }
        }
        double maxVolume = volumes.stream()
                .mapToDouble(d -> d)
                .max().orElse(0.0);

        double bidsLiquidity = 0;
        for (int d = 0; d < depth; d++) {
            Orderbook.Quote bid = orderbook.getBid(d);
            if (bid == null) {
                continue;
            }
            bidsLiquidity += bid.volume;
        }

        double asksLiquidity = 0;
        for (int d = 0; d < depth; d++) {
            Orderbook.Quote ask = orderbook.getAsk(d);
            if (ask == null) {
                continue;
            }
            asksLiquidity += ask.volume;
        }

        return Math.log(bidsLiquidity / asksLiquidity);
    }

    static public Analysis analyze(Orderbook orderbook, Parameter parameter) {
        Analysis ret = Analysis.builder()
                .market(orderbook.market)
                .symbol(orderbook.symbol)
                .epochSeconds(orderbook.epochSeconds)
                .bidPrice(orderbook.getTopBidPrice())
                .askPrice(orderbook.getTopAskPrice())
                .liquidityImbalance5(getLiquidityImbalance(orderbook, 5))
                .liquidityImbalance10(getLiquidityImbalance(orderbook, 10))
                .liquidityImbalance15(getLiquidityImbalance(orderbook, 15))
                .liquidityImbalance20(getLiquidityImbalance(orderbook, 20))
                .parameter(parameter)
                .build();
        return ret;
    }
}
