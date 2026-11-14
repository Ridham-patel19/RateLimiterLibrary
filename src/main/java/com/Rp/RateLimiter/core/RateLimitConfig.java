
package com.Rp.RateLimiter.core;

public class RateLimitConfig {

    private final RateLimiterAlgo algorithm;
    private final int limit;
    private final long durationInMillis;

    public RateLimitConfig(
            RateLimiterAlgo algorithm,
            int limit,
            long durationInMillis) {

        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        if (durationInMillis <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        this.algorithm = algorithm;
        this.limit = limit;
        this.durationInMillis = durationInMillis;
    }

    public RateLimiterAlgo getAlgorithm() {
        return algorithm;
    }

    public int getLimit() {
        return limit;
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }
}
