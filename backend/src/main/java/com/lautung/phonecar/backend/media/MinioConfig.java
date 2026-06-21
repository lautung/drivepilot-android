package com.lautung.phonecar.backend.media;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean
    @Qualifier("internalMinioClient")
    MinioClient internalMinioClient(MinioProperties properties) {
        return client(properties.endpoint(), properties);
    }

    @Bean
    @Qualifier("publicMinioClient")
    MinioClient publicMinioClient(MinioProperties properties) {
        return client(properties.publicEndpoint(), properties);
    }

    private MinioClient client(String endpoint, MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.accessKey(), properties.secretKey())
                .region(properties.region())
                .build();
    }
}
