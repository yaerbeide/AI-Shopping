package com.gk.study.ai.dto;

import com.gk.study.entity.Thing;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiGuideResponse {
    private String answer;
    private AiIntent intent;
    private List<Recommendation> recommendations = new ArrayList<>();
    private List<String> nextQuestions = new ArrayList<>();
    private Boolean fallback;

    @Data
    public static class Recommendation {
        private Long thingId;
        private String reason;
        private String risk;
        private Thing thing;
    }
}
