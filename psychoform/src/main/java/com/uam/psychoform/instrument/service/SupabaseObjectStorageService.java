package com.uam.psychoform.instrument.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SupabaseObjectStorageService {
    private static final long MAX_IMAGE_BYTES = 25L * 1024L * 1024L;
    private static final String ALLOWED_MIME_TYPES = "[\"image/png\",\"image/jpeg\",\"image/webp\"]";
    private final HttpClient http;
    private final String storageUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final boolean createBucket;
    private volatile boolean bucketChecked;

    public SupabaseObjectStorageService(@Value("${bfa.storage.supabase.url:}") String supabaseUrl,
            @Value("${bfa.storage.supabase.service-role-key:}") String serviceRoleKey,
            @Value("${bfa.storage.supabase.bucket:bfa-item-images}") String bucket,
            @Value("${bfa.storage.supabase.create-bucket:true}") boolean createBucket) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.storageUrl = normalizeStorageUrl(supabaseUrl);
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.bucket = bucket == null || bucket.isBlank() ? "bfa-item-images" : bucket.trim();
        this.createBucket = createBucket;
    }

    @PostConstruct
    void validateConfiguration() {
        if (storageUrl.isBlank() || serviceRoleKey.isBlank()) {
            return;
        }
        if (!storageUrl.endsWith("/storage/v1")) {
            throw new IllegalStateException("bfa.storage.supabase.url debe ser URL del proyecto Supabase o /storage/v1");
        }
    }

    public void upload(String objectPath, byte[] bytes, String contentType) {
        requireConfigured();
        ensureBucket();
        HttpRequest request = baseRequest("/object/" + encode(bucket) + "/" + encodePath(objectPath))
                .header("Content-Type", contentType)
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        sendExpectingSuccess(request, "subir objeto a Supabase Storage");
    }

    public StoredObject download(String objectPath, String fileName, String contentType) {
        requireConfigured();
        ensureBucket();
        HttpRequest request = baseRequest("/object/authenticated/" + encode(bucket) + "/" + encodePath(objectPath))
                .GET()
                .build();
        HttpResponse<byte[]> response = sendBytes(request, "leer objeto de Supabase Storage");
        return new StoredObject(fileName, contentType, response.body());
    }

    public void delete(String objectPath) {
        requireConfigured();
        ensureBucket();
        String body = "{\"prefixes\":[\"" + json(objectPath) + "\"]}";
        HttpRequest request = baseRequest("/object/" + encode(bucket))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(body))
                .build();
        sendExpectingSuccess(request, "eliminar objeto de Supabase Storage");
    }

    private void ensureBucket() {
        if (bucketChecked) {
            return;
        }
        synchronized (this) {
            if (bucketChecked) {
                return;
            }
            HttpRequest get = baseRequest("/bucket/" + encode(bucket)).GET().build();
            HttpResponse<String> response = sendString(get);
            if (isBucketNotFound(response) && createBucket) {
                String body = bucketBody();
                HttpRequest create = baseRequest("/bucket")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                sendExpectingSuccess(create, "crear bucket de Supabase Storage");
            } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw storageException("verificar bucket de Supabase Storage", response.statusCode(), response.body());
            } else {
                HttpRequest update = baseRequest("/bucket/" + encode(bucket))
                        .header("Content-Type", "application/json")
                        .method("PUT", HttpRequest.BodyPublishers.ofString(bucketBody()))
                        .build();
                sendExpectingSuccess(update, "actualizar bucket de Supabase Storage");
            }
            bucketChecked = true;
        }
    }

    private String bucketBody() {
        return "{\"id\":\"" + json(bucket) + "\",\"name\":\"" + json(bucket)
                + "\",\"public\":false,\"file_size_limit\":" + MAX_IMAGE_BYTES
                + ",\"allowed_mime_types\":" + ALLOWED_MIME_TYPES + "}";
    }

    private static boolean isBucketNotFound(HttpResponse<String> response) {
        return response.statusCode() == 404
                || (response.statusCode() == 400 && response.body() != null
                        && response.body().toLowerCase().contains("bucket not found"));
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder(URI.create(storageUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey);
    }

    private void requireConfigured() {
        if (storageUrl.isBlank() || serviceRoleKey.isBlank()) {
            throw new IllegalStateException("Supabase Storage no configurado. Defina BFA_SUPABASE_URL y BFA_SUPABASE_SERVICE_ROLE_KEY.");
        }
    }

    private void sendExpectingSuccess(HttpRequest request, String action) {
        HttpResponse<String> response = sendString(request);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw storageException(action, response.statusCode(), response.body());
        }
    }

    private HttpResponse<String> sendString(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo conectar con Supabase Storage", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operacion de Supabase Storage interrumpida", ex);
        }
    }

    private HttpResponse<byte[]> sendBytes(HttpRequest request, String action) {
        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw storageException(action, response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
            }
            return response;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo conectar con Supabase Storage", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operacion de Supabase Storage interrumpida", ex);
        }
    }

    private static IllegalStateException storageException(String action, int status, String body) {
        return new IllegalStateException("No se pudo " + action + " (HTTP " + status + "): " + body);
    }

    private static String normalizeStorageUrl(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isBlank()) {
            return "";
        }
        String noSlash = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        return noSlash.endsWith("/storage/v1") ? noSlash : noSlash + "/storage/v1";
    }

    private static String encodePath(String path) {
        return java.util.Arrays.stream(path.split("/"))
                .map(SupabaseObjectStorageService::encode)
                .reduce((left, right) -> left + "/" + right)
                .orElse("");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String json(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record StoredObject(String fileName, String mimeType, byte[] bytes) {
    }
}
