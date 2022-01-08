package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.OrderbookSlidingWindow;
import com.marketsignal.orderbook.Orderbook;
import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.lang3.ArrayUtils;

public class OrderFlowImbalance {

    static final double OUTLIER_THRESHOLD = 1.5;

    @Builder
    static public class Parameter {
        // the whole sample duration
        public Duration windowDuration;
        // flow summation window for each point
        public Duration aggregationDuration;
        // how often each point is calculated
        public Duration sampleDuration;

        public Parameter(Duration windowDuration, Duration aggregationDuration, Duration sampleDuration) {
            this.windowDuration = windowDuration;
            this.aggregationDuration = aggregationDuration;
            this.sampleDuration = sampleDuration;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderFlowImbalance.Parameter.class)
                    .add("windowDuration", windowDuration)
                    .add("aggregationDuration", aggregationDuration)
                    .add("sampleDuration", sampleDuration)
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
        public double recentOrderFlowImbalance;
        public double orderFlowImbalanceMedian;
        public double orderFlowImbalanceAverage;
        public double orderFlowImbalanceStandardDeviationWithoutOutliers;
        public double recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers;
        public double recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers;
        public Parameter parameter;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(OrderFlowImbalance.Parameter.class)
                    .add("market", market)
                    .add("symbol", symbol)
                    .add("epochSeconds", epochSeconds)
                    .add("bidPrice", bidPrice)
                    .add("askPrice", askPrice)
                    .add("recentOrderFlowImbalance", recentOrderFlowImbalance)
                    .add("orderFlowImbalanceMedian", orderFlowImbalanceMedian)
                    .add("orderFlowImbalanceAverage", orderFlowImbalanceAverage)
                    .add("orderFlowImbalanceStandardDeviationWithoutOutliers", orderFlowImbalanceStandardDeviationWithoutOutliers)
                    .add("recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers", recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers)
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

    static private double[] excludeOutliers(double[] imbalancesArray, double median, double standardDeviation) {
        List<Double> excluded = new ArrayList<>();
        for (double v : imbalancesArray) {
            if (Math.abs(v - median) / standardDeviation > OUTLIER_THRESHOLD) {
                continue;
            }
            excluded.add(v);
        }
        return ArrayUtils.toPrimitive(excluded.stream().toArray(Double[] ::new));
    }

    static public Analysis analyze(OrderbookSlidingWindow orderbooksSlidingWindow, Parameter parameter) {
        List<Orderbook> orderbooks = sampleOrderbooks(orderbooksSlidingWindow.window, parameter.sampleDuration.toSeconds());
        List<Orderbook> windowedOrderbooks = filterOutdatedOrderbooks(orderbooks, parameter.windowDuration);
        if (windowedOrderbooks.isEmpty()) {
            return Analysis.builder().build();
        }

        List<Double> imbalances = new ArrayList<>();
        ArrayDeque<Orderbook> aggregationWindow = new ArrayDeque<>();
        for (int i = 0; i < windowedOrderbooks.size(); i++) {
            aggregationWindow.add(windowedOrderbooks.get(i));
            while (!aggregationWindow.isEmpty() && aggregationWindow.getLast().epochSeconds - aggregationWindow.getFirst().epochSeconds > parameter.aggregationDuration.toSeconds()) {
                aggregationWindow.removeFirst();
            }
            if (aggregationWindow.size() < 2) {
                continue;
            }
            Iterator<Orderbook> oit = aggregationWindow.iterator();
            Orderbook bookFormer = null;
            Orderbook bookLatter = null;
            double imbalance = 0;
            while (oit.hasNext()) {
                bookFormer = bookLatter;
                bookLatter = oit.next();
                if (bookFormer != null) {
                    imbalance -= OrderFlow.getAskOrderFlow(bookFormer, bookLatter);
                    imbalance += OrderFlow.getBidOrderFlow(bookFormer, bookLatter);
                }
            }
            imbalances.add(imbalance);
        }

        double bidPrice = 0;
        double askPrice = 0;
        if (!windowedOrderbooks.isEmpty()) {
            Orderbook.Quote topBid = windowedOrderbooks.get(windowedOrderbooks.size()-1).getTopBid();
            Orderbook.Quote topAsk = windowedOrderbooks.get(windowedOrderbooks.size()-1).getTopAsk();
            if (topBid != null) {
                bidPrice = topBid.price;
            }
            if (topAsk != null) {
                askPrice = topAsk.price;
            }
        }

        StandardDeviation sd = new StandardDeviation();
        Mean mean = new Mean();
        Median median = new Median();
        double[] imbalancesArray = ArrayUtils.toPrimitive(imbalances.stream().toArray(Double[] ::new));
        double recentOrderFlowImbalance = 0;
        double orderFlowImbalanceMedian = 0;
        double orderFlowImbalanceAverage = 0;
        double orderFlowImbalanceStandardDeviation = 0;
        double recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers = 0;
        double recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers = 0;
        if (!imbalances.isEmpty()) {
            recentOrderFlowImbalance = imbalances.get(imbalances.size()-1);
            orderFlowImbalanceMedian = median.evaluate(imbalancesArray);
            orderFlowImbalanceAverage = mean.evaluate(imbalancesArray);
            orderFlowImbalanceStandardDeviation = sd.evaluate(imbalancesArray);
        }
        double[] imbalancesWithoutOutliers = excludeOutliers(imbalancesArray, orderFlowImbalanceMedian, orderFlowImbalanceStandardDeviation);
        double orderFlowImbalanceStandardDeviationWithoutOutliers = 0;
        if (imbalancesWithoutOutliers.length > 0) {
            orderFlowImbalanceMedian = median.evaluate(imbalancesWithoutOutliers);
            orderFlowImbalanceAverage = mean.evaluate(imbalancesWithoutOutliers);
            orderFlowImbalanceStandardDeviationWithoutOutliers = sd.evaluate(imbalancesWithoutOutliers);
        }
        if (orderFlowImbalanceStandardDeviationWithoutOutliers != 0) {
            recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers = (recentOrderFlowImbalance - orderFlowImbalanceMedian) / orderFlowImbalanceStandardDeviationWithoutOutliers;
            recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers = (recentOrderFlowImbalance - orderFlowImbalanceAverage) / orderFlowImbalanceStandardDeviationWithoutOutliers;
        }

        Analysis ret = Analysis.builder()
                .market(orderbooksSlidingWindow.market)
                .symbol(orderbooksSlidingWindow.symbol)
                .epochSeconds(orderbooksSlidingWindow.getLatestEpochSeconds())
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .recentOrderFlowImbalance(recentOrderFlowImbalance)
                .orderFlowImbalanceMedian(orderFlowImbalanceMedian)
                .orderFlowImbalanceAverage(orderFlowImbalanceAverage)
                .orderFlowImbalanceStandardDeviationWithoutOutliers(orderFlowImbalanceStandardDeviationWithoutOutliers)
                .recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers(recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers)
                .recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers(recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers)
                .parameter(parameter)
                .build();
        return ret;
    }
}
