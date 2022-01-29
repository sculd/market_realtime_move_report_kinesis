package com.trading.state;

import lombok.Builder;

import java.time.Duration;

public class TimeoutPlan {
    public Common.PriceSnapshot entryPriceSnapShot;

    Duration expirationDuration;

    @Builder
    public static class TimeoutPlanInitParameter {
        public Duration expirationDuration;
    }

    public void init(Common.PriceSnapshot entryPriceSnapShot, TimeoutPlanInitParameter timeoutPlanInitParameter) {
        this.entryPriceSnapShot = entryPriceSnapShot;
        this.expirationDuration = timeoutPlanInitParameter.expirationDuration;
    }

    public boolean getIfTriggered(long epochSeconds) {
        return epochSeconds - entryPriceSnapShot.epochSeconds >= expirationDuration.toSeconds();
    }
}
