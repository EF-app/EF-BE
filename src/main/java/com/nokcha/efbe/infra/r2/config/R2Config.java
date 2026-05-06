package com.nokcha.efbe.infra.r2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${cloud.r2.region:auto}")
    private String region;

    @Value("${cloud.r2.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${cloud.r2.access-key:dev-key}")
    private String accessKey;

    @Value("${cloud.r2.secret-key:dev-secret}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        String resolvedRegion = (region == null || region.isBlank()) ? "auto" : region;
        String resolvedAccessKey = (accessKey == null || accessKey.isBlank()) ? "dev-key" : accessKey;
        String resolvedSecretKey = (secretKey == null || secretKey.isBlank()) ? "dev-secret" : secretKey;

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(resolvedRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(resolvedAccessKey, resolvedSecretKey)
                ));
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
