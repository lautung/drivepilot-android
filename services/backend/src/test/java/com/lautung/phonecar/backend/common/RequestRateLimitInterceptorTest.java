package com.lautung.phonecar.backend.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestRateLimitInterceptorTest {
    @Test
    void repeatedAuthRequests_returnStableRateLimitContract() {
        RateLimitProperties.Rule onePerMinute = new RateLimitProperties.Rule(1, Duration.ofMinutes(1));
        RequestRateLimitInterceptor interceptor = new RequestRateLimitInterceptor(new RateLimitProperties(
                onePerMinute,
                onePerMinute,
                Duration.ofMinutes(15),
                100,
                List.of("127.0.0.1")));
        MockHttpServletRequest first = request("10.0.0.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        interceptor.preHandle(first, firstResponse, new Object());

        MockHttpServletRequest second = request("10.0.0.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        ApiException exception = assertThrows(ApiException.class,
                () -> interceptor.preHandle(second, secondResponse, new Object()));

        assertEquals(429, exception.status().value());
        assertEquals("RATE_LIMITED", exception.code());
        assertTrue(Long.parseLong(secondResponse.getHeader("Retry-After")) >= 1L);
    }

    @Test
    void forwardedAddress_isTrustedOnlyFromConfiguredProxy() {
        RateLimitProperties.Rule onePerMinute = new RateLimitProperties.Rule(1, Duration.ofMinutes(1));
        RequestRateLimitInterceptor interceptor = new RequestRateLimitInterceptor(new RateLimitProperties(
                onePerMinute, onePerMinute, Duration.ofMinutes(15), 100, List.of("127.0.0.1")));
        MockHttpServletRequest trusted = request("198.51.100.1");
        trusted.setRemoteAddr("127.0.0.1");
        interceptor.preHandle(trusted, new MockHttpServletResponse(), new Object());
        MockHttpServletRequest untrusted = request("198.51.100.1");
        untrusted.setRemoteAddr("192.0.2.10");
        interceptor.preHandle(untrusted, new MockHttpServletResponse(), new Object());
    }

    private MockHttpServletRequest request(String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", forwardedFor);
        return request;
    }
}
