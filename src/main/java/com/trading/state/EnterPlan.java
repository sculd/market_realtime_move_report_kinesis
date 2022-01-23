package com.trading.state;

public class EnterPlan {
    public double targetVolume;
    public Common.PositionSideType positionSideType;
    public Seek seek = new Seek();

    public void init(Common.PositionSideType positionSideType, double price, double targetVolume) {
        this.positionSideType = positionSideType;
        this.targetVolume = targetVolume;
        seek.init(Common.ChangeType.JUMP, price, 0);
    }
}
