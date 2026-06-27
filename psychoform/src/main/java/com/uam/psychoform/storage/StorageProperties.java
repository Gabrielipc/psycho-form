package com.uam.psychoform.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record StorageProperties(String endpoint, String region, String bucket, String accessKey, String secretKey,
        boolean pathStyle) {
}
