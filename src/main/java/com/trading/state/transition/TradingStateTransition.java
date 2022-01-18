package com.trading.state.transition;

import com.trading.position.Exit;
import com.trading.position.Position;
import com.trading.state.Common;
import com.trading.state.States;
import com.trading.performance.ClosedTrade;
import com.marketsignal.timeseries.analysis.Changes;
import lombok.Builder;

public class TradingStateTransition {
    Parameter parameter;

    @Builder
    static public class Parameter {
        public Parameter() {
        }
    }

    public TradingStateTransition(Parameter parameter) {
        this.parameter = parameter;
    }

    /*
     * update `seek` of state.
     */
    public void seek(States state) {
    }

    void setStateIdle(States state) {
        state.seek.seekToggleType = Common.SeekToggleType.IDLE;
        state.action.actionType = Common.ActionType.IDLE;
        state.position.positionToggleType = Common.PositionToggleType.IDLE;
        state.exit.exitToggleType = Common.ExitToggleType.IDLE;
    }

    /*
     * update `action`, depending on the `seek` of the state.
     */
    public void seekToAction(States state, double price) {
        switch (state.seek.seekToggleType) {
            case SEEK:
                switch (state.seek.changeType) {
                    case JUMP:
                        if (state.seek.seekPrice >= price) {
                            state.action.copyFromSeek(state.seek);
                            state.action.targetPrice = price;
                        } else {
                            state.action.actionType = Common.ActionType.IDLE;
                        }
                        break;
                    case DROP:
                        if (state.seek.seekPrice <= price) {
                            state.action.copyFromSeek(state.seek);
                            state.action.targetPrice = price;
                        } else {
                            state.action.actionType = Common.ActionType.IDLE;
                        }
                        break;
                }
                break;
            case IDLE:
                setStateIdle(state);
                break;
        }
    }

    Position enterPosition(Position.ExecuteParameter executeParameter) {
        return Position.dryRun(executeParameter);
    }

    Exit exitPosition(Exit.ExecuteParameter executeParameter) {
        return Exit.dryRun(executeParameter);
    }

    /*
     * update the `position`, depending on the `action`, if not idle.
     */
    public void actionToPositionAndExit(States state, String market, String symbol) {
        if (state.action.actionType == Common.ActionType.IDLE) {
            return;
        }

        switch (state.action.actionType) {
            case ENTER:
                Position.ExecuteParameter positionExecuteParameter = Position.ExecuteParameter.builder()
                        .market(market)
                        .symbol(symbol)
                        .positionType(state.action.positionType)
                        .targetPrice(state.action.targetPrice)
                        .targetVolume(state.action.targetVolume)
                        .build();

                state.position = enterPosition(positionExecuteParameter);
                state.exit.exitToggleType = Common.ExitToggleType.IDLE;
                break;
            case EXIT:
                Exit.ExecuteParameter exitExecuteParameter = Exit.ExecuteParameter.builder()
                        .market(market)
                        .symbol(symbol)
                        .positionType(state.action.positionType)
                        .targetPrice(state.action.targetPrice)
                        .build();

                state.exit = exitPosition(exitExecuteParameter);
                break;
            case IDLE:
                break;
        }
    }

    /*
     * update the `exit`, depending on the `position`.
     */
    public void recapClosedTrade(States state, String market, String symbol) {
        if (state.position.positionToggleType == Common.PositionToggleType.IDLE) {
            return;
        }
        if (state.exit.exitToggleType == Common.ExitToggleType.IDLE) {
            return;
        }

        state.closedTrade = ClosedTrade.builder()
                .market(market)
                .symbol(symbol)
                .positionType(state.position.positionType)
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

        // reset state after closing a trade.
        setStateIdle(state);
    }
}
