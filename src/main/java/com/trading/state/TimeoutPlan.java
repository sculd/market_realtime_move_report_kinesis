package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TimeoutPlan {
    public Common.PriceSnapshot entryPriceSnapShot;

    Duration expirationDuration;

    @Builder
    public static class TimeoutPlanInitParameter {
        public Duration expirationDuration;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(TimeoutPlanInitParameter.class)
                    .add("expirationDuration", expirationDuration)
                    .toString();
        }

        static public String toCsvHeader() {
            List<String> headers = new ArrayList<>();
            headers.add("expirationDuration");
            return String.join(",", headers);
        }

        public String toCsvLine() {
            List<String> columns = new ArrayList<>();
            columns.add(String.format("%d", expirationDuration.toMinutes()));
            return String.join(",", columns);
        }

        static public TimeoutPlanInitParameter fromCsvLine(String csvLine) {
            String[] columns = csvLine.split(",");
            return TimeoutPlanInitParameter.builder()
                    .expirationDuration(Duration.ofMinutes(Integer.valueOf(columns[0])))
                    .build();
        }
    }

    public void init(Common.PriceSnapshot entryPriceSnapShot, TimeoutPlanInitParameter timeoutPlanInitParameter) {
        this.entryPriceSnapShot = entryPriceSnapShot;
        this.expirationDuration = timeoutPlanInitParameter.expirationDuration;
    }

    public boolean getIfTriggered(long epochSeconds) {
        return epochSeconds - entryPriceSnapShot.epochSeconds >= expirationDuration.toSeconds();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TimeoutPlan.class)
                .add("entryPriceSnapShot", entryPriceSnapShot)
                .add("expirationDuration", expirationDuration)
                .toString();
    }
}
