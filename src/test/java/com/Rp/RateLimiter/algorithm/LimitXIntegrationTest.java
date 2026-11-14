
package com.Rp.RateLimiter.algorithm;

import com.Rp.RateLimiter.config.LimitXAutoConfiguration;
import com.Rp.RateLimiter.core.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "limitx.enable=true",
                "limitx.algorithm=TOKEN_BUCKET",
                "limitx.limit=5",
                "limitx.duration=60000"
        }
)
@AutoConfigureTestRestTemplate
@Import(LimitXAutoConfiguration.class)
class LimitXIntegrationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterGlobalRateLimiterBean() {
        assertNotNull(context.getBean(RateLimiter.class));
    }

    @Test
    void shouldRegisterInterceptorBean() {
        assertNotNull(context.getBean("limitXInceptor"));
    }

    @Test
    void shouldReturn429WhenGlobalLimitIsExceeded() {

        // First five requests should succeed.
        for (int i = 0; i < 5; i++) {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            "/integration-test",
                            String.class
                    );

            assertEquals(
                    HttpStatus.OK,
                    response.getStatusCode(),
                    "Request " + (i + 1) + " should succeed"
            );
        }

        // Sixth request should be rejected.
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/integration-test",
                        String.class
                );

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                response.getStatusCode()
        );
    }

}

