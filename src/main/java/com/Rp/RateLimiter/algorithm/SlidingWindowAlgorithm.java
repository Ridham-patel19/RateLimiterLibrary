package com.Rp.RateLimiter.algorithm;

import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowAlgorithm implements RateLimiter {



    private int limit;
    private long windowTimeDurationNano;
    private final Deque<Long> requestTimestamps = new ArrayDeque<>();

    public SlidingWindowAlgorithm(RateLimitConfig config) {
        this.limit = config.getLimit();
        this.windowTimeDurationNano = config.getDurationInMillis() * 1_000_000L;
    }


    @Override
    public synchronized boolean allowRequest() {
    long currentTime = System.nanoTime();


    while(!requestTimestamps.isEmpty() &&
    currentTime - requestTimestamps.peekFirst() > windowTimeDurationNano){
        requestTimestamps.pollFirst();
    }

        if(requestTimestamps.size() < limit){
            requestTimestamps.addLast(currentTime);

            return true;
        }else{
            return false;
        }
    }
}
