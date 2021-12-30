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
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.lang3.ArrayUtils;

public class OrderFlowImbalance {

    static final double OUTLIER_THRESHOLD = 1.5;

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
        Median median = new Median();
        double[] imbalancesArray = ArrayUtils.toPrimitive(imbalances.stream().toArray(Double[] ::new));
        double recentOrderFlowImbalance = 0;
        if (!imbalances.isEmpty()) {
            recentOrderFlowImbalance = imbalances.get(imbalances.size()-1);
        }
        double orderFlowImbalanceMedian = median.evaluate(imbalancesArray);
        double orderFlowImbalanceAverage = mean.evaluate(imbalancesArray);
        double orderFlowImbalanceStandardDeviation = sd.evaluate(imbalancesArray);
        double[] imbalancesWithoutOutliers = excludeOutliers(imbalancesArray, orderFlowImbalanceMedian, orderFlowImbalanceStandardDeviation);
        double orderFlowImbalanceStandardDeviationWithoutOutliers = sd.evaluate(imbalancesWithoutOutliers);

        Analysis ret = Analysis.builder()
                .market(orderbooksSlidingWindow.market)
                .symbol(orderbooksSlidingWindow.symbol)
                .epochSeconds(orderbooksSlidingWindow.getLatestEpochSeconds())
                .recentOrderFlowImbalance(recentOrderFlowImbalance)
                .orderFlowImbalanceMedian(orderFlowImbalanceMedian)
                .orderFlowImbalanceAverage(orderFlowImbalanceAverage)
                .orderFlowImbalanceStandardDeviationWithoutOutliers(orderFlowImbalanceStandardDeviationWithoutOutliers)
                .recentOrderFlowImbalanceDeviationFromMedianToStandardDeviationWithoutOutliers((recentOrderFlowImbalance - orderFlowImbalanceMedian) / orderFlowImbalanceStandardDeviationWithoutOutliers)
                .recentOrderFlowImbalanceDeviationFromAverageToStandardDeviationWithoutOutliers((recentOrderFlowImbalance - orderFlowImbalanceAverage) / orderFlowImbalanceStandardDeviationWithoutOutliers)
                .parameter(parameter)
                .build();
        return ret;
    }
}
