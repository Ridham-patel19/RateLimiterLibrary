package com.Rp.RateLimiter.annotation;


import com.Rp.RateLimiter.core.RateLimiterAlgo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int limit();
    long duration();
    RateLimiterAlgo algorithm() default RateLimiterAlgo.TOKEN_BUCKET;
}
