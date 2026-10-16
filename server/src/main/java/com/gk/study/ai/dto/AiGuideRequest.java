package com.gk.study.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiGuideRequest {
    private Long userId;
    private String query;
    private Long currentThingId;
    private List<CartItem> cartItems;

    @Data
    public static class CartItem {
        private Long thingId;
        private Integer quantity;
    }
}
