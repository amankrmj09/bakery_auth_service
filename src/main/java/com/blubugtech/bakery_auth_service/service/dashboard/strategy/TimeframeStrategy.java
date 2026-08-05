package com.blubugtech.bakery_auth_service.service.dashboard.strategy;

import java.time.LocalDate;

public enum TimeframeStrategy {
    DAILY("1d") {
        @Override
        public LocalDate getPastDate(LocalDate today) {
            return today.minusDays(1);
        }

        @Override
        public LocalDate getPreviousPeriodDate(LocalDate today) {
            return today.minusDays(2);
        }
    },
    WEEKLY("7d") {
        @Override
        public LocalDate getPastDate(LocalDate today) {
            return today.minusDays(7);
        }

        @Override
        public LocalDate getPreviousPeriodDate(LocalDate today) {
            return today.minusDays(14);
        }
    },
    MONTHLY("30d") {
        @Override
        public LocalDate getPastDate(LocalDate today) {
            return today.minusMonths(1);
        }

        @Override
        public LocalDate getPreviousPeriodDate(LocalDate today) {
            return today.minusMonths(2);
        }
    };

    private final String timeframe;

    TimeframeStrategy(String timeframe) {
        this.timeframe = timeframe;
    }

    public static TimeframeStrategy fromString(String timeframe) {
        for (TimeframeStrategy strategy : values()) {
            if (strategy.timeframe.equalsIgnoreCase(timeframe)) {
                return strategy;
            }
        }
        return MONTHLY;
    }

    public abstract LocalDate getPastDate(LocalDate today);

    public abstract LocalDate getPreviousPeriodDate(LocalDate today);
}
