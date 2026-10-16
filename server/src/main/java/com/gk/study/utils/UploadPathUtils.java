package com.gk.study.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class UploadPathUtils {

    private UploadPathUtils() {
    }

    public static String resolveUploadRoot(String configuredUploadPath) {
        List<Path> candidates = new ArrayList<>();
        String userDir = System.getProperty("user.dir");

        if (configuredUploadPath != null && !configuredUploadPath.trim().isEmpty()) {
            candidates.add(Paths.get(configuredUploadPath).toAbsolutePath().normalize());
        }
        candidates.add(Paths.get(userDir, "server", "upload").toAbsolutePath().normalize());
        candidates.add(Paths.get(userDir, "upload").toAbsolutePath().normalize());

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        return candidates.get(0).toString();
    }

    public static String resolveResourceLocation(String configuredUploadPath, String folderName) {
        String uri = Paths.get(resolveUploadRoot(configuredUploadPath), folderName)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        if (uri.endsWith("/")) {
            return uri;
        }
        return uri + "/";
    }

    public static Path resolveFilePath(String configuredUploadPath, String folderName, String fileName) {
        return Paths.get(resolveUploadRoot(configuredUploadPath), folderName, fileName)
                .toAbsolutePath()
                .normalize();
    }
}
