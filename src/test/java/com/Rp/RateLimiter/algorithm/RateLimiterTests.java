package com.Rp.RateLimiter.algorithm;

import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;
import com.Rp.RateLimiter.core.RateLimiterAlgo;
import com.Rp.RateLimiter.factory.RateLimiterAlgoFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTests {


    @Test
    void shouldAllowRequestUnderLimit(){
        RateLimitConfig config = new RateLimitConfig(RateLimiterAlgo.SLIDING_WINDOW , 5 , 5000);

        SlidingWindowAlgorithm limiter =
                new SlidingWindowAlgorithm(config);

        for (int i = 0; i < 6; i++) {
            if( i == 5){
                assertFalse(limiter.allowRequest());
                continue;
            }
            assertTrue(limiter.allowRequest());
        }
    }


    @Test
    void shouldAllowRequestUnderLimit2(){
        RateLimitConfig config = new RateLimitConfig(RateLimiterAlgo.TOKEN_BUCKET , 5 , 5000);

        RateLimiter limiter =
                new TokenBucketRateLimiter(config);

        for (int i = 0; i < 6; i++) {
            if( i == 5){
                assertFalse(limiter.allowRequest());
                continue;
            }
            assertTrue(limiter.allowRequest());
        }
    }

    @Test
    void shouldAllowRequestUnderLimitAndAfterRefill() throws InterruptedException {
        RateLimitConfig config = new RateLimitConfig(
                RateLimiterAlgo.SLIDING_WINDOW, 5, 5000
        );

        RateLimiter limiter = new SlidingWindowAlgorithm(config);

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                Thread.sleep(5100);
                assertTrue(limiter.allowRequest());
                continue;
            }

            assertTrue(limiter.allowRequest());
        }
    }

    @Test
    void shouldAllowRequestUnderLimitAndRefillForTokenBucket() throws InterruptedException {
        RateLimitConfig config = new RateLimitConfig(RateLimiterAlgo.TOKEN_BUCKET , 5 , 2000);

        RateLimiter limiter =
                new TokenBucketRateLimiter(config);

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                Thread.sleep(2100);
                assertTrue(limiter.allowRequest());
                continue;
            }
            assertTrue(limiter.allowRequest());
        }
    }

    @Test
    void shouldAllowRequestAfterWindowExpires() throws InterruptedException {
        RateLimitConfig config = new RateLimitConfig(
                RateLimiterAlgo.SLIDING_WINDOW, 5, 2000
        );

        RateLimiter limiter = new SlidingWindowAlgorithm(config);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest());
        }

        assertFalse(limiter.allowRequest());

        Thread.sleep(2100);

        assertTrue(limiter.allowRequest());
    }


    @Test
    void rateLimiterFactoryWorking(){

        RateLimitConfig config = new RateLimitConfig(RateLimiterAlgo.SLIDING_WINDOW , 5 , 2000);
        assertInstanceOf(SlidingWindowAlgorithm.class , RateLimiterAlgoFactory.create(config)  );
    }

    @Test
    void rateLimiterFactoryWorking2() {

        RateLimitConfig config = new RateLimitConfig(RateLimiterAlgo.TOKEN_BUCKET, 5, 2000);
        assertInstanceOf(TokenBucketRateLimiter.class, RateLimiterAlgoFactory.create(config));
    }

    @Test
    void rateLimiterFactoryWorking3(){
        assertThrowsExactly(IllegalArgumentException.class , ()->  new RateLimitConfig(null , 5 , 2000));


    }



    @Test
    void shouldMaintainIndependentStateForEachLimiter() {
        RateLimitConfig config = new RateLimitConfig(
                RateLimiterAlgo.TOKEN_BUCKET, 2, 5000
        );

        RateLimiter limiterA = new TokenBucketRateLimiter(config);
        RateLimiter limiterB = new TokenBucketRateLimiter(config);

        assertTrue(limiterA.allowRequest());
        assertTrue(limiterA.allowRequest());
        assertFalse(limiterA.allowRequest());

        assertTrue(limiterB.allowRequest());
        assertTrue(limiterB.allowRequest());
        assertFalse(limiterB.allowRequest());
    }

    @Test
    void shouldNotExceedTokenBucketLimitUnderConcurrency()
            throws InterruptedException {

        int limit = 100;
        int totalRequests = 200;

        RateLimitConfig config = new RateLimitConfig(
                RateLimiterAlgo.TOKEN_BUCKET, limit, 60_000
        );

        RateLimiter limiter = new TokenBucketRateLimiter(config);

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(totalRequests);

        AtomicInteger acceptedRequests = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startSignal.await();

                    if (limiter.allowRequest()) {
                        acceptedRequests.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completed.countDown();
                }
            });
        }

        startSignal.countDown();
        completed.await();
        executor.shutdown();

        assertEquals(limit, acceptedRequests.get());
    }

    @Test
    void shouldNotExceedSlidingWindowLimitUnderConcurrency()
            throws InterruptedException {

        int limit = 100;
        int totalRequests = 200;

        RateLimitConfig config = new RateLimitConfig(
                RateLimiterAlgo.SLIDING_WINDOW, limit, 60_000
        );

        RateLimiter limiter = new SlidingWindowAlgorithm(config);

        ExecutorService executor = Executors.newFixedThreadPool(20);

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(totalRequests);

        AtomicInteger acceptedRequests = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startSignal.await();

                    if (limiter.allowRequest()) {
                        acceptedRequests.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completed.countDown();
                }
            });
        }

        startSignal.countDown();
        completed.await();
        executor.shutdown();

        assertEquals(limit, acceptedRequests.get());
    }
}
