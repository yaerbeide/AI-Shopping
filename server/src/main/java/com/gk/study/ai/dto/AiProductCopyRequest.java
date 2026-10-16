package com.gk.study.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiProductCopyRequest {
    private String productName;
    private String keywords;
    private BigDecimal price;
    private Long classificationId;
    private String description;
}
