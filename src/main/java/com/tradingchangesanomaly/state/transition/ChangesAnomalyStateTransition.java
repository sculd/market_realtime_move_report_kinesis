package com.tradingchangesanomaly.state.transition;

import com.google.common.base.MoreObjects;
import com.marketsignal.marginasset.MarginAsset;
import com.marketsignal.orderbook.OrderbookFactory;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.analysis.Analyses;
import com.marketsignal.timeseries.analysis.changes.Changes;
import com.marketsignal.timeseries.analysis.volatility.Volatility;
import com.trading.performance.ClosedTrade;
import com.trading.state.Common;
import com.trading.state.States;
import com.trading.state.transition.StateTransition;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangesAnomalyStateTransition extends StateTransition {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyStateTransition.class);

    OrderbookFactory orderbookFactory;
    MarginAsset marginAsset;

    @Builder
    static public class TransitionInitParameter {
        public double maxJumpThreshold;
        public double minDropThreshold;
        public Duration changeAnalysisWindow;
        public enum TriggerAnomalyType {
            JUMP,
            DROP,
            JUMP_OR_DROP;

            private static final Map<String, TriggerAnomalyType> ENUM_MAP = Stream.of(TriggerAnomalyType.values())
                    .collect(Collectors.toMap(Enum::name, Function.identity()));

            public static TriggerAnomalyType of(final String name) {
                return ENUM_MAP.getOrDefault(name, JUMP_OR_DROP);
            }
        }
        public TriggerAnomalyType triggerAnomalyType;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TransitionInitParameter.class)
                    .add("maxJumpThreshold", maxJumpThreshold)
                    .add("minDropThreshold", minDropThreshold)
                    .add("changeAnalysisWindow", changeAnalysisWindow)
                    .add("triggerAnomalyType", triggerAnomalyType)
                    .toString();
        }

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add("maxJumpThreshold");
            headers.add("minDropThreshold");
            headers.add("changeAnalysisWindow");
            headers.add("triggerAnomalyType");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%f", maxJumpThreshold));
            columns.add(String.format("%f", minDropThreshold));
            columns.add(String.format("%d", changeAnalysisWindow.toMinutes()));
            columns.add(String.format("%s", triggerAnomalyType));
            return String.join(",", columns);
        }

        static public TransitionInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            return TransitionInitParameter.builder()
                    .maxJumpThreshold(Double.parseDouble(columns[0]))
                    .minDropThreshold(Double.parseDouble(columns[1]))
                    .changeAnalysisWindow(Duration.ofMinutes(Integer.parseInt(columns[2])))
                    .triggerAnomalyType(TriggerAnomalyType.of(columns[3]))
                    .build();
        }
    }
    public TransitionInitParameter initParameter;

    public ChangesAnomalyStateTransition(String market, String symbol,
                                         OrderbookFactory orderbookFactory,
                                         MarginAsset marginAsset,
                                         TransitionInitParameter initParameter) {
        super(market, symbol);
        this.orderbookFactory = orderbookFactory;
        this.marginAsset = marginAsset;
        this.initParameter = initParameter;
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis, boolean isMarginAsset) {
        // to be implemented in following / reversal trades.
        return  StateTransitionFollowUp.HALT_TRANSITION;
    }

    static public class HandleStateResult {
        public ClosedTrade closedTrade;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(HandleStateResult.class)
                    .add("closedTrade", closedTrade)
                    .toString();
        }
    }

    public HandleStateResult handleState(States state, BarWithTimeSlidingWindow barWithTimeSlidingWindow) {
        Changes.AnalyzeResult changeAnalysis = Changes.analyze(barWithTimeSlidingWindow, Changes.AnalyzeParameter.builder()
                .windowSize(initParameter.changeAnalysisWindow)
                .build());
        Volatility.AnalyzeResult volatilityAnalysis = Volatility.analyze(barWithTimeSlidingWindow, Volatility.AnalyzeParameter.builder()
                .windowSizes(List.of(initParameter.changeAnalysisWindow, Duration.ofMinutes(initParameter.changeAnalysisWindow.toMinutes() * 2)))
                .build());
        Analyses analyses = new Analyses();
        analyses.analysisList.add(changeAnalysis);
        analyses.analysisList.add(volatilityAnalysis);
        HandleStateResult handleStateResult = new HandleStateResult();
        StateTransitionFollowUp stateTransitionFollowUp = StateTransitionFollowUp.CONTINUE_TRANSITION;
        States.StateType initStateType = state.stateType;
        while (stateTransitionFollowUp == StateTransitionFollowUp.CONTINUE_TRANSITION) {
            switch (state.stateType) {
                case IDLE:
                    // seek price, spread
                    stateTransitionFollowUp = planEnter(state, changeAnalysis, marginAsset.isMarginAsset(this.symbol));
                    break;
                case ENTER_PLAN:
                    // adjust enter seek target
                    state.enterPlan.onPriceUpdate(changeAnalysis.priceAtAnalysis);
                    // check if the plan is triggered
                    stateTransitionFollowUp = handleEnterPlanState(
                            state,
                            Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build(),
                            orderbookFactory.create(this.market, this.symbol, changeAnalysis.priceAtAnalysis));
                    break;
                case ENTER:
                    // execute the plan
                    stateTransitionFollowUp = handleEnterState(
                            state,
                            Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build(),
                            analyses);
                    break;
                case ENTER_ORDER_IN_PROGRESS:
                    // check if the execution is complete
                    // plan exit
                    stateTransitionFollowUp = handleEnterInProgressState(state);
                    break;
                case IN_POSITION:
                    // adjust exit seek target
                    state.exitPlan.stopLossPlan.onPriceUpdate(changeAnalysis.priceAtAnalysis);
                    stateTransitionFollowUp = handlePositionState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build());
                    break;
                case EXIT:
                    stateTransitionFollowUp = handleExitState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build(), analyses);
                    break;
                case EXIT_ORDER_IN_PROGRESS:
                    stateTransitionFollowUp = handleExitInProgressState(state);
                    break;
                case TRADE_CLOSED:
                    stateTransitionFollowUp = handleTradeClosed(state);
                    handleStateResult.closedTrade = state.closedTrade;
                    break;
            }
        }
        switch (initStateType) {
            case IDLE:
                break;
            default:
                log.info("non idle state: {}, stateTransitionFollowUp: {}, handleStateResult: {}", state.toString(), stateTransitionFollowUp, handleStateResult);
        }
        return handleStateResult;
    }
}
