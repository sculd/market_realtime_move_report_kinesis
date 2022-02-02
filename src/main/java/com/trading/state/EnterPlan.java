package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

@Builder
public class EnterPlan {
    static int precisionDecimals = 3;
    public double targetVolume;
    public Common.PositionSideType positionSideType;
    @Builder.Default
    public Seek seek = new Seek();

    @Builder
    public static class EnterPlanInitParameter {
        public double targetFiatVolume;
        public double seekReverseChangeAmplitude;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(EnterPlanInitParameter.class)
                    .add("targetFiatVolume", targetFiatVolume)
                    .add("seekReverseChangeAmplitude", seekReverseChangeAmplitude)
                    .toString();
        }
    }
    EnterPlanInitParameter enterPlanInitParameter;

    public void init(Common.PositionSideType positionSideType, double price) {
        this.positionSideType = positionSideType;
        long scale = (long)Math.pow(10, precisionDecimals);
        this.targetVolume = Math.round(enterPlanInitParameter.targetFiatVolume / price * scale) / scale;
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
