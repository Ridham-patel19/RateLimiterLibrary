package com.Rp.RateLimiter.config;


import com.Rp.RateLimiter.core.RateLimitConfig;
import com.Rp.RateLimiter.core.RateLimiter;
import com.Rp.RateLimiter.factory.RateLimiterAlgoFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(LimitXWebConfiguration.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LimitXProperties.class)
@ConditionalOnProperty(
        prefix = "limitx",
        name = "enable",
        havingValue = "true",
        matchIfMissing = true
)
public class LimitXAutoConfiguration {


    @Bean
    public RateLimiter globalRateLimiter(LimitXProperties properties){
        RateLimitConfig config = new RateLimitConfig(
                properties.getAlgorithm(),

                properties.getLimit(),
                properties.getDuration()
        );

        System.out.println(properties.getExcludes());
        return RateLimiterAlgoFactory.create(config);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "limitx",
            name = "enable",
            havingValue = "true",
            matchIfMissing = true
    )
    public LimitXInceptor limitXInceptor(RateLimiter globalRateLimiter ,   LimitXProperties properties) {
        return new LimitXInceptor(globalRateLimiter , properties);
    }
}
