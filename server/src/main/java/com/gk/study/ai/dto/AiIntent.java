package com.gk.study.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiIntent {
    private List<String> categoryKeywords = new ArrayList<>();
    private List<String> productKeywords = new ArrayList<>();
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String scene;
    private String targetUser;
    private List<String> mustHave = new ArrayList<>();
    private List<String> avoid = new ArrayList<>();
    private String sortPreference;
}
