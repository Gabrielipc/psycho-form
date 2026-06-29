package com.uam.psychoform.instrument.controller;

import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.instrument.service.ItemImageStorageService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ItemImageController {
    private final ItemImageStorageService storage;

    public ItemImageController(ItemImageStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/items/{itemId}/images")
    public ApiResponse<?> list(@PathVariable Long itemId) {
        return ApiResponse.ok(storage.listImages(itemId));
    }

    @GetMapping("/tests/{testId}/images")
    public ApiResponse<?> listByTest(@PathVariable Long testId) {
        return ApiResponse.ok(storage.listImagesByTest(testId));
    }

    @GetMapping("/subtests/{subtestId}/images")
    public ApiResponse<?> listBySubtest(@PathVariable Long subtestId) {
        return ApiResponse.ok(storage.listImagesBySubtest(subtestId));
    }

    @PostMapping(path = "/items/{itemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> upload(@PathVariable Long itemId, @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "ITEM") @NotBlank String role,
            @RequestParam(required = false) Integer order,
            @RequestParam(required = false) String altText) {
        return ApiResponse.ok(storage.upload(itemId, file, role, order, altText));
    }

    @GetMapping("/items/images/resources/{resourceId}")
    public ResponseEntity<byte[]> resource(@PathVariable Long resourceId) {
        var file = storage.readResource(resourceId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.bytes());
    }

    @DeleteMapping("/items/images/{imageId}")
    public ApiResponse<Void> delete(@PathVariable Long imageId) {
        storage.deleteImage(imageId);
        return ApiResponse.ok(null);
    }
}
