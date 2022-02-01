package com.changesanomalytrading.transition;

import com.changesanomalytrading.state.stream.ChangesAnomalyTradingStream;
import com.google.common.base.MoreObjects;
import com.marketsignal.timeseries.analysis.Changes;
import com.marketsignal.timeseries.analysis.ChangesAnomaly;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.marketsignal.util.Time;
import com.trading.performance.ClosedTrade;
import com.trading.state.Common;
import com.trading.state.States;
import com.trading.state.StopLossPlan;
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
    TransitionInitParameter initParameter;

    public ChangesAnomalyStateTransition(String market, String symbol, TransitionInitParameter initParameter) {
        super(market, symbol);
        this.initParameter = initParameter;
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.IDLE) {
            return ret;
        }
        boolean triggerOnJumpAnomaly = initParameter.triggerAnomalyType == TransitionInitParameter.TriggerAnomalyType.JUMP ||
                initParameter.triggerAnomalyType == TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP;
        boolean triggerOnDropAnomaly = initParameter.triggerAnomalyType == TransitionInitParameter.TriggerAnomalyType.DROP ||
                initParameter.triggerAnomalyType == TransitionInitParameter.TriggerAnomalyType.JUMP_OR_DROP;
        boolean jumpAnomalyTriggered = ChangesAnomaly.isMaxJumpAnomaly(analysis, initParameter.maxJumpThreshold) &&
                analysis.analyzeParameter.windowSize.toMinutes() <= initParameter.changeAnalysisWindow.toMinutes();
        boolean dropAnomalyTriggered = ChangesAnomaly.isMinDropAnomaly(analysis, initParameter.minDropThreshold) &&
                analysis.analyzeParameter.windowSize.toMinutes() <= initParameter.changeAnalysisWindow.toMinutes();

        if (triggerOnJumpAnomaly && jumpAnomalyTriggered) {
            log.info(String.format("%s jump anomaly found: %s, analysis: %s", Time.fromEpochSecondsToDateTimeStr(analysis.epochSecondsAtAnalysis), state, analysis));
            state.enterPlan.init(Common.PositionSideType.SHORT, analysis.priceAtAnalysis);
            state.stateType = States.StateType.ENTER_PLAN;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        if (triggerOnDropAnomaly && dropAnomalyTriggered) {
            log.info(String.format("%s drop anomaly found: %s, analysis: %s", Time.fromEpochSecondsToDateTimeStr(analysis.epochSecondsAtAnalysis), state, analysis));
            state.enterPlan.init(Common.PositionSideType.LONG, analysis.priceAtAnalysis);
            state.stateType = States.StateType.ENTER_PLAN;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        return ret;
    }

    public StateTransitionFollowUp handlePositionState(States state, Common.PriceSnapshot priceSnapshot) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.IN_POSITION) {
            return ret;
        }

        boolean takeProfitTriggered = state.exitPlan.takeProfitPlan.seek.getIfTriggered(priceSnapshot.price);
        boolean stopLossTriggered = state.exitPlan.stopLossPlan.seek.getIfTriggered(priceSnapshot.price);
        boolean timeoutTriggered = state.exitPlan.timeoutPlan.getIfTriggered(priceSnapshot.epochSeconds);
        if (takeProfitTriggered) {
            log.info(String.format("%s takeProfitTriggered: state: %s position: %s, at %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, state.position, priceSnapshot));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.takeProfitPlan.seek.seekPrice);
        }
        else if (stopLossTriggered) {
            log.info(String.format("%s stopLossTriggered: state: %s position: %s, at %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, state.position, priceSnapshot));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.stopLossPlan.seek.seekPrice);
        } else if (timeoutTriggered) {
            log.info(String.format("%s timeoutTriggered: state: %s position: %s, at %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, state.position, priceSnapshot));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, priceSnapshot.price);
        }

        return ret;
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
