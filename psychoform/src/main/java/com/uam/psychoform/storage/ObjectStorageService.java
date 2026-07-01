package com.uam.psychoform.storage;

public interface ObjectStorageService {
    StoredObject putObject(String key, String contentType, byte[] bytes);

    void deleteObject(String key);

    byte[] getObject(String key);
}
