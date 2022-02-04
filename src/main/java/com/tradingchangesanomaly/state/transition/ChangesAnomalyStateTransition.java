package com.tradingchangesanomaly.state.transition;

import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.timeseries.analysis.Changes;
import com.trading.performance.ClosedTrade;
import com.trading.state.Common;
import com.trading.state.States;
import com.trading.state.transition.StateTransition;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

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
    }
    public TransitionInitParameter initParameter;

    public ChangesAnomalyStateTransition(String market, String symbol, TransitionInitParameter initParameter) {
        super(market, symbol);
        this.initParameter = initParameter;
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis) {
        return  StateTransitionFollowUp.HALT_TRANSITION;
    }

    static public class HandleStateResult {
        public ClosedTrade closedTrade;
    }

    public HandleStateResult handleState(States state, BarWithTimeSlidingWindow barWithTimeSlidingWindow) {
        Changes.AnalyzeResult analysis = Changes.analyze(barWithTimeSlidingWindow, Changes.AnalyzeParameter.builder()
                .windowSize(initParameter.changeAnalysisWindow)
                .build());
        HandleStateResult handleStateResult = new HandleStateResult();
        StateTransitionFollowUp stateTransitionFollowUp = StateTransitionFollowUp.CONTINUE_TRANSITION;
        while (stateTransitionFollowUp == StateTransitionFollowUp.CONTINUE_TRANSITION) {
            switch (state.stateType) {
                case IDLE:
                    stateTransitionFollowUp = planEnter(state, analysis);
                    break;
                case ENTER_PLAN:
                    stateTransitionFollowUp = handleEnterPlanState(state, Common.PriceSnapshot.builder().price(analysis.priceAtAnalysis).epochSeconds(analysis.epochSecondsAtAnalysis).build());
                    break;
                case ENTER:
                    stateTransitionFollowUp = handleEnterState(state, Common.PriceSnapshot.builder().price(analysis.priceAtAnalysis).epochSeconds(analysis.epochSecondsAtAnalysis).build());
                    break;
                case IN_POSITION:
                    state.exitPlan.stopLossPlan.onPriceUpdate(analysis.priceAtAnalysis);
                    stateTransitionFollowUp = handlePositionState(state, Common.PriceSnapshot.builder().price(analysis.priceAtAnalysis).epochSeconds(analysis.epochSecondsAtAnalysis).build());
                    break;
                case EXIT:
                    stateTransitionFollowUp = handleExitState(state, Common.PriceSnapshot.builder().price(analysis.priceAtAnalysis).epochSeconds(analysis.epochSecondsAtAnalysis).build());
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
