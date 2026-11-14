package com.Rp.RateLimiter.config;

import com.Rp.RateLimiter.annotation.RateLimited;
import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;
import com.Rp.RateLimiter.factory.RateLimiterAlgoFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LimitXInceptor implements HandlerInterceptor {


    private final RateLimiter globalRateLimiter;
    private final ConcurrentMap<String , RateLimiter> endPointLimiters
            = new ConcurrentHashMap<>();


    private final   LimitXProperties properties;

    public LimitXInceptor(RateLimiter globalRateLimiter, LimitXProperties properties) {
        this.globalRateLimiter = globalRateLimiter;
        this.properties = properties;
    }


    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            //represent the method or activity that is being call like controller method or static resource
            Object handler
    )throws Exception{


        String requestPath = request.getRequestURI();


        //if it contains any URI which in exclude array it doesnot count in limit
        if(properties.getExcludes().contains(requestPath)){
            return true;
        }

// first we initiliase global limiter
        RateLimiter limiter = globalRateLimiter;


        //checks if comming object is handler method (endpoint method in controller)
        if(handler instanceof HandlerMethod handlerMethod){
            //getting annotation
            RateLimited annotation = handlerMethod.getMethodAnnotation(RateLimited.class);


//if developer had annoted with our annotation
            if(annotation != null){

                validateRateLimit(annotation);

                //so here for every endoiint will have its own limiter obj
                //we are managing it in concurrent map with the key and RateLimiter
                //object


                //here we are getting key like class signature from where this
                //method is being called and then method signature
                String endpointKey = handlerMethod.getBeanType().getName()
                        + "#" + handlerMethod.getMethod().toGenericString();

                //it will check if this endpointKey exist in map or not if not then it will
                //create one an dadd it to the map and if exist then it will return that
                limiter = endPointLimiters.computeIfAbsent(
                        endpointKey,
                        key -> RateLimiterAlgoFactory.create(new RateLimitConfig(
                                annotation.algorithm(),
                                annotation.limit(),
                                annotation.duration()

                        ))
                );

            }
        }
        if(limiter.allowRequest()){
            return true;
        }

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\"}"
        );

        return false;
    }



    private void validateRateLimit(RateLimited annotation) {
        if (annotation.limit() <= 0) {
            throw new IllegalArgumentException(
                    "@RateLimited limit must be greater than zero"
            );
        }

        if (annotation.duration() <= 0) {
            throw new IllegalArgumentException(
                    "@RateLimited duration must be greater than zero"
            );
        }

        if (annotation.algorithm() == null) {
            throw new IllegalArgumentException(
                    "@RateLimited algorithm cannot be null"
            );
        }
    }

}
