package com.lautung.phonecar.backend.media;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "phonecar.minio.initialize-bucket", havingValue = "true", matchIfMissing = true)
public class MinioBucketInitializer implements ApplicationRunner {
    private final MinioClient client;
    private final MinioProperties properties;

    public MinioBucketInitializer(
            @Qualifier("internalMinioClient") MinioClient client,
            MinioProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        BucketExistsArgs exists = BucketExistsArgs.builder().bucket(properties.bucket()).build();
        if (!client.bucketExists(exists)) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
        }
    }
}
