package com.gk.study.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.gk.study.ai.client.AiModelClient;
import com.gk.study.ai.dto.AiCandidateProduct;
import com.gk.study.ai.dto.AiChatMessage;
import com.gk.study.ai.dto.AiGuideRequest;
import com.gk.study.ai.dto.AiGuideResponse;
import com.gk.study.ai.dto.AiIntent;
import com.gk.study.entity.AiProductProfile;
import com.gk.study.entity.Classification;
import com.gk.study.entity.Thing;
import com.gk.study.mapper.AiProductProfileMapper;
import com.gk.study.mapper.ClassificationMapper;
import com.gk.study.mapper.ThingMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiGuideService {
    private static final Logger logger = LoggerFactory.getLogger(AiGuideService.class);
    private static final int CANDIDATE_LIMIT = 20;
    private static final int RECOMMEND_LIMIT = 6;

    private final Gson gson = new Gson();

    @Autowired
    private AiModelClient aiModelClient;

    @Autowired
    private ThingMapper thingMapper;

    @Autowired
    private ClassificationMapper classificationMapper;

    @Autowired
    private AiProductProfileMapper aiProductProfileMapper;

    public AiGuideResponse guide(AiGuideRequest request) {
        String query = request == null ? "" : StringUtils.trimToEmpty(request.getQuery());
        AiIntent intent = parseIntent(query);
        List<Thing> candidates = findCandidates(intent, query);
        if (candidates.isEmpty()) {
            return emptyResponse(intent);
        }

        AiGuideResponse response = rerankWithModel(query, intent, candidates);
        if (response == null || response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
            response = fallbackResponse(intent, candidates, true);
        }

        attachProducts(response, candidates);
        return response;
    }

    private AiIntent parseIntent(String query) {
        AiIntent localIntent = parseIntentLocally(query);
        if (!aiModelClient.available()) {
            return localIntent;
        }

        try {
            String prompt = "你是商城搜索意图解析助手。请把用户需求解析成 JSON，不要输出多余文本。\n"
                    + "字段：categoryKeywords、productKeywords、budgetMin、budgetMax、scene、targetUser、mustHave、avoid、sortPreference。\n"
                    + "没有值时使用 null 或空数组。\n"
                    + "用户需求：" + query;
            String content = aiModelClient.chat(Arrays.asList(
                    new AiChatMessage("system", "你只返回可解析 JSON。"),
                    new AiChatMessage("user", prompt)
            ));
            AiIntent modelIntent = gson.fromJson(extractJsonObject(content), AiIntent.class);
            AiIntent merged = mergeIntent(localIntent, modelIntent);
            enrichIntentByScenario(merged, query);
            return merged;
        } catch (Exception ex) {
            logger.warn("AI intent parse failed, using local intent. query={}, error={}", query, ex.getMessage());
            return localIntent;
        }
    }

    private AiIntent parseIntentLocally(String query) {
        AiIntent intent = new AiIntent();
        if (StringUtils.isBlank(query)) {
            intent.setProductKeywords(new ArrayList<>());
            return intent;
        }

        List<String> productKeywords = new ArrayList<>();
        for (String part : query.split("[,，\\s]+")) {
            String keyword = part.trim();
            if (keyword.length() >= 2 && !keyword.matches(".*\\d+.*")) {
                productKeywords.add(keyword);
            }
        }
        intent.setProductKeywords(productKeywords);

        BigDecimal budgetMax = extractBudgetMax(query);
        if (budgetMax != null) {
            intent.setBudgetMax(budgetMax);
        }

        if (query.contains("父母") || query.contains("长辈")) {
            intent.setTargetUser("父母/长辈");
        }
        if (query.contains("办公")) {
            intent.setScene("办公");
        } else if (query.contains("宿舍")) {
            intent.setScene("宿舍");
        } else if (query.contains("送")) {
            intent.setScene("送礼");
        }

        List<String> categoryKeywords = new ArrayList<>();
        if (query.contains("手机")) categoryKeywords.add("手机");
        if (query.contains("电脑") || query.contains("笔记本")) categoryKeywords.add("电脑");
        if (query.contains("洗衣机") || query.contains("空调") || query.contains("家电")) categoryKeywords.add("家电");
        if (query.contains("书")) categoryKeywords.add("图书");
        intent.setCategoryKeywords(categoryKeywords);
        intent.setSortPreference("综合推荐");
        enrichIntentByScenario(intent, query);
        return intent;
    }

    private AiIntent mergeIntent(AiIntent localIntent, AiIntent modelIntent) {
        if (modelIntent == null) {
            return localIntent;
        }
        if (modelIntent.getBudgetMax() == null) modelIntent.setBudgetMax(localIntent.getBudgetMax());
        if (modelIntent.getBudgetMin() == null) modelIntent.setBudgetMin(localIntent.getBudgetMin());
        modelIntent.setProductKeywords(mergeKeywords(localIntent.getProductKeywords(), modelIntent.getProductKeywords()));
        modelIntent.setCategoryKeywords(mergeKeywords(localIntent.getCategoryKeywords(), modelIntent.getCategoryKeywords()));
        modelIntent.setMustHave(mergeKeywords(localIntent.getMustHave(), modelIntent.getMustHave()));
        modelIntent.setAvoid(mergeKeywords(localIntent.getAvoid(), modelIntent.getAvoid()));
        if (StringUtils.isBlank(modelIntent.getScene())) modelIntent.setScene(localIntent.getScene());
        if (StringUtils.isBlank(modelIntent.getTargetUser())) modelIntent.setTargetUser(localIntent.getTargetUser());
        return modelIntent;
    }

    private List<String> mergeKeywords(List<String> first, List<String> second) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        addKeywords(map, first);
        addKeywords(map, second);
        return new ArrayList<>(map.values());
    }

    private List<Thing> findCandidates(AiIntent intent, String rawQuery) {
        QueryWrapper<Thing> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "0");
        List<Thing> all = thingMapper.selectList(wrapper);
        if (all == null || all.isEmpty()) {
            all = thingMapper.selectList(new QueryWrapper<>());
        }

        Map<Long, String> classMap = getClassificationMap();
        Map<Long, AiProductProfile> profileMap = loadProfileMap(all);
        List<String> keywords = collectKeywords(intent, rawQuery);
        List<String> strictCategoryKeywords = collectExplicitCategoryKeywords(rawQuery, intent);
        BigDecimal budgetMax = intent == null ? null : intent.getBudgetMax();
        boolean hasConstraints = !keywords.isEmpty()
                || budgetMax != null
                || (intent != null && intent.getBudgetMin() != null)
                || !strictCategoryKeywords.isEmpty();

        List<ScoredThing> scored = new ArrayList<>();
        for (Thing thing : all) {
            if (thing == null) continue;
            if (StringUtils.isNotBlank(thing.getStatus()) && !"0".equals(thing.getStatus())) continue;
            if (toInt(thing.getRepertory()) <= 0) continue;
            BigDecimal price = toDecimal(thing.getPrice());
            if (budgetMax != null && price != null && price.compareTo(budgetMax) > 0) continue;

            String classification = classMap.get(thing.getClassificationId());
            AiProductProfile profile = profileMap.get(thing.getId());
            if (!strictCategoryKeywords.isEmpty()
                    && !matchesStrictCategory(thing, classification, profile, strictCategoryKeywords)) {
                continue;
            }
            int matchScore = scoreThing(thing, classification, profile, keywords);
            if (!keywords.isEmpty() && matchScore <= 0) continue;
            scored.add(new ScoredThing(thing, matchScore + hotScore(thing)));
        }

        if (scored.isEmpty() && isScenarioIntent(intent, rawQuery)) {
            scored.addAll(findScenarioCandidates(intent, all, classMap, profileMap, budgetMax, strictCategoryKeywords));
        }

        if (scored.isEmpty()) {
            if (hasConstraints) {
                logger.info("AI guide no candidates after strict filters. query={}, intent={}", rawQuery, gson.toJson(intent));
                return new ArrayList<>();
            }
            for (Thing thing : all) {
                if (thing == null) continue;
                if (StringUtils.isNotBlank(thing.getStatus()) && !"0".equals(thing.getStatus())) continue;
                if (toInt(thing.getRepertory()) <= 0) continue;
                scored.add(new ScoredThing(thing, hotScore(thing)));
            }
        }

        Collections.sort(scored, new Comparator<ScoredThing>() {
            @Override
            public int compare(ScoredThing a, ScoredThing b) {
                return Integer.compare(b.score, a.score);
            }
        });

        return scored.stream().limit(CANDIDATE_LIMIT).map(item -> item.thing).collect(Collectors.toList());
    }

    private AiGuideResponse rerankWithModel(String query, AiIntent intent, List<Thing> candidates) {
        if (!aiModelClient.available()) {
            return null;
        }

        try {
            String prompt = "你是商城智能导购助手。你只能从候选商品列表中选择商品，不能推荐列表外商品。\n"
                    + "商品名称、价格、库存必须以候选商品数据为准。返回 JSON，不要输出多余文本。\n"
                    + "用户需求：" + query + "\n"
                    + "解析后的意图：" + gson.toJson(intent) + "\n"
                    + "候选商品：" + gson.toJson(toCandidateProducts(candidates)) + "\n"
                    + "返回格式：{\"answer\":\"...\",\"recommendations\":[{\"thingId\":1,\"reason\":\"...\",\"risk\":\"...\"}],\"nextQuestions\":[\"...\",\"...\"]}";
            String content = aiModelClient.chat(Arrays.asList(
                    new AiChatMessage("system", "你是严格基于候选商品数据的电商导购助手。"),
                    new AiChatMessage("user", prompt)
            ));
            AiGuideResponse response = gson.fromJson(extractJsonObject(content), AiGuideResponse.class);
            if (response != null) {
                response.setIntent(intent);
                response.setFallback(false);
                response.setRecommendations(validateRecommendations(response.getRecommendations(), candidates));
                if (response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
                    logger.warn("AI rerank returned no valid candidate ids. query={}, content={}", query, shortText(content, 800));
                }
            }
            return response;
        } catch (Exception ex) {
            logger.warn("AI rerank failed, using fallback recommendation. query={}, error={}", query, ex.getMessage());
            return null;
        }
    }

    private List<AiGuideResponse.Recommendation> validateRecommendations(List<AiGuideResponse.Recommendation> recommendations, List<Thing> candidates) {
        if (recommendations == null) {
            return new ArrayList<>();
        }
        Set<Long> candidateIds = candidates.stream().map(Thing::getId).collect(Collectors.toSet());
        List<AiGuideResponse.Recommendation> valid = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        for (AiGuideResponse.Recommendation item : recommendations) {
            if (item == null || item.getThingId() == null) continue;
            if (!candidateIds.contains(item.getThingId())) continue;
            if (used.contains(item.getThingId())) continue;
            used.add(item.getThingId());
            valid.add(item);
            if (valid.size() >= RECOMMEND_LIMIT) break;
        }
        return valid;
    }

    private AiGuideResponse fallbackResponse(AiIntent intent, List<Thing> candidates, boolean modelFallback) {
        AiGuideResponse response = new AiGuideResponse();
        response.setIntent(intent);
        response.setFallback(modelFallback);
        response.setAnswer(modelFallback
                ? "已根据商品分类、价格、库存和热度为你整理了可选商品。"
                : "根据你的需求，下面这些商品更值得优先查看。");
        response.setNextQuestions(Arrays.asList("更看重价格还是品质？", "是否需要我继续缩小范围？"));

        Map<Long, AiProductProfile> profileMap = loadProfileMap(candidates);
        for (Thing thing : candidates.stream().limit(RECOMMEND_LIMIT).collect(Collectors.toList())) {
            AiProductProfile profile = profileMap.get(thing.getId());
            AiGuideResponse.Recommendation recommendation = new AiGuideResponse.Recommendation();
            recommendation.setThingId(thing.getId());
            recommendation.setReason(buildFallbackReason(thing, profile));
            recommendation.setRisk(buildFallbackRisk(thing, profile));
            response.getRecommendations().add(recommendation);
        }
        return response;
    }

    private String buildFallbackReason(Thing thing, AiProductProfile profile) {
        if (profile != null) {
            String summary = readProfileSummary(profile);
            if (StringUtils.isNotBlank(summary)) {
                return shortText(summary, 90);
            }
            if (StringUtils.isNotBlank(profile.getSellingPoints())) {
                return "推荐点：" + shortText(profile.getSellingPoints().replace(",", "、"), 80);
            }
            if (StringUtils.isNotBlank(profile.getScenes())) {
                return "适合" + shortText(profile.getScenes().replace(",", "、"), 60) + "等场景。";
            }
        }
        String title = thing == null ? "这件商品" : StringUtils.defaultIfBlank(thing.getTitle(), "这件商品");
        return shortText(title, 40) + "与当前需求相关，并且处于可购买状态。";
    }

    private String buildFallbackRisk(Thing thing, AiProductProfile profile) {
        if (profile != null && StringUtils.isNotBlank(profile.getImageSummary())) {
            return "已参考商品档案；下单前仍建议核对详情页参数和库存。";
        }
        if (thing != null && toInt(thing.getRepertory()) <= 5) {
            return "库存偏少，建议尽快确认规格后下单。";
        }
        return "具体参数仍建议进入详情页核对。";
    }

    private AiGuideResponse emptyResponse(AiIntent intent) {
        AiGuideResponse response = new AiGuideResponse();
        response.setIntent(intent);
        response.setFallback(true);
        response.setAnswer("暂时没有找到足够匹配的可购买商品，可以换一个预算、分类或使用场景再试。");
        response.setNextQuestions(Arrays.asList("预算可以放宽吗？", "你更想看哪个分类？"));
        return response;
    }

    private void attachProducts(AiGuideResponse response, List<Thing> candidates) {
        Map<Long, Thing> candidateMap = new LinkedHashMap<>();
        for (Thing thing : candidates) {
            candidateMap.put(thing.getId(), thing);
        }
        for (AiGuideResponse.Recommendation item : response.getRecommendations()) {
            item.setThing(candidateMap.get(item.getThingId()));
            if (StringUtils.isBlank(item.getReason())) {
                item.setReason("这件商品和你的需求匹配度较高。");
            }
            if (StringUtils.isBlank(item.getRisk())) {
                item.setRisk("下单前建议核对商品详情和库存。");
            }
        }
    }

    private List<AiCandidateProduct> toCandidateProducts(List<Thing> things) {
        Map<Long, String> classMap = getClassificationMap();
        Map<Long, AiProductProfile> profileMap = loadProfileMap(things);
        List<AiCandidateProduct> list = new ArrayList<>();
        for (Thing thing : things) {
            AiProductProfile profile = profileMap.get(thing.getId());
            AiCandidateProduct product = new AiCandidateProduct();
            product.setId(thing.getId());
            product.setTitle(thing.getTitle());
            product.setCover(thing.getCover());
            product.setDescription(shortText(thing.getDescription(), 120));
            product.setPrice(thing.getPrice());
            product.setRepertory(thing.getRepertory());
            product.setScore(thing.getScore());
            product.setPv(thing.getPv());
            product.setRecommendCount(thing.getRecommendCount());
            product.setWishCount(thing.getWishCount());
            product.setCollectCount(thing.getCollectCount());
            product.setClassificationId(thing.getClassificationId());
            product.setClassificationTitle(classMap.get(thing.getClassificationId()));
            if (profile != null) {
                product.setProfileSummary(shortText(readProfileSummary(profile), 160));
                product.setProfileKeywords(profile.getKeywords());
                product.setProfileScenes(profile.getScenes());
                product.setProfileTargetUsers(profile.getTargetUsers());
                product.setProfileSellingPoints(shortText(profile.getSellingPoints(), 180));
                product.setProfileImageSummary(shortText(profile.getImageSummary(), 120));
                product.setContentQualityScore(profile.getContentQualityScore());
            }
            list.add(product);
        }
        return list;
    }

    private List<String> collectKeywords(AiIntent intent, String rawQuery) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (intent != null) {
            addKeywords(map, intent.getCategoryKeywords());
            addKeywords(map, intent.getProductKeywords());
            addKeywords(map, intent.getMustHave());
            addKeyword(map, intent.getScene());
            addKeyword(map, intent.getTargetUser());
        }
        if (StringUtils.isNotBlank(rawQuery)) {
            for (String part : rawQuery.split("[,，\\s]+")) {
                if (part.length() >= 2
                        && part.length() <= 8
                        && !part.matches(".*\\d+.*")
                        && !isIntentStopPhrase(part)) {
                    addKeyword(map, part);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<String> collectExplicitCategoryKeywords(String rawQuery, AiIntent intent) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String text = StringUtils.defaultString(rawQuery);
        if (containsAny(text, "手机", "iphone", "苹果手机", "华为手机", "安卓手机")) {
            addStrictCategory(map, "手机");
        }
        if (containsAny(text, "电脑", "笔记本", "台式机", "平板")) {
            addStrictCategory(map, "电脑");
        }
        if (containsAny(text, "洗衣机", "空调", "冰箱", "家电")) {
            addStrictCategory(map, "家电");
        }
        if (containsAny(text, "图书", "书籍", "教材", "教程")) {
            addStrictCategory(map, "图书");
        }
        if (containsAny(text, "口红", "护肤", "精华", "美妆", "香水")) {
            addStrictCategory(map, "美妆");
        }
        if (containsAny(text, "鞋", "衣服", "外套", "包")) {
            addStrictCategory(map, "服装");
        }

        if (map.isEmpty() && intent != null) {
            List<String> categories = intent.getCategoryKeywords();
            if (categories != null && categories.size() == 1) {
                addStrictCategory(map, categories.get(0));
            }
        }
        return new ArrayList<>(map.values());
    }

    private void addStrictCategory(Map<String, String> map, String value) {
        String item = StringUtils.trimToEmpty(value);
        if (item.length() >= 2) {
            map.put(item.toLowerCase(), item);
        }
    }

    private boolean matchesStrictCategory(Thing thing, String classification, AiProductProfile profile, List<String> strictCategoryKeywords) {
        if (strictCategoryKeywords == null || strictCategoryKeywords.isEmpty()) {
            return true;
        }
        String matchText = (StringUtils.defaultString(thing == null ? "" : thing.getTitle()) + " "
                + StringUtils.defaultString(classification) + " "
                + buildProfileMatchText(profile)).toLowerCase();
        for (String keyword : strictCategoryKeywords) {
            if (matchesStrictCategoryKeyword(matchText, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesStrictCategoryKeyword(String matchText, String keyword) {
        String text = StringUtils.defaultString(matchText).toLowerCase();
        String item = StringUtils.trimToEmpty(keyword).toLowerCase();
        if (StringUtils.isBlank(item)) {
            return false;
        }
        if (text.contains(item)) {
            return true;
        }
        if ("手机".equals(item)) {
            return containsAny(text, "iphone", "苹果", "华为mate", "galaxy", "安卓", "旗舰机", "拍照手机", "数码手机");
        }
        if ("电脑".equals(item)) {
            return containsAny(text, "笔记本", "电脑办公", "pc", "台式机", "轻薄本", "游戏本");
        }
        if ("家电".equals(item)) {
            return containsAny(text, "洗衣机", "空调", "冰箱", "电视", "家用电器");
        }
        if ("图书".equals(item)) {
            return containsAny(text, "书", "教材", "教程", "图书音像");
        }
        if ("美妆".equals(item)) {
            return containsAny(text, "口红", "护肤", "精华", "香水", "美妆个护");
        }
        if ("服装".equals(item)) {
            return containsAny(text, "鞋", "衣服", "外套", "包", "服装鞋包");
        }
        return false;
    }

    private void enrichIntentByScenario(AiIntent intent, String query) {
        if (intent == null || StringUtils.isBlank(query)) {
            return;
        }
        String text = query.toLowerCase();

        if (containsAny(text, "女朋友", "女友", "女生", "女孩", "老婆", "爱人", "情侣")) {
            intent.setTargetUser(StringUtils.defaultIfBlank(intent.getTargetUser(), "女生/女朋友"));
            addUnique(intent.getCategoryKeywords(), "美妆个护", "服装鞋包", "家居家装", "食品生鲜");
            addUnique(intent.getProductKeywords(), "香水", "口红", "护肤", "精华", "包", "鞋", "礼盒");
            addUnique(intent.getMustHave(), "好看", "精致", "实用", "品质", "新款", "品牌精选", "高端");
            if (containsAny(text, "数码", "手机", "耳机", "电脑", "拍照")) {
                addUnique(intent.getCategoryKeywords(), "手机数码");
                addUnique(intent.getProductKeywords(), "耳机", "手机", "拍照");
            }
        }

        if (containsAny(text, "礼物", "送礼", "生日", "纪念日", "情人节", "七夕", "惊喜")) {
            intent.setScene(StringUtils.defaultIfBlank(intent.getScene(), "送礼"));
            addUnique(intent.getProductKeywords(), "礼盒", "精选", "新品", "高端", "实用", "时尚");
            addUnique(intent.getMustHave(), "适合送礼", "有心意", "拿得出手");
        }

        if (containsAny(text, "父母", "长辈", "爸妈", "老人")) {
            intent.setTargetUser(StringUtils.defaultIfBlank(intent.getTargetUser(), "父母/长辈"));
            addUnique(intent.getCategoryKeywords(), "家用电器", "食品生鲜", "手机数码", "家居家装");
            addUnique(intent.getProductKeywords(), "空调", "洗衣机", "手机", "健康", "实用");
            addUnique(intent.getMustHave(), "易用", "实用", "舒适", "品质");
        }

        if (containsAny(text, "办公", "学习", "上课", "考研")) {
            intent.setScene(StringUtils.defaultIfBlank(intent.getScene(), "办公学习"));
            addUnique(intent.getCategoryKeywords(), "电脑办公", "图书音像", "手机数码");
            addUnique(intent.getProductKeywords(), "笔记本", "电脑", "教程", "书", "键盘", "鼠标");
            addUnique(intent.getMustHave(), "性能", "效率", "轻便", "耐用");
        }

        if (containsAny(text, "宿舍", "租房", "卧室")) {
            intent.setScene(StringUtils.defaultIfBlank(intent.getScene(), "宿舍生活"));
            addUnique(intent.getCategoryKeywords(), "家用电器", "家居家装", "电脑办公");
            addUnique(intent.getProductKeywords(), "空调", "洗衣机", "收纳", "台灯", "小家电");
            addUnique(intent.getMustHave(), "小空间", "实用", "安静", "省心");
        }

        if (containsAny(text, "玩梗", "整活", "有梗", "搞笑", "趣味", "猎奇", "收藏", "摆件", "新奇", "稀奇")) {
            intent.setScene(StringUtils.defaultIfBlank(intent.getScene(), "趣味收藏"));
            addUnique(intent.getProductKeywords(), "玩梗", "整活", "趣味", "猎奇", "收藏", "新奇", "特殊");
            addUnique(intent.getMustHave(), "趣味属性", "辨识度", "适合收藏", "有话题感");
        }
    }

    private List<ScoredThing> findScenarioCandidates(AiIntent intent, List<Thing> all, Map<Long, String> classMap, Map<Long, AiProductProfile> profileMap, BigDecimal budgetMax, List<String> strictCategoryKeywords) {
        List<String> categoryKeywords = intent == null ? new ArrayList<>() : intent.getCategoryKeywords();
        List<String> softKeywords = collectKeywords(intent, "");
        List<ScoredThing> scored = new ArrayList<>();
        for (Thing thing : all) {
            if (thing == null) continue;
            if (StringUtils.isNotBlank(thing.getStatus()) && !"0".equals(thing.getStatus())) continue;
            if (toInt(thing.getRepertory()) <= 0) continue;
            BigDecimal price = toDecimal(thing.getPrice());
            if (budgetMax != null && price != null && price.compareTo(budgetMax) > 0) continue;

            String classification = classMap.get(thing.getClassificationId());
            AiProductProfile profile = profileMap.get(thing.getId());
            if (!strictCategoryKeywords.isEmpty()
                    && !matchesStrictCategory(thing, classification, profile, strictCategoryKeywords)) {
                continue;
            }
            int score = scoreThing(thing, classification, profile, softKeywords);
            if (score <= 0 && matchesAnyCategory(classification, categoryKeywords)) {
                score = 12;
            }
            if (score > 0) {
                scored.add(new ScoredThing(thing, score + hotScore(thing)));
            }
        }
        return scored;
    }

    private boolean matchesAnyCategory(String classification, List<String> categoryKeywords) {
        if (StringUtils.isBlank(classification) || categoryKeywords == null) {
            return false;
        }
        String clazz = classification.toLowerCase();
        for (String keyword : categoryKeywords) {
            String item = StringUtils.trimToEmpty(keyword).toLowerCase();
            if (item.length() >= 2 && (clazz.contains(item) || item.contains(clazz))) {
                return true;
            }
        }
        return false;
    }

    private boolean isScenarioIntent(AiIntent intent, String rawQuery) {
        String text = StringUtils.defaultString(rawQuery);
        return (intent != null && (StringUtils.isNotBlank(intent.getScene()) || StringUtils.isNotBlank(intent.getTargetUser())))
                || containsAny(text, "礼物", "送礼", "适合", "推荐", "女朋友", "女生", "父母", "长辈", "宿舍", "办公", "学习",
                "玩梗", "整活", "有梗", "搞笑", "趣味", "猎奇", "收藏", "新奇");
    }

    private boolean isIntentStopPhrase(String value) {
        String text = StringUtils.trimToEmpty(value);
        return containsAny(text, "适合", "推荐", "礼物", "送礼", "送", "想买", "需要", "以内", "以下", "左右");
    }

    private boolean containsAny(String source, String... words) {
        if (StringUtils.isBlank(source) || words == null) {
            return false;
        }
        for (String word : words) {
            if (StringUtils.isNotBlank(word) && source.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void addUnique(List<String> list, String... values) {
        if (list == null || values == null) {
            return;
        }
        for (String value : values) {
            String item = StringUtils.trimToEmpty(value);
            if (item.length() >= 2 && !list.contains(item)) {
                list.add(item);
            }
        }
    }

    private void addKeywords(Map<String, String> map, List<String> values) {
        if (values == null) return;
        for (String value : values) {
            addKeyword(map, value);
        }
    }

    private void addKeyword(Map<String, String> map, String value) {
        for (String keyword : expandKeyword(value)) {
            if (keyword.length() >= 2) {
                map.put(keyword.toLowerCase(), keyword);
            }
        }
    }

    private List<String> expandKeyword(String value) {
        String keyword = StringUtils.trimToEmpty(value);
        if (keyword.length() < 2) {
            return new ArrayList<>();
        }
        LinkedHashMap<String, String> expanded = new LinkedHashMap<>();
        expanded.put(keyword.toLowerCase(), keyword);

        String normalized = keyword
                .replaceAll("^(想买|需要|推荐|适合|找|看)", "")
                .replaceAll("(商品|产品|好物|东西|款|类)$", "");
        if (normalized.length() >= 2) {
            expanded.put(normalized.toLowerCase(), normalized);
        }

        String text = keyword.toLowerCase();
        if (containsAny(text, "玩梗", "整活", "有梗", "搞笑", "趣味", "猎奇", "新奇", "稀奇")) {
            addExpandedKeyword(expanded, "玩梗", "整活", "趣味", "猎奇", "搞笑", "新奇", "特殊", "话题感", "辨识度");
        }
        if (containsAny(text, "收藏", "摆件", "藏品")) {
            addExpandedKeyword(expanded, "收藏", "摆件", "藏品", "趣味", "纪念", "展示");
        }
        return new ArrayList<>(expanded.values());
    }

    private void addExpandedKeyword(Map<String, String> map, String... values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            String keyword = StringUtils.trimToEmpty(value);
            if (keyword.length() >= 2) {
                map.put(keyword.toLowerCase(), keyword);
            }
        }
    }

    private int scoreThing(Thing thing, String classification, AiProductProfile profile, List<String> keywords) {
        int score = 0;
        String title = StringUtils.defaultString(thing.getTitle()).toLowerCase();
        String desc = StringUtils.defaultString(thing.getDescription()).toLowerCase();
        String clazz = StringUtils.defaultString(classification).toLowerCase();
        String profileText = buildProfileMatchText(profile).toLowerCase();
        String scenes = profile == null ? "" : StringUtils.defaultString(profile.getScenes()).toLowerCase();
        String targetUsers = profile == null ? "" : StringUtils.defaultString(profile.getTargetUsers()).toLowerCase();
        for (String keyword : keywords) {
            String k = keyword.toLowerCase();
            if (title.contains(k)) score += 30;
            if (desc.contains(k)) score += 10;
            if (clazz.contains(k)) score += 18;
            if (profileText.contains(k)) score += 20;
            if (scenes.contains(k)) score += 24;
            if (targetUsers.contains(k)) score += 24;
        }
        return score;
    }

    private Map<Long, AiProductProfile> loadProfileMap(List<Thing> things) {
        Map<Long, AiProductProfile> map = new HashMap<>();
        if (things == null || things.isEmpty()) {
            return map;
        }
        List<Long> ids = things.stream()
                .filter(item -> item != null && item.getId() != null)
                .map(Thing::getId)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return map;
        }
        try {
            QueryWrapper<AiProductProfile> wrapper = new QueryWrapper<>();
            wrapper.in("thing_id", ids);
            List<AiProductProfile> profiles = aiProductProfileMapper.selectList(wrapper);
            if (profiles != null) {
                for (AiProductProfile profile : profiles) {
                    if (profile != null && profile.getThingId() != null) {
                        map.put(profile.getThingId(), profile);
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("AI product profile table is not available, guide will use base product fields. error={}", ex.getMessage());
        }
        return map;
    }

    private String buildProfileMatchText(AiProductProfile profile) {
        if (profile == null) {
            return "";
        }
        return StringUtils.defaultString(profile.getSearchText()) + " "
                + StringUtils.defaultString(profile.getKeywords()) + " "
                + StringUtils.defaultString(profile.getScenes()) + " "
                + StringUtils.defaultString(profile.getTargetUsers()) + " "
                + StringUtils.defaultString(profile.getSellingPoints()) + " "
                + StringUtils.defaultString(profile.getImageSummary());
    }

    private String readProfileSummary(AiProductProfile profile) {
        if (profile == null || StringUtils.isBlank(profile.getProfileJson())) {
            return "";
        }
        try {
            Object summary = gson.fromJson(profile.getProfileJson(), Map.class).get("summary");
            return summary == null ? "" : String.valueOf(summary);
        } catch (Exception ex) {
            return "";
        }
    }

    private int hotScore(Thing thing) {
        return Math.min(toInt(thing.getRecommendCount()), 100)
                + Math.min(toInt(thing.getCollectCount()), 100)
                + Math.min(toInt(thing.getWishCount()), 100)
                + Math.min(toInt(thing.getPv()) / 10, 80);
    }

    private Map<Long, String> getClassificationMap() {
        Map<Long, String> map = new HashMap<>();
        List<Classification> list = classificationMapper.selectList(new QueryWrapper<>());
        if (list != null) {
            for (Classification item : list) {
                map.put(item.getId(), item.getTitle());
            }
        }
        return map;
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

    private BigDecimal extractBudgetMax(String query) {
        if (StringUtils.isBlank(query)) return null;
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(元|块|以内|以下|左右|万)?");
        Matcher matcher = pattern.matcher(query);
        BigDecimal max = null;
        while (matcher.find()) {
            BigDecimal value = new BigDecimal(matcher.group(1));
            String unit = matcher.group(2);
            if ("万".equals(unit)) {
                value = value.multiply(new BigDecimal("10000"));
            }
            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }

    private BigDecimal toDecimal(String value) {
        try {
            if (StringUtils.isBlank(value)) return null;
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private int toInt(String value) {
        try {
            if (StringUtils.isBlank(value)) return 0;
            return new BigDecimal(value.trim()).intValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    private String shortText(String value, int maxLength) {
        if (StringUtils.isBlank(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private boolean isEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }

    private static class ScoredThing {
        private final Thing thing;
        private final int score;

        private ScoredThing(Thing thing, int score) {
            this.thing = thing;
            this.score = score;
        }
    }
}
