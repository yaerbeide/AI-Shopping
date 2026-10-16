package com.gk.study.ai.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiProductImageResponse {
    private List<ImageItem> images = new ArrayList<>();
    private List<String> riskTips = new ArrayList<>();
    private Boolean fallback;

    @Data
    public static class ImageItem {
        private String url;
        private String base64;
        private String prompt;
        private String style;
    }
}
