
package com.Rp.RateLimiter;

import com.Rp.RateLimiter.annotation.RateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController22 {

    @RateLimited(limit = 3, duration = 60000)
    @GetMapping("/test")
    public String test() {
        return "Request successful!";
    }


    @GetMapping("/global")
    public String global() {
        return "Request successful for global limiter!";
    }


    @GetMapping("/health")
    public String health() {
        return "Request successful for health checks!";
    }
}
