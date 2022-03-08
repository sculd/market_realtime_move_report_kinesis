package com.trading.state.transition;

import com.marketsignal.timeseries.analysis.Analyses;
import com.marketsignal.util.Time;
import com.trading.state.*;
import com.trading.performance.ClosedTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StateTransition {
    private static final Logger log = LoggerFactory.getLogger(StateTransition.class);

    public String market;
    public String symbol;

    public StateTransition(String market, String symbol) {
        this.market = market;
        this.symbol = symbol;
    }

    public enum StateTransitionFollowUp {
        CONTINUE_TRANSITION,
        HALT_TRANSITION;
    }

    /*
     * handle the ENTER_PLAN state
     */
    public StateTransitionFollowUp handleEnterPlanState(States state, Common.PriceSnapshot priceSnapshot) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.ENTER_PLAN) {
            return ret;
        }

        boolean enterPlanTriggered = state.enterPlan.seek.getIfTriggered(priceSnapshot.price);
        if (enterPlanTriggered) {
            log.info(String.format("%s enterPlanTriggered: %s at %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, priceSnapshot));
            state.enter.targetPrice = priceSnapshot.price;
            state.enter.positionSideType = state.enterPlan.positionSideType;
            state.enter.targetVolume = state.enterPlan.targetVolume;
            state.stateType = States.StateType.ENTER;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        return ret;
    }

    Enter.ExecuteResult enterPosition(Enter enter, Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        return enter.execute(priceSnapshot, analyses);
    }

    Exit.ExecuteResult exitPosition(Exit exit) {
        return exit.execute();
    }

    /*
     * handle the ENTER state
     */
    public StateTransitionFollowUp handleEnterState(States state, Common.PriceSnapshot priceSnapshot, Analyses analyses) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.ENTER) {
            return ret;
        }

        Enter.ExecuteResult executeResult = enterPosition(state.enter, priceSnapshot, analyses);
        switch (executeResult.result) {
            case SUCCESS:
                log.info(String.format("%s entering into a position succeeded: %s at %s, resulting position: %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, priceSnapshot, executeResult.position));
                state.stateType = States.StateType.ENTER_ORDER_IN_PROGRESS;
                state.enterInProgress.init(executeResult.orderID, priceSnapshot);
                ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
                break;
            case FAIL:
                log.info(String.format("%s entering into a position failed: %s at %s, resulting position: %s", Time.fromEpochSecondsToDateTimeStr(priceSnapshot.epochSeconds), state, priceSnapshot, executeResult.position));
                state.stateType = States.StateType.IDLE;
                ret = StateTransitionFollowUp.HALT_TRANSITION;
                break;
        }
        state.position = executeResult.position;
        state.exitPlan = executeResult.exitPlan;
        state.stateType = States.StateType.IN_POSITION;
        return ret;
    }

    /*
     * handle the ENTER_ORDER_IN_PROGRESS state
     */
    public StateTransitionFollowUp handleEnterInProgressState(States state) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.ENTER_ORDER_IN_PROGRESS) {
            return ret;
        }

        OrderInProgress.OrderInProgressStatus enterInProgressStatus = state.enterInProgress.getProgressStatus();
        switch (enterInProgressStatus) {
            case ORDER_COMPLETE:
                state.stateType = States.StateType.IN_POSITION;
                ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
                break;
            case ORDER_IN_PROGRESS:
                // wait and retry
                ret = StateTransitionFollowUp.HALT_TRANSITION;
                break;
            case TIMEOUT:
                state.stateType = States.StateType.IDLE;
                ret = StateTransitionFollowUp.HALT_TRANSITION;
                break;
        }
        return ret;
    }

    public StateTransitionFollowUp handlePositionState(States state, Common.PriceSnapshot priceSnapshot) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.IN_POSITION) {
            return ret;
        }

        boolean takeProfitTriggered = state.exitPlan.takeProfitPlan.getIfTriggered(priceSnapshot.price);
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

    /*
     * tba
     */
    public StateTransitionFollowUp handleExitState(States state, Common.PriceSnapshot priceSnapshot) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.EXIT) {
            return ret;
        }

        Exit.ExecuteResult executeResult = exitPosition(state.exit);

        switch (executeResult.result) {
            case SUCCESS:
                log.info(String.format("exiting from a position succeeded: %s at %s", state.toString(), priceSnapshot));
                state.stateType = States.StateType.EXIT_ORDER_IN_PROGRESS;
                ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
                break;
            case FAIL:
                log.info(String.format("exiting from a position failed: %s at %s", state.toString(), priceSnapshot));
                state.stateType = States.StateType.EXIT;
                break;
        }
        return ret;
    }

    /*
     * handle the EXIT_ORDER_IN_PROGRESS state
     */
    public StateTransitionFollowUp handleExitInProgressState(States state) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.EXIT_ORDER_IN_PROGRESS) {
            return ret;
        }

        OrderInProgress.OrderInProgressStatus enterInProgressStatus = state.enterInProgress.getProgressStatus();
        switch (enterInProgressStatus) {
            case ORDER_COMPLETE:
                state.stateType = States.StateType.TRADE_CLOSED;
                ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
                break;
            case ORDER_IN_PROGRESS:
                // wait and retry
                ret = StateTransitionFollowUp.HALT_TRANSITION;
                break;
            case TIMEOUT:
                state.stateType = States.StateType.IDLE;
                ret = StateTransitionFollowUp.HALT_TRANSITION;
                break;
        }
        return ret;
    }

    /*
     * tba
     */
    public StateTransitionFollowUp handleTradeClosed(States state) {
        log.info(String.format("recapping a closed trade: %s", state.toString()));
        state.closedTrade = ClosedTrade.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(state.position.positionSideType)
                .entryTargetPrice(state.enter.targetPrice)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(state.position.entryPriceSnapshot.price)
                        .epochSeconds(state.position.entryPriceSnapshot.epochSeconds)
                        .build())
                .volume(state.position.volume)
                .exitTargetPrice(state.exit.targetPrice)
                .exitPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(state.exit.exitPriceSnapshot.price)
                        .epochSeconds(state.exit.exitPriceSnapshot.epochSeconds)
                        .build())
                .analysesUponEnter(state.position.analysesUponEnter)
                .build();
        state.stateType = States.StateType.IDLE;
        return StateTransitionFollowUp.HALT_TRANSITION;
    }
}
