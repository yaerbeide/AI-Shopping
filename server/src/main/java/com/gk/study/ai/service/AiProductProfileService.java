package com.gk.study.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gk.study.ai.client.AiModelClient;
import com.gk.study.ai.client.AiVisionModelClient;
import com.gk.study.ai.dto.AiChatMessage;
import com.gk.study.ai.dto.AiProductProfileView;
import com.gk.study.entity.AiProductProfile;
import com.gk.study.entity.Classification;
import com.gk.study.entity.Tag;
import com.gk.study.entity.Thing;
import com.gk.study.entity.ThingTag;
import com.gk.study.mapper.AiProductProfileMapper;
import com.gk.study.mapper.ClassificationMapper;
import com.gk.study.mapper.TagMapper;
import com.gk.study.mapper.ThingMapper;
import com.gk.study.mapper.ThingTagMapper;
import com.gk.study.utils.UploadPathUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiProductProfileService {
    private static final Logger logger = LoggerFactory.getLogger(AiProductProfileService.class);
    private final Gson gson = new Gson();

    @Autowired
    private AiModelClient aiModelClient;

    @Autowired
    private AiVisionModelClient aiVisionModelClient;

    @Autowired
    private AiProductProfileMapper aiProductProfileMapper;

    @Autowired
    private ThingMapper thingMapper;

    @Autowired
    private ClassificationMapper classificationMapper;

    @Autowired
    private ThingTagMapper thingTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @Value("${File.uploadPath:}")
    private String uploadPath;

    @Value("${ai.vision.public-image-base-url:}")
    private String publicImageBaseUrl;

    @Value("${ai.vision.prefer-base64:true}")
    private Boolean preferBase64;

    public AiProductProfileView getView(Long thingId) {
        AiProductProfile profile = getByThingId(thingId);
        if (profile == null) {
            return null;
        }
        return toView(profile);
    }

    public AiProductProfileView generate(Long thingId, boolean force) {
        if (thingId == null) {
            throw new IllegalArgumentException("thingId is required");
        }
        Thing thing = thingMapper.selectById(thingId);
        if (thing == null) {
            throw new IllegalArgumentException("Product does not exist");
        }

        String sourceHash = buildSourceHash(thing);
        AiProductProfile existing = getByThingId(thingId);
        if (!force && existing != null && StringUtils.equals(sourceHash, existing.getSourceHash())) {
            return toView(existing);
        }

        AiProductProfile profile = existing == null ? new AiProductProfile() : existing;
        profile.setThingId(thingId);
        profile.setSourceHash(sourceHash);
        profile.setStatus("READY");
        profile.setErrorMessage(null);

        try {
            JsonObject generated = generateByBestAvailableModel(thing);
            fillProfile(profile, generated);
        } catch (Exception ex) {
            profile.setStatus("FALLBACK");
            profile.setErrorMessage(StringUtils.left(ex.getMessage(), 900));
            fillProfile(profile, fallbackProfile(thing));
        }

        long now = System.currentTimeMillis();
        profile.setGeneratedAt(String.valueOf(now));
        profile.setUpdateTime(String.valueOf(now));
        if (profile.getId() == null) {
            profile.setCreateTime(String.valueOf(now));
            aiProductProfileMapper.insert(profile);
        } else {
            aiProductProfileMapper.updateById(profile);
        }
        return toView(profile);
    }

    private JsonObject generateByBestAvailableModel(Thing thing) {
        if (aiVisionModelClient.available()) {
            try {
                JsonObject generated = generateByVisionModel(thing);
                if (generated != null) {
                    return generated;
                }
            } catch (Exception ex) {
                logger.warn("AI vision product profile failed, falling back to text model. thingId={}, error={}",
                        thing.getId(), ex.getMessage());
            }
        }
        if (aiModelClient.available()) {
            return generateByModel(thing);
        }
        return fallbackProfile(thing);
    }

    public AiProductProfile getByThingId(Long thingId) {
        if (thingId == null) {
            return null;
        }
        QueryWrapper<AiProductProfile> wrapper = new QueryWrapper<>();
        wrapper.eq("thing_id", thingId);
        wrapper.last("limit 1");
        return aiProductProfileMapper.selectOne(wrapper);
    }

    private JsonObject generateByVisionModel(Thing thing) {
        Map<String, Object> imagePart = buildImageContentPart(thing);
        if (imagePart == null) {
            throw new IllegalStateException("Product image is empty or cannot be resolved");
        }

        Map<String, Object> input = buildProductInput(thing);
        String prompt = "请结合商品基础信息和商品图片，为这个商城商品生成 AI 导购档案，用于后续商品召回和推荐。要求：\n"
                + "1. 只返回 JSON，不要 Markdown。\n"
                + "2. 必须先识别图片中的商品品类、外观、可见文字、规格线索、海报卖点，再结合商品基础信息生成档案。\n"
                + "3. 不要编造图片和商品资料都无法支持的硬件参数；不确定的内容写入 guideHints 或 avoidReasons。\n"
                + "4. 重点补齐导购可用信息：适用场景、目标人群、核心卖点、需求关键词、购买提醒。\n"
                + "5. 字段：summary, keywords, scenes, targetUsers, sellingPoints, specs, imageSummary, contentQualityScore, guideHints, avoidReasons。\n"
                + "6. imageSummary 必须描述你从图片中实际看到的信息。\n"
                + "7. keywords 用中文短词，包含同义词、场景词和用户自然语言可能搜索的词。\n"
                + "商品基础信息 JSON：\n" + gson.toJson(input);

        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);
        content.add(textPart);
        content.add(imagePart);

        String result = aiVisionModelClient.chat(Arrays.asList(
                new AiChatMessage("system", "你是电商商品视觉识别与 AI 档案生成助手，只返回可解析 JSON。"),
                new AiChatMessage("user", content)
        ));
        return JsonParser.parseString(extractJsonObject(result)).getAsJsonObject();
    }

    private JsonObject generateByModel(Thing thing) {
        Map<String, Object> input = buildProductInput(thing);
        String prompt = "请为这个商城商品生成 AI 导购档案，用于后续商品召回和推荐。要求：\n"
                + "1. 只返回 JSON，不要 Markdown。\n"
                + "2. 不要编造无法从商品名称、分类、标签、价格、简介推断的硬件参数。\n"
                + "3. 重点补齐导购可用信息：适用场景、目标人群、核心卖点、需求关键词、购买提醒。\n"
                + "4. 字段：summary, keywords, scenes, targetUsers, sellingPoints, specs, imageSummary, contentQualityScore, guideHints, avoidReasons。\n"
                + "5. keywords 用中文短词，包含同义词和场景词，例如：送礼、办公、学生、轻薄、拍照、家用。\n"
                + "商品输入 JSON：\n" + gson.toJson(input);
        String content = aiModelClient.chat(Arrays.asList(
                new AiChatMessage("system", "你是电商商品 AI 档案生成助手，只返回可解析 JSON。"),
                new AiChatMessage("user", prompt)
        ));
        return JsonParser.parseString(extractJsonObject(content)).getAsJsonObject();
    }

    private Map<String, Object> buildProductInput(Thing thing) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", thing.getId());
        input.put("title", thing.getTitle());
        input.put("description", thing.getDescription());
        input.put("price", thing.getPrice());
        input.put("repertory", thing.getRepertory());
        input.put("cover", thing.getCover());
        input.put("classification", getClassificationTitle(thing.getClassificationId()));
        input.put("tags", getTagTitles(thing.getId()));
        return input;
    }

    private Map<String, Object> buildImageContentPart(Thing thing) {
        String imageUrl = resolveImageUrl(thing);
        if (StringUtils.isBlank(imageUrl)) {
            return null;
        }

        Map<String, Object> imageUrlObject = new LinkedHashMap<>();
        imageUrlObject.put("url", imageUrl);

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrlObject);
        return imagePart;
    }

    private String resolveImageUrl(Thing thing) {
        if (thing == null || StringUtils.isBlank(thing.getCover())) {
            return "";
        }

        String cover = thing.getCover().trim();
        if (cover.startsWith("http://") || cover.startsWith("https://") || cover.startsWith("data:image/")) {
            return cover;
        }

        if (Boolean.TRUE.equals(preferBase64)) {
            String dataUrl = resolveLocalImageAsDataUrl(cover);
            if (StringUtils.isNotBlank(dataUrl)) {
                return dataUrl;
            }
        }

        String publicUrl = buildPublicImageUrl(cover);
        if (StringUtils.isNotBlank(publicUrl)) {
            return publicUrl;
        }

        return resolveLocalImageAsDataUrl(cover);
    }

    private String buildPublicImageUrl(String cover) {
        if (StringUtils.isBlank(publicImageBaseUrl)) {
            return "";
        }
        String fileName = safeFileName(cover);
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        String base = publicImageBaseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/staticfiles/image/" + fileName;
    }

    private String resolveLocalImageAsDataUrl(String cover) {
        String fileName = safeFileName(cover);
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        try {
            Path path = UploadPathUtils.resolveFilePath(uploadPath, "image", fileName);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "";
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) {
                return "";
            }
            String mimeType = Files.probeContentType(path);
            if (StringUtils.isBlank(mimeType)) {
                mimeType = guessImageMimeType(fileName);
            }
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            logger.warn("Resolve local product image failed. cover={}, error={}", cover, ex.getMessage());
            return "";
        }
    }

    private String safeFileName(String cover) {
        String value = StringUtils.trimToEmpty(cover).replace("\\", "/");
        int index = value.lastIndexOf('/');
        if (index >= 0) {
            value = value.substring(index + 1);
        }
        if (StringUtils.isBlank(value) || value.contains("..") || value.contains("/") || value.contains("\\")) {
            return "";
        }
        return value;
    }

    private String guessImageMimeType(String fileName) {
        String lower = StringUtils.defaultString(fileName).toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    private void fillProfile(AiProductProfile profile, JsonObject object) {
        AiProductProfileView view = parseObject(object);
        profile.setProfileJson(gson.toJson(object));
        profile.setKeywords(join(view.getKeywords()));
        profile.setScenes(join(view.getScenes()));
        profile.setTargetUsers(join(view.getTargetUsers()));
        profile.setSellingPoints(join(view.getSellingPoints()));
        profile.setSpecs(gson.toJson(view.getSpecs()));
        profile.setImageSummary(view.getImageSummary());
        profile.setContentQualityScore(view.getContentQualityScore() == null ? 60 : view.getContentQualityScore());
        profile.setSearchText(buildSearchText(view));
    }

    public AiProductProfileView toView(AiProductProfile profile) {
        AiProductProfileView view = parseProfileJson(profile.getProfileJson());
        view.setProfile(profile);
        if (view.getKeywords().isEmpty()) view.setKeywords(split(profile.getKeywords()));
        if (view.getScenes().isEmpty()) view.setScenes(split(profile.getScenes()));
        if (view.getTargetUsers().isEmpty()) view.setTargetUsers(split(profile.getTargetUsers()));
        if (view.getSellingPoints().isEmpty()) view.setSellingPoints(split(profile.getSellingPoints()));
        if (StringUtils.isBlank(view.getImageSummary())) view.setImageSummary(profile.getImageSummary());
        if (view.getContentQualityScore() == null) view.setContentQualityScore(profile.getContentQualityScore());
        return view;
    }

    private AiProductProfileView parseProfileJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new AiProductProfileView();
        }
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            return parseObject(object);
        } catch (Exception ex) {
            return new AiProductProfileView();
        }
    }

    private AiProductProfileView parseObject(JsonObject object) {
        AiProductProfileView view = new AiProductProfileView();
        view.setSummary(getString(object, "summary"));
        view.setKeywords(getStringList(object.get("keywords")));
        view.setScenes(getStringList(object.get("scenes")));
        view.setTargetUsers(getStringList(object.get("targetUsers")));
        view.setSellingPoints(getStringList(object.get("sellingPoints")));
        view.setImageSummary(getString(object, "imageSummary"));
        view.setGuideHints(getStringList(object.get("guideHints")));
        view.setAvoidReasons(getStringList(object.get("avoidReasons")));
        view.setContentQualityScore(getInteger(object, "contentQualityScore"));

        JsonElement specsElement = object == null ? null : object.get("specs");
        if (specsElement != null && specsElement.isJsonObject()) {
            Map<String, String> specs = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : specsElement.getAsJsonObject().entrySet()) {
                specs.put(entry.getKey(), elementToText(entry.getValue()));
            }
            view.setSpecs(specs);
        }
        return view;
    }

    private JsonObject fallbackProfile(Thing thing) {
        JsonObject root = new JsonObject();
        String title = StringUtils.defaultIfBlank(thing.getTitle(), "商品");
        String classification = getClassificationTitle(thing.getClassificationId());
        List<String> tags = getTagTitles(thing.getId());
        List<String> keywords = new ArrayList<>();
        keywords.add(title);
        if (StringUtils.isNotBlank(classification)) keywords.add(classification);
        keywords.addAll(tags);
        addScenarioKeywords(keywords, title + " " + classification + " " + String.join(" ", tags));

        root.addProperty("summary", title + "适合有相关需求的用户选购，建议结合价格、库存和详情页信息确认。");
        root.add("keywords", toJsonArray(keywords));
        root.add("scenes", toJsonArray(guessScenes(keywords)));
        root.add("targetUsers", toJsonArray(guessTargetUsers(keywords)));
        root.add("sellingPoints", toJsonArray(Arrays.asList("当前可售", "信息来自商品基础资料", "下单前建议核对规格")));
        JsonObject specs = new JsonObject();
        specs.addProperty("分类", StringUtils.defaultIfBlank(classification, "未分类"));
        specs.addProperty("价格", StringUtils.defaultIfBlank(thing.getPrice(), "以页面为准"));
        specs.addProperty("库存", StringUtils.defaultIfBlank(thing.getRepertory(), "以页面为准"));
        root.add("specs", specs);
        root.addProperty("imageSummary", StringUtils.isBlank(thing.getCover()) ? "暂无商品图信息" : "商品已上传封面图，后续可接入视觉模型提取图片信息");
        root.addProperty("contentQualityScore", StringUtils.isBlank(thing.getDescription()) ? 45 : 65);
        root.add("guideHints", toJsonArray(Arrays.asList("可用于基础导购召回", "建议补充更完整的商品详情")));
        root.add("avoidReasons", toJsonArray(Arrays.asList("缺少精确参数时不要承诺具体配置")));
        return root;
    }

    private void addScenarioKeywords(List<String> keywords, String text) {
        if (StringUtils.containsAny(text, "手机", "数码", "拍照")) {
            keywords.addAll(Arrays.asList("拍照", "送礼", "日常使用", "性能"));
        }
        if (StringUtils.containsAny(text, "电脑", "笔记本", "办公")) {
            keywords.addAll(Arrays.asList("办公", "学习", "性能", "便携"));
        }
        if (StringUtils.containsAny(text, "空调", "洗衣机", "家电")) {
            keywords.addAll(Arrays.asList("家用", "宿舍", "租房", "实用"));
        }
        if (StringUtils.containsAny(text, "美妆", "护肤", "服装", "包")) {
            keywords.addAll(Arrays.asList("送礼", "女生", "精致", "日常"));
        }
    }

    private List<String> guessScenes(List<String> keywords) {
        List<String> scenes = new ArrayList<>();
        String text = String.join(" ", keywords);
        if (StringUtils.containsAny(text, "办公", "电脑", "笔记本")) scenes.add("办公学习");
        if (StringUtils.containsAny(text, "送礼", "女生", "美妆", "包")) scenes.add("礼物赠送");
        if (StringUtils.containsAny(text, "家用", "家电", "空调", "洗衣机")) scenes.add("居家生活");
        if (scenes.isEmpty()) scenes.add("日常使用");
        return scenes;
    }

    private List<String> guessTargetUsers(List<String> keywords) {
        List<String> users = new ArrayList<>();
        String text = String.join(" ", keywords);
        if (StringUtils.containsAny(text, "女生", "美妆", "送礼")) users.add("女生/礼物对象");
        if (StringUtils.containsAny(text, "办公", "电脑", "学习")) users.add("学生/办公人群");
        if (StringUtils.containsAny(text, "家用", "家电")) users.add("家庭用户");
        if (users.isEmpty()) users.add("普通消费者");
        return users;
    }

    private String buildSourceHash(Thing thing) {
        return md5(StringUtils.defaultString(thing.getTitle()) + "|"
                + StringUtils.defaultString(thing.getDescription()) + "|"
                + StringUtils.defaultString(thing.getPrice()) + "|"
                + StringUtils.defaultString(thing.getRepertory()) + "|"
                + StringUtils.defaultString(thing.getCover()) + "|"
                + imageVersionToken(thing.getCover()) + "|"
                + thing.getClassificationId() + "|"
                + String.join(",", getTagTitles(thing.getId())));
    }

    private String imageVersionToken(String cover) {
        String fileName = safeFileName(cover);
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        try {
            Path path = UploadPathUtils.resolveFilePath(uploadPath, "image", fileName);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "";
            }
            return Files.size(path) + "@" + Files.getLastModifiedTime(path).toMillis();
        } catch (Exception ex) {
            return "";
        }
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(StringUtils.defaultString(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception ex) {
            return String.valueOf(StringUtils.defaultString(value).hashCode());
        }
    }

    private String buildSearchText(AiProductProfileView view) {
        List<String> parts = new ArrayList<>();
        parts.add(view.getSummary());
        parts.addAll(view.getKeywords());
        parts.addAll(view.getScenes());
        parts.addAll(view.getTargetUsers());
        parts.addAll(view.getSellingPoints());
        parts.add(view.getImageSummary());
        parts.addAll(view.getGuideHints());
        parts.addAll(view.getAvoidReasons());
        if (view.getSpecs() != null) {
            for (Map.Entry<String, String> entry : view.getSpecs().entrySet()) {
                parts.add(entry.getKey());
                parts.add(entry.getValue());
            }
        }
        return parts.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(" "));
    }

    private String getClassificationTitle(Long id) {
        if (id == null) return "";
        Classification classification = classificationMapper.selectById(id);
        return classification == null ? "" : classification.getTitle();
    }

    private List<String> getTagTitles(Long thingId) {
        if (thingId == null) {
            return new ArrayList<>();
        }
        QueryWrapper<ThingTag> wrapper = new QueryWrapper<>();
        wrapper.eq("thing_id", thingId);
        List<ThingTag> thingTags = thingTagMapper.selectList(wrapper);
        if (thingTags == null || thingTags.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> titles = new ArrayList<>();
        for (ThingTag thingTag : thingTags) {
            Tag tag = tagMapper.selectById(thingTag.getTagId());
            if (tag != null && StringUtils.isNotBlank(tag.getTitle())) {
                titles.add(tag.getTitle());
            }
        }
        return titles;
    }

    private String join(List<String> values) {
        if (values == null) {
            return "";
        }
        return values.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
    }

    private List<String> split(String value) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(value)) {
            return result;
        }
        for (String item : value.split("[,，、\\s]+")) {
            if (StringUtils.isNotBlank(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }

    private JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (String value : values.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList())) {
                array.add(value);
            }
        }
        return array;
    }

    private String getString(JsonObject object, String name) {
        if (object == null || !object.has(name) || object.get(name).isJsonNull()) {
            return "";
        }
        return elementToText(object.get(name));
    }

    private Integer getInteger(JsonObject object, String name) {
        if (object == null || !object.has(name) || object.get(name).isJsonNull()) {
            return null;
        }
        try {
            return object.get(name).getAsInt();
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> getStringList(JsonElement element) {
        List<String> result = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return result;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                String text = elementToText(item);
                if (StringUtils.isNotBlank(text)) {
                    result.add(text);
                }
            }
        } else {
            String text = elementToText(element);
            if (StringUtils.isNotBlank(text)) {
                result.add(text);
            }
        }
        return result;
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
