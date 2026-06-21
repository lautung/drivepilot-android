package com.lautung.phonecar.backend.media;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("phonecar.minio")
public record MinioProperties(String endpoint, String publicEndpoint, String accessKey, String secretKey,
                              String bucket, String region, Duration presignedUrlTtl) {}
