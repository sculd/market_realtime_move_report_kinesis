package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.orderbook.Orderbook;
import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.lang3.ArrayUtils;

public class OrderFlowImbalance {

    @Builder
    static public class Parameter {
        public Duration flowDuration;
        public Duration sampleDuration;

        public Parameter(Duration flowDuration, Duration sampleDuration) {
            this.flowDuration = flowDuration;
            this.sampleDuration = sampleDuration;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderFlowImbalance.Parameter.class)
                    .add("flowDuration", flowDuration)
                    .add("sampleDuration", sampleDuration)
                    .toString();
        }
    }

    @Builder
    static public class Analysis {
        public String market;
        public String symbol;
        public long epochSeconds;
        public double recentOrderFlowImbalance;
        public double orderFlowImbalanceAverage;
        public double orderFlowImbalanceStandardDeviation;
        public Parameter parameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderFlowImbalance.Parameter.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("epochSeconds", epochSeconds)
                    .add("recentOrderFlowImbalance", recentOrderFlowImbalance)
                    .add("orderFlowImbalanceAverage", orderFlowImbalanceAverage)
                    .add("orderFlowImbalanceStandardDeviation", orderFlowImbalanceStandardDeviation)
                    .add("parameter", parameter.toString())
                    .toString();
        }
    }

    static private List<Orderbook> sampleOrderbooks(Collection<Orderbook> orderbooks, long intervalSeconds) {
        Set<Long> nameSet = new HashSet<>();
        return orderbooks.stream()
                .filter(ob -> nameSet.add(ob.anchorEpochSeconds(intervalSeconds)))
                .collect(Collectors.toList());
    }

    static private List<Orderbook> filterOutdatedOrderbooks(List<Orderbook> orderbooks, Duration duration) {
        if (orderbooks.isEmpty()) {
            return new ArrayList<>();
        }
        long latestEpochSeconds = orderbooks.get(orderbooks.size() - 1).epochSeconds;
        return orderbooks.stream()
                .filter(ob -> ob.epochSeconds >= latestEpochSeconds - duration.toSeconds())
                .collect(Collectors.toList());
    }

    static public Analysis analyze(OrderbookSlidingWindow orderbooksSlidingWindow, Parameter parameter) {
        List<Orderbook> orderbooks = sampleOrderbooks(orderbooksSlidingWindow.window, parameter.sampleDuration.toSeconds());
        List<Orderbook> windowedOrderbooks = filterOutdatedOrderbooks(orderbooks, parameter.flowDuration);
        if (windowedOrderbooks.isEmpty()) {
            return Analysis.builder().build();
        }

        List<Double> imbalances = new ArrayList<>();
        for (int i = 1; i < windowedOrderbooks.size(); i++) {
            double imbalance = 0;
            imbalance -= OrderFlow.getAskOrderFlow(windowedOrderbooks.get(i-1), windowedOrderbooks.get(i));
            imbalance += OrderFlow.getBidOrderFlow(windowedOrderbooks.get(i-1), windowedOrderbooks.get(i));
            imbalances.add(imbalance);
        }

        StandardDeviation sd = new StandardDeviation();
        Mean mean = new Mean();
        double[] imbalancesArray = ArrayUtils.toPrimitive(imbalances.stream().toArray(Double[] ::new));
        Analysis ret = Analysis.builder()
                .market(orderbooksSlidingWindow.market)
                .symbol(orderbooksSlidingWindow.symbol)
                .epochSeconds(orderbooksSlidingWindow.getLatestEpochSeconds())
                .recentOrderFlowImbalance(imbalances.get(imbalances.size()-1))
                .orderFlowImbalanceAverage(mean.evaluate(imbalancesArray))
                .orderFlowImbalanceStandardDeviation(sd.evaluate(imbalancesArray))
                .build();
        return ret;
    }
}
