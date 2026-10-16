package com.gk.study.ai.client;

import com.google.gson.Gson;
import com.gk.study.ai.dto.AiChatMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiModelClient {
    private final Gson gson = new Gson();

    @Value("${ai.enabled:false}")
    private Boolean enabled;

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    @Value("${ai.temperature:0.2}")
    private Double temperature;

    @Value("${ai.timeout-seconds:30}")
    private Integer timeoutSeconds;

    @Value("${ai.max-tokens:800}")
    private Integer maxTokens;

    @Value("${ai.vision.enabled:false}")
    private Boolean visionEnabled;

    @Value("${ai.vision.base-url:}")
    private String visionBaseUrl;

    @Value("${ai.vision.api-key:}")
    private String visionApiKey;

    @Value("${ai.vision.model:}")
    private String visionModel;

    public boolean available() {
        return primaryAvailable() || visionFallbackAvailable();
    }

    private boolean primaryAvailable() {
        return Boolean.TRUE.equals(enabled)
                && StringUtils.isNotBlank(baseUrl)
                && StringUtils.isNotBlank(apiKey)
                && StringUtils.isNotBlank(model);
    }

    private boolean visionFallbackAvailable() {
        return Boolean.TRUE.equals(enabled)
                && Boolean.TRUE.equals(visionEnabled)
                && StringUtils.isNotBlank(visionBaseUrl)
                && StringUtils.isNotBlank(visionApiKey)
                && StringUtils.isNotBlank(visionModel);
    }

    public String chat(List<AiChatMessage> messages) {
        if (!available()) {
            throw new IllegalStateException("AI model API is not configured");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", resolveModel());
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resolveApiKey());

        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(body), headers);
        ResponseEntity<Map> response = buildRestTemplate().postForEntity(buildChatUrl(), entity, Map.class);
        return readContent(response.getBody());
    }

    private RestTemplate buildRestTemplate() {
        int timeout = timeoutSeconds == null ? 30000 : timeoutSeconds * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    private String buildChatUrl() {
        String url = resolveBaseUrl();
        String normalized = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return normalized + "/chat/completions";
    }

    private String resolveBaseUrl() {
        return primaryAvailable() ? baseUrl : visionBaseUrl;
    }

    private String resolveApiKey() {
        return primaryAvailable() ? apiKey : visionApiKey;
    }

    private String resolveModel() {
        return primaryAvailable() ? model : visionModel;
    }

    private String readContent(Map body) {
        if (body == null || !(body.get("choices") instanceof List)) {
            throw new IllegalStateException("AI model response has no choices");
        }
        List choices = (List) body.get("choices");
        if (choices.isEmpty() || !(choices.get(0) instanceof Map)) {
            throw new IllegalStateException("AI model response choices are empty");
        }
        Map first = (Map) choices.get(0);
        Object message = first.get("message");
        if (message instanceof Map) {
            Object content = ((Map) message).get("content");
            if (content != null) {
                return String.valueOf(content);
            }
        }
        Object text = first.get("text");
        if (text != null) {
            return String.valueOf(text);
        }
        throw new IllegalStateException("AI model response has no content");
    }
}
