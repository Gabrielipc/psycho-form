package com.uam.psychoform.storage;

import java.net.URI;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ObjectStorageService implements ObjectStorageService {
    private final StorageProperties properties;

    public S3ObjectStorageService(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredObject putObject(String key, String contentType, byte[] bytes) {
        requireConfigured();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();
        try (S3Client client = client()) {
            return new StoredObject(key, client.putObject(request, RequestBody.fromBytes(bytes)).eTag());
        }
    }

    @Override
    public void deleteObject(String key) {
        requireConfigured();
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        try (S3Client client = client()) {
            client.deleteObject(request);
        }
    }

    private S3Client client() {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .forcePathStyle(properties.pathStyle())
                .build();
    }

    private void requireConfigured() {
        if (blank(properties.endpoint()) || blank(properties.region()) || blank(properties.bucket())
                || blank(properties.accessKey()) || blank(properties.secretKey())) {
            throw new IllegalStateException("Storage S3 no configurado");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
