package com.gk.study.ai.dto;

import com.gk.study.entity.AiProductProfile;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AiProductProfileView {
    private AiProductProfile profile;
    private String summary;
    private List<String> keywords = new ArrayList<>();
    private List<String> scenes = new ArrayList<>();
    private List<String> targetUsers = new ArrayList<>();
    private List<String> sellingPoints = new ArrayList<>();
    private Map<String, String> specs = new LinkedHashMap<>();
    private String imageSummary;
    private Integer contentQualityScore;
    private List<String> guideHints = new ArrayList<>();
    private List<String> avoidReasons = new ArrayList<>();
}
