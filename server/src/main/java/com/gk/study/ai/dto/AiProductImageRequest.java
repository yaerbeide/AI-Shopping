package com.gk.study.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AiProductImageRequest {
    private String productName;
    private String keywords;
    private BigDecimal price;
    private Long classificationId;
    private String classificationTitle;
    private String description;
    private List<String> highlights;
    private Map<String, String> specs;
    private String usage;
    private String style;
    private String ratio;
    private Integer count;
}
