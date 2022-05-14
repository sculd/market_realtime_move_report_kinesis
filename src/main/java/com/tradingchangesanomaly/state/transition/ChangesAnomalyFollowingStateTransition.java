package com.tradingchangesanomaly.state.transition;

import com.marketsignal.marginasset.MarginAsset;
import com.marketsignal.orderbook.OrderbookFactory;
import com.marketsignal.timeseries.analysis.changes.Changes;
import com.marketsignal.timeseries.analysis.changes.ChangesAnomaly;
import com.marketsignal.util.Time;
import com.trading.state.Common;
import com.trading.state.States;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChangesAnomalyFollowingStateTransition extends ChangesAnomalyStateTransition {
    private static final Logger log = LoggerFactory.getLogger(ChangesAnomalyFollowingStateTransition.class);

    public ChangesAnomalyFollowingStateTransition(String market, String symbol, OrderbookFactory orderbookFactory, MarginAsset marginAsset, TransitionInitParameter initParameter) {
        super(market, symbol, orderbookFactory, marginAsset, initParameter);
    }

    public StateTransitionFollowUp planEnter(States state, Changes.AnalyzeResult analysis, boolean isMarginAsset) {
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
            state.enterPlan.init(Common.PositionSideType.LONG, analysis.priceAtAnalysis);
            state.stateType = States.StateType.ENTER_PLAN;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        if (triggerOnDropAnomaly && dropAnomalyTriggered) {
            log.info(String.format("%s drop anomaly found: %s, analysis: %s", Time.fromEpochSecondsToDateTimeStr(analysis.epochSecondsAtAnalysis), state, analysis));

            if (!isMarginAsset) {
                log.info(String.format("%s drop anomaly found but not a tradable asset: %s analysis: %s", state, analysis));
                return ret;
            }

            state.enterPlan.init(Common.PositionSideType.SHORT, analysis.priceAtAnalysis);
            state.stateType = States.StateType.ENTER_PLAN;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        return ret;
    }
}
