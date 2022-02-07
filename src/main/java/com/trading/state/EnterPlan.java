package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

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
        public double seekChangeAmplitude;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(EnterPlanInitParameter.class)
                    .add("targetFiatVolume", targetFiatVolume)
                    .add("seekChangeAmplitude", seekChangeAmplitude)
                    .toString();
        }

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add(String.format("%s", "targetFiatVolume"));
            headers.add(String.format("%s", "seekChangeAmplitude"));
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%f", targetFiatVolume));
            columns.add(String.format("%f", seekChangeAmplitude));
            return String.join(",", columns);
        }

        static public EnterPlanInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            return EnterPlanInitParameter.builder()
                    .targetFiatVolume(Double.parseDouble(columns[0]))
                    .seekChangeAmplitude(Double.parseDouble(columns[1]))
                    .build();
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
        seek.init(changeType, price, sign * enterPlanInitParameter.seekChangeAmplitude);
    }
}
