package com.changesanomalytrading.transition;

import com.marketsignal.timeseries.analysis.Changes;
import com.trading.state.Common;
import com.trading.state.States;
import com.trading.state.transition.StateTransition;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangesAnomalyStateTransition extends StateTransition {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyStateTransition.class);

    @Builder
    static public class Parameter {
        public Parameter() {
        }
    }

    Parameter parameter;

    public ChangesAnomalyStateTransition(String market, String symbol, Parameter parameter) {
        super(market, symbol);
        this.parameter = parameter;
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.IDLE) {
            return ret;
        }
        if (analysis.maxJump > 0.05 && analysis.analyzeParameter.windowSize.toMinutes() <= 20) {
            log.info(String.format("anomaly found: %s", state.toString()));
            state.enterPlan.init(Common.PositionSideType.SHORT, analysis.priceAtAnalysis, 1000);
            state.stateType = States.StateType.ENTER_PLAN;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        return ret;
    }

    public StateTransitionFollowUp handlePositionState(States state, double price) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.IN_POSITION) {
            return ret;
        }

        boolean takeProfitTriggered = state.exitPlan.takeProfitPlan.seek.getIfTriggered(price);
        boolean stopLossTriggered = state.exitPlan.stopLossPlan.seek.getIfTriggered(price);
        if (takeProfitTriggered) {
            log.info(String.format("takeProfitTriggered: %s", state.toString()));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.takeProfitPlan.seek.seekPrice);
        }
        else if (stopLossTriggered) {
            log.info(String.format("stopLossTriggered: %s", state.toString()));
            state.stateType = States.StateType.EXIT;
            state.exit.init(state.position, state.exitPlan.stopLossPlan.seek.seekPrice);
        }

        return ret;
    }

    public void handleState(States state, Changes.AnalyzeResult analysis) {
        StateTransitionFollowUp stateTransitionFollowUp = StateTransitionFollowUp.CONTINUE_TRANSITION;
        while (stateTransitionFollowUp == StateTransitionFollowUp.CONTINUE_TRANSITION) {
            switch (state.stateType) {
                case IDLE:
                    stateTransitionFollowUp = planEnter(state, analysis);
                    break;
                case ENTER_PLAN:
                    stateTransitionFollowUp = handleEnterPlanState(state, analysis.priceAtAnalysis);
                    break;
                case ENTER:
                    stateTransitionFollowUp = handleEnterState(state);
                    break;
                case IN_POSITION:
                    stateTransitionFollowUp = handlePositionState(state, analysis.priceAtAnalysis);
                    break;
                case EXIT:
                    stateTransitionFollowUp = handleExitState(state);
                    break;
            }
        }
    }
}
