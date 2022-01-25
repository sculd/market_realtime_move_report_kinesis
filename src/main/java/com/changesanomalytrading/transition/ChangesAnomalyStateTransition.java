package com.changesanomalytrading.transition;

import com.marketsignal.timeseries.analysis.Changes;
import com.marketsignal.timeseries.analysis.ChangesAnomaly;
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
        if (ChangesAnomaly.isMaxJumpAnomaly(analysis, initParameter.maxJumpThreshold) && analysis.analyzeParameter.windowSize.toMinutes() <= 20) {
            log.info(String.format("anomaly found: %s, analysis: %s", state.toString(), analysis));
            state.enterPlan.init(Common.PositionSideType.SHORT, analysis.priceAtAnalysis);
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
        if (takeProfitTriggered) {
            log.info(String.format("takeProfitTriggered: %s at %s", state.toString(), priceSnapshot));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.takeProfitPlan.seek.seekPrice);
        }
        else if (stopLossTriggered) {
            log.info(String.format("stopLossTriggered: %s at %s", state.toString(), priceSnapshot));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.stopLossPlan.seek.seekPrice);
        }

        return ret;
    }

    static public class HandleStateResult {
        public ClosedTrade closedTrade;
    }

    public HandleStateResult handleState(States state, Changes.AnalyzeResult analysis) {
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
