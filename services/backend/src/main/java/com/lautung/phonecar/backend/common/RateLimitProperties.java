package com.lautung.phonecar.backend.common;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("phonecar.rate-limit")
public record RateLimitProperties(
        Rule auth,
        Rule mediaUpload,
        Duration inactiveEntryTtl,
        int maxEntries,
        List<String> trustedProxyAddresses) {
    public RateLimitProperties {
        if (auth == null || mediaUpload == null) throw new IllegalArgumentException("Rate limit rules are required");
        if (inactiveEntryTtl == null || inactiveEntryTtl.isZero() || inactiveEntryTtl.isNegative()) {
            throw new IllegalArgumentException("Rate limit entry TTL must be positive");
        }
        if (maxEntries < 1) throw new IllegalArgumentException("Rate limit max entries must be positive");
        trustedProxyAddresses = trustedProxyAddresses == null ? List.of() : List.copyOf(trustedProxyAddresses);
    }

    public record Rule(long capacity, Duration refillPeriod) {
        public Rule {
            if (capacity < 1) throw new IllegalArgumentException("Rate limit capacity must be positive");
            if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
                throw new IllegalArgumentException("Rate limit refill period must be positive");
            }
        }
    }
}
