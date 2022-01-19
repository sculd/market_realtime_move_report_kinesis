package com.trading.state;

import lombok.Builder;

@Builder
public class Seek {
    public Common.SeekToggleType seekToggleType;

    public Common.ActionType actionType;

    public Common.ChangeType changeType;

    public Common.PositionType positionType;

    public double priceAtSeekInit;

    public double seekPrice;

    public void setSeekChange(double changeRatio) {
        seekPrice = priceAtSeekInit * (1.0 + changeRatio);
    }
}
