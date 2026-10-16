package com.gk.study.service.impl;

import com.gk.study.service.FileStorageService;
import com.gk.study.utils.UploadPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${File.uploadPath:}")
    private String uploadPath;

    @Override
    public String save(String folder, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (!isAllowedFolder(folder)) {
            throw new IOException("Unsupported upload folder: " + folder);
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null) {
            int idx = originalName.lastIndexOf('.');
            if (idx >= 0 && idx < originalName.length() - 1) {
                ext = originalName.substring(idx);
                if (ext.length() > 16) {
                    ext = "";
                }
            }
        }

        String newFileName = UUID.randomUUID().toString() + ext;
        Path destPath = UploadPathUtils.resolveFilePath(uploadPath, folder, newFileName);
        Files.createDirectories(destPath.getParent());
        file.transferTo(destPath.toFile());
        return newFileName;
    }

    private boolean isAllowedFolder(String folder) {
        return "image".equals(folder) || "avatar".equals(folder) || "banner".equals(folder);
    }
}
