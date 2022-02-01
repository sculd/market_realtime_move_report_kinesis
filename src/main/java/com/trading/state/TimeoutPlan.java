package com.trading.state;

import com.google.common.base.MoreObjects;
import lombok.Builder;

import java.time.Duration;

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
