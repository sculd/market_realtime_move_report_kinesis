package com.trading.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeekSpread {
    private static final Logger log = LoggerFactory.getLogger(SeekSpread.class);

    public enum SpreadSeekType {
        LARGER,
        SMALLER;

        private static final Map<String, SpreadSeekType> ENUM_MAP = Stream.of(SpreadSeekType.values())
                .collect(Collectors.toMap(Enum::name, Function.identity()));

        public static SpreadSeekType of(final String name) {
            return ENUM_MAP.getOrDefault(name, SMALLER);
        }
    }

    public SpreadSeekType spreadSeekType;

    public double seekSpreadToMidRatio;

    public void init(SpreadSeekType spreadSeekType, double seekSpreadToMidRatio) {
        this.spreadSeekType = spreadSeekType;
        this.seekSpreadToMidRatio = seekSpreadToMidRatio;
    }

    public boolean getIfTriggered(double bestAsk, double bestBid) {
        double mid = (bestAsk + bestBid) / 2.0;
        double spreadToMidRatio = (bestAsk - bestBid) / mid;

        switch (spreadSeekType) {
            case SMALLER:
                return spreadToMidRatio <= seekSpreadToMidRatio;
            case LARGER:
                return spreadToMidRatio > seekSpreadToMidRatio;
            default:
                log.error("[SeekSpread.getIfTriggered] invalid spreadSeekType: {}", spreadSeekType);
        }
        return false;
    }
}
