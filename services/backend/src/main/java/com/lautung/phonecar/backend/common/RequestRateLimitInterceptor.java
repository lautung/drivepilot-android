package com.lautung.phonecar.backend.common;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestRateLimitInterceptor implements HandlerInterceptor {
    private static final String AUTH_PREFIX = "/api/v1/auth/";
    private static final String MEDIA_PATH = "/api/v1/admin/media";
    private static final String OVERFLOW_KEY = "__overflow__";
    private final RateLimitProperties properties;
    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();

    public RequestRateLimitInterceptor(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        SelectedRule selected = select(request);
        if (selected == null) return true;
        long now = System.nanoTime();
        if ((requests.incrementAndGet() & 255) == 0) evictInactive(now);
        String key = selected.name() + ':' + rateLimitKey(request, selected);
        if (!buckets.containsKey(key) && buckets.size() >= properties.maxEntries()) {
            evictInactive(now);
            if (buckets.size() >= properties.maxEntries()) key = selected.name() + ':' + OVERFLOW_KEY;
        }
        String bucketKey = key;
        Entry entry = buckets.computeIfAbsent(bucketKey, ignored -> new Entry(bucket(selected.rule()), now));
        entry.lastAccessNanos = now;
        ConsumptionProbe probe = entry.bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) return true;
        long retryAfterSeconds = Math.max(1L,
                Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1L);
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
                "Too many requests; retry later");
    }

    private SelectedRule select(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) return null;
        String path = request.getRequestURI();
        if (path.startsWith(AUTH_PREFIX)) return new SelectedRule("auth", properties.auth());
        if (MEDIA_PATH.equals(path)) return new SelectedRule("media", properties.mediaUpload());
        return null;
    }

    private Bucket bucket(RateLimitProperties.Rule rule) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillGreedy(rule.capacity(), rule.refillPeriod())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientAddress(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (properties.trustedProxyAddresses() == null
                || !properties.trustedProxyAddresses().contains(remote)) return remote;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) return remote;
        return forwarded.split(",", 2)[0].trim();
    }

    private String rateLimitKey(HttpServletRequest request, SelectedRule selected) {
        if ("media".equals(selected.name()) && request.getUserPrincipal() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }
        return "ip:" + clientAddress(request);
    }

    private void evictInactive(long now) {
        long ttl = properties.inactiveEntryTtl().toNanos();
        buckets.entrySet().removeIf(entry -> now - entry.getValue().lastAccessNanos > ttl);
    }

    private static final class Entry {
        private final Bucket bucket;
        private volatile long lastAccessNanos;
        private Entry(Bucket bucket, long lastAccessNanos) {
            this.bucket = bucket;
            this.lastAccessNanos = lastAccessNanos;
        }
    }

    private record SelectedRule(String name, RateLimitProperties.Rule rule) {}
}
