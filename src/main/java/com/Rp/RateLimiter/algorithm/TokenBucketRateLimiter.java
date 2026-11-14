package com.Rp.RateLimiter.algorithm;

import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;

public class TokenBucketRateLimiter implements RateLimiter {


    // it is the size of bucket
    private int bucketSize;

    //it is the time last time we have added a token in bucket
    private long lastRefillTime;

    //this is the currently available token
    private double availableTokens;

    //this is the time after we will refill one in bucket
    private long refillTimeIntervalForToken;


    public TokenBucketRateLimiter(RateLimitConfig config) {
        this.bucketSize = config.getLimit();
        this.lastRefillTime = System.nanoTime();
        this.availableTokens = config.getLimit();
        this.refillTimeIntervalForToken = config.getDurationInMillis() * 1_000_000L;
    }


    @Override
    public synchronized boolean allowRequest() {

        refillTheBucket();

        if(availableTokens >= 1){
            availableTokens--;
            return true;
        }
        return false;
    }

    private void refillTheBucket() {
        long currentTime = System.nanoTime();

        //here we are calculating that how much had been completed after last refill
        long elapsedTime = currentTime - lastRefillTime;

        //here in this algo we are using lazy token adding
        //this method used in isAllowReq in that whenever req will come
        //this method wil be called so it will calculate the total time elapsed
        //after the last refill and then devide by interval so now we have how many
        //token we have to add . this is all done when req come that is why
        //this is a lazy loading
        double tokenToAdd =(double) elapsedTime/refillTimeIntervalForToken;

        if(tokenToAdd >=1){
            availableTokens = Math.min(bucketSize , availableTokens + tokenToAdd);
        }


        if (availableTokens >= bucketSize) {
            //if buvcket is already full we donot have to preserve those extra fraction tokens
            availableTokens = bucketSize;
            //we are again setting last refill time cuz we have checked the refill
            lastRefillTime = currentTime;
        } else {
            long wholeTokens = (long) tokenToAdd;

            long leftoverNanos = elapsedTime
                    - (wholeTokens * refillTimeIntervalForToken);

            lastRefillTime = currentTime - leftoverNanos;
        }


    }
}
