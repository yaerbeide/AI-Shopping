package com.gk.study.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String save(String folder, MultipartFile file) throws IOException;
}
