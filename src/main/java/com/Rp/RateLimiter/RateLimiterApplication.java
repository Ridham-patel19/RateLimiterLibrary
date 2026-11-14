package com.Rp.RateLimiter;

import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;
import com.Rp.RateLimiter.core.RateLimiterAlgo;
import com.Rp.RateLimiter.factory.RateLimiterAlgoFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimiterApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(RateLimiterApplication.class, args);

	}
}
