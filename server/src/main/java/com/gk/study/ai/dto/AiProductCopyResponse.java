package com.gk.study.ai.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AiProductCopyResponse {
    private String title;
    private List<String> highlights = new ArrayList<>();
    private String description;
    private Map<String, String> specs = new LinkedHashMap<>();
    private ClassificationSuggestion classificationSuggestion;
    private List<String> tagSuggestions = new ArrayList<>();
    private List<String> seoKeywords = new ArrayList<>();
    private List<String> riskTips = new ArrayList<>();
    private Boolean fallback;

    @Data
    public static class ClassificationSuggestion {
        private Long id;
        private String title;
        private Double confidence;
    }
}
