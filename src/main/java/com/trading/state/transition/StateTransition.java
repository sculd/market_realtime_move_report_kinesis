package com.trading.state.transition;

import com.trading.state.Enter;
import com.trading.state.Exit;
import com.trading.state.States;
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
     * tba
     */
    public StateTransitionFollowUp handleEnterPlanState(States state, double price) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.ENTER_PLAN) {
            return ret;
        }

        boolean enterPlanTriggered = state.enterPlan.seek.getIfTriggered(price);
        if (enterPlanTriggered) {
            log.info(String.format("enterPlanTriggered: %s", state.toString()));
            state.enter.targetPrice = price;
            state.enter.positionSideType = state.enterPlan.positionSideType;
            state.stateType = States.StateType.ENTER;
            ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
        }
        return ret;
    }

    Enter.ExecuteResult enterPosition(Enter enter) {
        return enter.execute();
    }

    Exit.ExecuteResult exitPosition(Exit exit) {
        return exit.execute();
    }

    /*
     * tba
     */
    public StateTransitionFollowUp handleEnterState(States state) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.ENTER) {
            return ret;
        }

        Enter.ExecuteResult executeResult = enterPosition(state.enter);
        switch (executeResult.result) {
            case SUCCESS:
                log.info(String.format("entering into a position succeeded: %s", state.toString()));
                state.stateType = States.StateType.IN_POSITION;
                ret = StateTransitionFollowUp.CONTINUE_TRANSITION;
                break;
            case FAIL:
                log.info(String.format("entering into a position failed: %s", state.toString()));
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
     * tba
     */
    public StateTransitionFollowUp handleExitState(States state) {
        StateTransitionFollowUp ret = StateTransitionFollowUp.HALT_TRANSITION;
        if (state.stateType != States.StateType.EXIT) {
            return ret;
        }

        Exit.ExecuteResult executeResult = exitPosition(state.exit);

        switch (executeResult) {
            case SUCCESS:
                log.info(String.format("exiting from a position succeeded: %s", state.toString()));
                state.stateType = States.StateType.IDLE;
                break;
            case FAIL:
                log.info(String.format("exiting from a position failed: %s", state.toString()));
                state.stateType = States.StateType.EXIT;
                break;
        }
        return ret;
    }

    /*
     * tba
     */
    public void recapClosedTrade(States state) {
        log.info(String.format("recaping a closed trade: %s", state.toString()));
        state.closedTrade = ClosedTrade.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(state.position.positionSideType)
                .entryPriceSnapshot(ClosedTrade.PriceSnapshot.builder()
                        .price(state.position.entryPriceSnapshot.price)
                        .epochSeconds(state.position.entryPriceSnapshot.epochSeconds)
                        .build())
                .volume(state.position.volume)
                .exitPriceSnapshot(ClosedTrade.PriceSnapshot.builder()
                        .price(state.exit.exitPriceSnapshot.price)
                        .epochSeconds(state.exit.exitPriceSnapshot.epochSeconds)
                        .build())
                .build();
    }
}
