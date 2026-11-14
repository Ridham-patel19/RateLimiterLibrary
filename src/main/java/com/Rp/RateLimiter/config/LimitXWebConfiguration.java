package com.Rp.RateLimiter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration(proxyBeanMethods = false)
public class LimitXWebConfiguration implements WebMvcConfigurer {


    private LimitXInceptor  limitXInceptor;


    public LimitXWebConfiguration(LimitXInceptor limitXInceptor) {
        this.limitXInceptor = limitXInceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limitXInceptor);
    }
}
