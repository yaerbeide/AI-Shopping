package com.gk.study.config;

import com.gk.study.utils.UploadPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UploadPathStartupLogger implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UploadPathStartupLogger.class);

    @Value("${File.uploadPath:}")
    private String uploadPath;

    @Override
    public void run(ApplicationArguments args) {
        String root = UploadPathUtils.resolveUploadRoot(uploadPath);
        logger.info("Resolved upload root: {}", root);
    }
}
