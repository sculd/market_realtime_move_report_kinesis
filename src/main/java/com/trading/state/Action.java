package com.trading.state;

import lombok.Builder;

@Builder
public class Action {
    public Common.ActionType actionType;

    public Common.PositionType positionType;

    public double targetPrice;

    public double targetVolume;

    public void copyFromSeek(Seek seek) {
        actionType = seek.actionType;
        positionType = seek.positionType;
    }
}
