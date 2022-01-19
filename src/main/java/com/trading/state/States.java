package com.trading.state;

import com.trading.position.Exit;
import com.trading.position.Position;
import com.trading.performance.ClosedTrade;

public class States {
    public String market;
    public String symbol;

    public Seek seek;
    public Action action;
    public Position position;
    public Exit exit;
    public ClosedTrade closedTrade;

    public States(String market, String symbol) {
        this.market = market;
        this.symbol = symbol;

        seek = Seek.builder().seekToggleType(Common.SeekToggleType.IDLE).build();
        action = Action.builder().actionType(Common.ActionType.IDLE).build();
        position = Position.builder().positionToggleType(Common.PositionToggleType.IDLE).build();
        exit = Exit.builder().exitToggleType(Common.ExitToggleType.IDLE).build();
    }
}
