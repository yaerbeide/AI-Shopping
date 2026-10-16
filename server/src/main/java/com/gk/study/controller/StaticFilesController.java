package com.gk.study.controller;

import com.gk.study.utils.UploadPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
public class StaticFilesController {

    @Value("${File.uploadPath:}")
    private String uploadPath;

    @GetMapping("/staticfiles/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/staticfiles/{folder}/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String folder, @PathVariable String filename) {
        if (!isAllowedFolder(folder)) {
            return ResponseEntity.notFound().build();
        }
        if (!isSafeFileName(filename)) {
            return ResponseEntity.notFound().build();
        }

        Path path = UploadPathUtils.resolveFilePath(uploadPath, folder, filename);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }

        UrlResource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(resource);
    }

    private boolean isAllowedFolder(String folder) {
        return "image".equals(folder) || "avatar".equals(folder) || "banner".equals(folder);
    }

    private boolean isSafeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\") || filename.contains("\0")) {
            return false;
        }
        return true;
    }
}
