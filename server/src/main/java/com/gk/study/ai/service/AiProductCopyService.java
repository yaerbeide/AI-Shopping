package com.gk.study.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gk.study.ai.client.AiModelClient;
import com.gk.study.ai.dto.AiChatMessage;
import com.gk.study.ai.dto.AiProductCopyRequest;
import com.gk.study.ai.dto.AiProductCopyResponse;
import com.gk.study.entity.Classification;
import com.gk.study.entity.Tag;
import com.gk.study.mapper.ClassificationMapper;
import com.gk.study.mapper.TagMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiProductCopyService {
    private static final Logger logger = LoggerFactory.getLogger(AiProductCopyService.class);

    private final Gson gson = new Gson();

    @Autowired
    private AiModelClient aiModelClient;

    @Autowired
    private ClassificationMapper classificationMapper;

    @Autowired
    private TagMapper tagMapper;

    public AiProductCopyResponse generate(AiProductCopyRequest request) {
        List<Classification> classifications = classificationMapper.selectList(new QueryWrapper<>());
        List<Tag> tags = tagMapper.selectList(new QueryWrapper<>());

        if (!aiModelClient.available()) {
            return fallback(request, classifications, tags);
        }

        try {
            String prompt = buildPrompt(request, classifications, tags);
            String content = aiModelClient.chat(Arrays.asList(
                    new AiChatMessage("system", "你是严谨的电商商品上架文案助手，只返回 JSON。"),
                    new AiChatMessage("user", prompt)
            ));
            AiProductCopyResponse response = parseFlexibleResponse(content);
            normalize(response, request, classifications, tags);
            response.setFallback(false);
            return response;
        } catch (Exception ex) {
            logger.warn("AI product copy generation failed, using fallback. error={}", ex.getMessage());
            return fallback(request, classifications, tags);
        }
    }

    private String buildPrompt(AiProductCopyRequest request, List<Classification> classifications, List<Tag> tags) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("productName", request.getProductName());
        input.put("keywords", request.getKeywords());
        input.put("price", request.getPrice());
        input.put("classificationId", request.getClassificationId());
        input.put("description", request.getDescription());
        input.put("classifications", toClassificationOptions(classifications));
        input.put("existingTags", toTagOptions(tags));

        return "请根据商品基础信息生成电商上架文案。要求：\n"
                + "1. 必须返回 JSON，不要输出 Markdown 或解释。\n"
                + "2. 不得编造无法确认的硬件参数，未知参数写“以实际商品参数为准”。\n"
                + "3. 不得使用全网最低、绝对第一、永久保修等夸张或违规表达。\n"
                + "4. 标题控制在 20-45 个中文字符。\n"
                + "5. 卖点 3-5 条，每条不超过 32 个中文字符。\n"
                + "6. 分类必须从 classifications 中选择，返回 id、title、confidence。\n"
                + "7. 标签优先从 existingTags 中选择，可补充少量建议标签。\n"
                + "8. 返回字段：title, highlights, description, specs, classificationSuggestion, tagSuggestions, seoKeywords, riskTips。\n"
                + "商品基础信息 JSON：\n" + gson.toJson(input);
    }

    private AiProductCopyResponse parseFlexibleResponse(String content) {
        JsonObject root = JsonParser.parseString(extractJsonObject(content)).getAsJsonObject();
        AiProductCopyResponse response = new AiProductCopyResponse();
        response.setTitle(getString(root, "title"));
        response.setDescription(getString(root, "description"));
        response.setHighlights(getStringList(root.get("highlights")));
        response.setTagSuggestions(getStringList(root.get("tagSuggestions")));
        response.setSeoKeywords(getStringList(root.get("seoKeywords")));
        response.setRiskTips(getStringList(root.get("riskTips")));

        JsonElement specsElement = root.get("specs");
        if (specsElement != null && specsElement.isJsonObject()) {
            Map<String, String> specs = new LinkedHashMap<>();
            JsonObject specsObject = specsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : specsObject.entrySet()) {
                specs.put(entry.getKey(), elementToText(entry.getValue()));
            }
            response.setSpecs(specs);
        }

        JsonElement classificationElement = root.get("classificationSuggestion");
        if (classificationElement != null && classificationElement.isJsonObject()) {
            JsonObject item = classificationElement.getAsJsonObject();
            AiProductCopyResponse.ClassificationSuggestion suggestion = new AiProductCopyResponse.ClassificationSuggestion();
            if (item.has("id") && !item.get("id").isJsonNull()) {
                try {
                    suggestion.setId(item.get("id").getAsLong());
                } catch (Exception ignored) {
                }
            }
            suggestion.setTitle(getString(item, "title"));
            if (item.has("confidence") && !item.get("confidence").isJsonNull()) {
                try {
                    suggestion.setConfidence(item.get("confidence").getAsDouble());
                } catch (Exception ignored) {
                }
            }
            response.setClassificationSuggestion(suggestion);
        }

        return response;
    }

    private String getString(JsonObject object, String name) {
        if (object == null || !object.has(name) || object.get(name).isJsonNull()) {
            return null;
        }
        return elementToText(object.get(name));
    }

    private List<String> getStringList(JsonElement element) {
        List<String> result = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return result;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                addSplitValues(result, elementToText(item));
            }
            return result;
        }
        addSplitValues(result, elementToText(element));
        return result;
    }

    private void addSplitValues(List<String> result, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        for (String item : value.split("[,，、;；\\n]+")) {
            if (StringUtils.isNotBlank(item)) {
                result.add(item.trim());
            }
        }
    }

    private String elementToText(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return element.toString();
    }

    private void normalize(AiProductCopyResponse response, AiProductCopyRequest request, List<Classification> classifications, List<Tag> tags) {
        if (response == null) {
            throw new IllegalStateException("empty AI response");
        }
        if (StringUtils.isBlank(response.getTitle())) {
            response.setTitle(defaultTitle(request));
        }
        if (response.getHighlights() == null || response.getHighlights().isEmpty()) {
            response.setHighlights(defaultHighlights(request));
        }
        if (StringUtils.isBlank(response.getDescription())) {
            response.setDescription(defaultDescription(request));
        }
        if (response.getSpecs() == null) {
            response.setSpecs(defaultSpecs(request));
        }
        if (response.getSeoKeywords() == null) {
            response.setSeoKeywords(defaultSeoKeywords(request));
        }
        if (response.getRiskTips() == null) {
            response.setRiskTips(new ArrayList<>());
        }
        normalizeClassification(response, request, classifications);
        if (response.getTagSuggestions() == null || response.getTagSuggestions().isEmpty()) {
            response.setTagSuggestions(defaultTags(request, tags));
        }
    }

    private void normalizeClassification(AiProductCopyResponse response, AiProductCopyRequest request, List<Classification> classifications) {
        AiProductCopyResponse.ClassificationSuggestion suggestion = response.getClassificationSuggestion();
        Classification matched = null;
        if (suggestion != null && suggestion.getId() != null) {
            for (Classification item : classifications) {
                if (suggestion.getId().equals(item.getId())) {
                    matched = item;
                    break;
                }
            }
        }
        if (matched == null && request.getClassificationId() != null) {
            for (Classification item : classifications) {
                if (request.getClassificationId().equals(item.getId())) {
                    matched = item;
                    break;
                }
            }
        }
        if (matched == null && !classifications.isEmpty()) {
            matched = classifications.get(0);
        }
        if (matched != null) {
            AiProductCopyResponse.ClassificationSuggestion normalized = new AiProductCopyResponse.ClassificationSuggestion();
            normalized.setId(matched.getId());
            normalized.setTitle(matched.getTitle());
            normalized.setConfidence(suggestion != null && suggestion.getConfidence() != null ? suggestion.getConfidence() : 0.7);
            response.setClassificationSuggestion(normalized);
        }
    }

    private AiProductCopyResponse fallback(AiProductCopyRequest request, List<Classification> classifications, List<Tag> tags) {
        AiProductCopyResponse response = new AiProductCopyResponse();
        response.setTitle(defaultTitle(request));
        response.setHighlights(defaultHighlights(request));
        response.setDescription(defaultDescription(request));
        response.setSpecs(defaultSpecs(request));
        response.setSeoKeywords(defaultSeoKeywords(request));
        response.setTagSuggestions(defaultTags(request, tags));
        response.setRiskTips(Arrays.asList("当前为规则兜底文案，请人工核对商品参数。"));
        response.setFallback(true);
        normalizeClassification(response, request, classifications);
        return response;
    }

    private String defaultTitle(AiProductCopyRequest request) {
        String name = StringUtils.defaultIfBlank(request.getProductName(), "精选商品");
        String keywords = StringUtils.defaultString(request.getKeywords()).replace("，", " ").replace(",", " ").trim();
        if (StringUtils.isBlank(keywords)) {
            return name;
        }
        return name + " " + keywords;
    }

    private List<String> defaultHighlights(AiProductCopyRequest request) {
        List<String> list = new ArrayList<>();
        list.add("适合日常使用场景");
        list.add("品质稳定，购买更省心");
        list.add("具体参数以下单页面为准");
        return list;
    }

    private String defaultDescription(AiProductCopyRequest request) {
        String name = StringUtils.defaultIfBlank(request.getProductName(), "这款商品");
        String keywords = StringUtils.defaultString(request.getKeywords());
        return name + "适合有相关需求的用户选购。"
                + (StringUtils.isBlank(keywords) ? "" : "商品关键词包括：" + keywords + "。")
                + "下单前建议核对商品详情、规格参数和库存状态。";
    }

    private Map<String, String> defaultSpecs(AiProductCopyRequest request) {
        Map<String, String> specs = new LinkedHashMap<>();
        specs.put("适用场景", StringUtils.defaultIfBlank(request.getKeywords(), "以实际使用需求为准"));
        specs.put("核心参数", "以实际商品参数为准");
        specs.put("售后说明", "以平台和商家规则为准");
        return specs;
    }

    private List<String> defaultSeoKeywords(AiProductCopyRequest request) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotBlank(request.getProductName())) {
            list.add(request.getProductName());
        }
        for (String keyword : splitKeywords(request.getKeywords())) {
            list.add(keyword);
        }
        if (list.isEmpty()) {
            list.add("精选商品");
        }
        return list;
    }

    private List<String> defaultTags(AiProductCopyRequest request, List<Tag> tags) {
        List<String> result = new ArrayList<>();
        List<String> keywords = splitKeywords(request.getKeywords());
        for (String keyword : keywords) {
            result.add(keyword);
        }
        for (Tag tag : tags) {
            if (result.size() >= 4) break;
            if (tag != null && StringUtils.isNotBlank(tag.getTitle())) {
                result.add(tag.getTitle());
            }
        }
        return result;
    }

    private List<String> splitKeywords(String keywords) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(keywords)) {
            return result;
        }
        for (String item : keywords.split("[,，\\s]+")) {
            if (StringUtils.isNotBlank(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }

    private List<Map<String, Object>> toClassificationOptions(List<Classification> classifications) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Classification item : classifications) {
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("id", item.getId());
            option.put("title", item.getTitle());
            result.add(option);
        }
        return result;
    }

    private List<String> toTagOptions(List<Tag> tags) {
        List<String> result = new ArrayList<>();
        for (Tag item : tags) {
            if (item != null && StringUtils.isNotBlank(item.getTitle())) {
                result.add(item.getTitle());
            }
        }
        return result;
    }

    private String extractJsonObject(String content) {
        if (StringUtils.isBlank(content)) {
            return "{}";
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*", "").replaceFirst("```$", "").trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
