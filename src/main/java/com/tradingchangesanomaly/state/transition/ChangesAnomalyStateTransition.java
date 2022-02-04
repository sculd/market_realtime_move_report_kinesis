package com.tradingchangesanomaly.state.transition;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
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

public class ChangesAnomalyStateTransition extends StateTransition {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyStateTransition.class);

    @Builder
    static public class TransitionInitParameter {
        public double maxJumpThreshold;
        public double minDropThreshold;
        public Duration changeAnalysisWindow;
        public enum TriggerAnomalyType {
            JUMP,
            DROP,
            JUMP_OR_DROP;
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
    }
    public TransitionInitParameter initParameter;

    public ChangesAnomalyStateTransition(String market, String symbol, TransitionInitParameter initParameter) {
        super(market, symbol);
        this.initParameter = initParameter;
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis) {
        // to be implemented in following / reversal trades.
        return  StateTransitionFollowUp.HALT_TRANSITION;
    }

    static public class HandleStateResult {
        public ClosedTrade closedTrade;
    }

    public HandleStateResult handleState(States state, BarWithTimeSlidingWindow barWithTimeSlidingWindow) {
        Changes.AnalyzeResult changeAnalysis = Changes.analyze(barWithTimeSlidingWindow, Changes.AnalyzeParameter.builder()
                .windowSize(initParameter.changeAnalysisWindow)
                .build());
        Volatility.AnalyzeResult volatilityAnalysis = Volatility.analyze(barWithTimeSlidingWindow, Volatility.AnalyzeParameter.builder()
                .windowSizes(List.of(initParameter.changeAnalysisWindow, Duration.ofMinutes(initParameter.changeAnalysisWindow.toMinutes() * 2)))
                .build());
        HandleStateResult handleStateResult = new HandleStateResult();
        StateTransitionFollowUp stateTransitionFollowUp = StateTransitionFollowUp.CONTINUE_TRANSITION;
        while (stateTransitionFollowUp == StateTransitionFollowUp.CONTINUE_TRANSITION) {
            switch (state.stateType) {
                case IDLE:
                    stateTransitionFollowUp = planEnter(state, changeAnalysis);
                    break;
                case ENTER_PLAN:
                    stateTransitionFollowUp = handleEnterPlanState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build());
                    break;
                case ENTER:
                    stateTransitionFollowUp = handleEnterState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build());
                    break;
                case IN_POSITION:
                    state.exitPlan.stopLossPlan.onPriceUpdate(changeAnalysis.priceAtAnalysis);
                    stateTransitionFollowUp = handlePositionState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build());
                    break;
                case EXIT:
                    stateTransitionFollowUp = handleExitState(state, Common.PriceSnapshot.builder().price(changeAnalysis.priceAtAnalysis).epochSeconds(changeAnalysis.epochSecondsAtAnalysis).build());
                    break;
                case TRADE_CLOSED:
                    stateTransitionFollowUp = handleTradeClosed(state);
                    handleStateResult.closedTrade = state.closedTrade;
                    break;
            }
        }
        return handleStateResult;
    }
}
