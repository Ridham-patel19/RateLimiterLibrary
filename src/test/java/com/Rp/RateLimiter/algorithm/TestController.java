
package com.Rp.RateLimiter.algorithm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/integration-test")
    public String integrationTest() {
        return "OK";
    }
}
