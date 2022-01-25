package com.trading.state;

import lombok.Builder;

@Builder
public class EnterPlan {
    public double targetVolume;
    public Common.PositionSideType positionSideType;
    @Builder.Default
    public Seek seek = new Seek();

    @Builder
    public static class EnterPlanInitParameter {
        public double targetVolume;
        public double seekReverseChangeAmplitude;
    }
    EnterPlanInitParameter enterPlanInitParameter;

    public void init(Common.PositionSideType positionSideType, double price) {
        this.positionSideType = positionSideType;
        this.targetVolume = enterPlanInitParameter.targetVolume;
        Common.ChangeType changeType = Common.ChangeType.JUMP;
        double sign = 1.0;
        switch (positionSideType) {
            case SHORT:
                changeType = Common.ChangeType.DROP;
                sign = -1.0;
                break;
        }
        seek.init(changeType, price, sign * enterPlanInitParameter.seekReverseChangeAmplitude);
    }
}
