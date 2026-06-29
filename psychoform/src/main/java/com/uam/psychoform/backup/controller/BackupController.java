package com.uam.psychoform.backup.controller;

import com.uam.psychoform.backup.service.BackupService;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.security.SecurityPermissions;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backups")
public class BackupController {
    private final BackupService backups;

    public BackupController(BackupService backups) {
        this.backups = backups;
    }

    @GetMapping
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ApiResponse<?> list() {
        return ApiResponse.ok(backups.list());
    }

    @PostMapping
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ApiResponse<?> generate() {
        return ApiResponse.ok(backups.generate());
    }

    @GetMapping("/{fileName}")
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        BackupService.BackupFile file = backups.download(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .body(file.bytes());
    }

    @PostMapping("/{fileName}/restore-requests")
    @PreAuthorize(SecurityPermissions.REPORTE_EXPORTAR)
    public ApiResponse<?> requestRestore(@PathVariable String fileName) {
        return ApiResponse.ok(backups.requestRestore(fileName));
    }
}
