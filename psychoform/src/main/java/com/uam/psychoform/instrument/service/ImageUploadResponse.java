package com.uam.psychoform.instrument.service;

public record ImageUploadResponse(Long imageLinkId, Long resourceId, String storagePath, String filename,
        String mimeType, Long sizeBytes, String hash, Integer order, String altText, String role) {
}
