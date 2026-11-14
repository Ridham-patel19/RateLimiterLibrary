package com.Rp.RateLimiter.factory;

import com.Rp.RateLimiter.algorithm.SlidingWindowAlgorithm;
import com.Rp.RateLimiter.algorithm.TokenBucketRateLimiter;
import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;
import com.Rp.RateLimiter.core.RateLimiterAlgo;

public class RateLimiterAlgoFactory {

    public static RateLimiter create(RateLimitConfig config){

        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        RateLimiterAlgo algo = config.getAlgorithm();

        return switch (algo) {
            case RateLimiterAlgo.TOKEN_BUCKET -> new TokenBucketRateLimiter(config);
            case RateLimiterAlgo.SLIDING_WINDOW ->  new SlidingWindowAlgorithm(config);
            default -> throw new IllegalArgumentException("No Algorithm like this exist");
        };
    }
}
