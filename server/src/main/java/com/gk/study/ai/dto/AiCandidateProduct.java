package com.gk.study.ai.dto;

import lombok.Data;

@Data
public class AiCandidateProduct {
    private Long id;
    private String title;
    private String cover;
    private String description;
    private String price;
    private String repertory;
    private String score;
    private String pv;
    private String recommendCount;
    private String wishCount;
    private String collectCount;
    private Long classificationId;
    private String classificationTitle;
    private String profileSummary;
    private String profileKeywords;
    private String profileScenes;
    private String profileTargetUsers;
    private String profileSellingPoints;
    private String profileImageSummary;
    private Integer contentQualityScore;
}
