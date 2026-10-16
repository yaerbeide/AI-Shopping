package com.gk.study.ai.service;

import com.gk.study.ai.client.AiImageModelClient;
import com.gk.study.ai.dto.AiProductImageRequest;
import com.gk.study.ai.dto.AiProductImageResponse;
import com.gk.study.entity.Classification;
import com.gk.study.mapper.ClassificationMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiProductImageService {
    private static final Logger logger = LoggerFactory.getLogger(AiProductImageService.class);

    @Autowired
    private AiImageModelClient aiImageModelClient;

    @Autowired
    private ClassificationMapper classificationMapper;

    public AiProductImageResponse generate(AiProductImageRequest request) {
        AiProductImageRequest safeRequest = request == null ? new AiProductImageRequest() : request;
        AiProductImageResponse response = new AiProductImageResponse();
        response.setRiskTips(defaultRiskTips());

        if (!aiImageModelClient.available()) {
            response.setFallback(true);
            return response;
        }

        try {
            String style = StringUtils.defaultIfBlank(safeRequest.getStyle(), "clean-business");
            String prompt = buildPrompt(safeRequest, style);
            logger.info("AI product image prompt productName={}, classification={}, ratio={}, style={}",
                    safeRequest.getProductName(), safeRequest.getClassificationTitle(), safeRequest.getRatio(), style);
            logger.info("AI product image prompt detail={}", prompt);
            String size = resolveSize(safeRequest.getRatio());
            int count = safeRequest.getCount() == null ? 1 : safeRequest.getCount();
            response.setImages(aiImageModelClient.generate(prompt, count, size, style));
            response.setFallback(false);
            return response;
        } catch (Exception ex) {
            logger.warn("AI product image generation failed. error={}", ex.getMessage());
            response.setFallback(true);
            return response;
        }
    }

    private String buildPrompt(AiProductImageRequest request, String style) {
        String productName = StringUtils.defaultIfBlank(request.getProductName(), "selected ecommerce product");
        String classification = StringUtils.defaultIfBlank(request.getClassificationTitle(), getClassificationTitle(request.getClassificationId()));
        String subject = subjectPrompt(productName, classification, request.getKeywords(), request.getDescription());
        String usage = usageText(request.getUsage());
        String styleText = styleText(style);

        StringBuilder builder = new StringBuilder();
        builder.append("电商").append(usage).append("，");
        builder.append("唯一主体：").append(subject).append("。");
        builder.append("商品：").append(productName).append("。");
        if (StringUtils.isNotBlank(classification)) {
            builder.append("分类：").append(classification).append("。");
        }
        if (StringUtils.isNotBlank(request.getKeywords())) {
            builder.append("关键词：").append(StringUtils.left(request.getKeywords(), 60)).append("。");
        }
        if (StringUtils.isNotBlank(request.getDescription())) {
            builder.append("简介：").append(StringUtils.left(request.getDescription(), 80)).append("。");
        }
        if (request.getHighlights() != null && !request.getHighlights().isEmpty()) {
            builder.append("卖点：").append(StringUtils.left(String.join("、", request.getHighlights()), 80)).append("。");
        }
        builder.append(styleText).append("，主体居中，背景干净，真实商业摄影，高端电商质感。");
        builder.append("不要花朵植物宠物卡通人物摆件风景抽象装饰，不要logo水印二维码文字。");
        return StringUtils.left(builder.toString(), 760);
    }

    private String subjectPrompt(String productName, String classification, String keywords, String description) {
        String text = StringUtils.defaultString(productName) + " "
                + StringUtils.defaultString(classification) + " "
                + StringUtils.defaultString(keywords) + " "
                + StringUtils.defaultString(description);
        if (containsAny(text, "游戏本", "笔记本", "电脑", "电脑办公", "4060", "显卡", "续航")) {
            return "一台真实的现代高性能游戏笔记本电脑，打开屏幕放置在简洁桌面上，机身轻薄，有电竞性能感，适合办公、学习和游戏场景";
        }
        if (containsAny(text, "手机", "iphone", "galaxy", "mate", "数码")) {
            return "一部真实的现代智能手机，展示正反面或产品主体，高端数码设备质感";
        }
        if (containsAny(text, "空调", "冷暖", "变频")) {
            return "一台真实的壁挂式空调，位于简洁家居室内场景";
        }
        if (containsAny(text, "洗衣机", "洗烘", "滚筒")) {
            return "一台真实的前开门滚筒洗衣机，位于干净的洗衣房或家居场景";
        }
        if (containsAny(text, "吉他", "乐器")) {
            return "一把真实的吉他商品，干净棚拍电商摄影";
        }
        if (containsAny(text, "鞋", "球鞋", "运动鞋")) {
            return "一双真实的运动鞋商品，干净电商棚拍摄影";
        }
        if (containsAny(text, "背包", "书包", "登山包")) {
            return "一个真实的背包商品，干净电商棚拍摄影";
        }
        if (containsAny(text, "护肤", "精华", "面霜", "美妆")) {
            return "一瓶真实的护肤或美妆产品，干净高端电商摄影";
        }
        return "商品名称和分类所描述的真实商品，不是无关装饰物";
    }

    private boolean containsAny(String text, String... words) {
        if (StringUtils.isBlank(text)) return false;
        String lower = text.toLowerCase();
        for (String word : words) {
            if (lower.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String getClassificationTitle(Long id) {
        if (id == null) return "";
        Classification classification = classificationMapper.selectById(id);
        return classification == null ? "" : classification.getTitle();
    }

    private String resolveSize(String ratio) {
        String value = StringUtils.defaultIfBlank(ratio, "1:1");
        if ("16:9".equals(value)) return "1664x936";
        if ("4:3".equals(value)) return "1472x1104";
        return "1328x1328";
    }

    private String usageText(String usage) {
        if ("poster".equals(usage)) return "宣传海报";
        if ("detail".equals(usage)) return "详情首图";
        return "商品封面图";
    }

    private String styleText(String style) {
        if ("ecommerce-poster".equals(style)) return "明亮电商海报风，清晰有吸引力但不过度花哨";
        if ("premium".equals(style)) return "高端克制、精致商务化";
        if ("lifestyle".equals(style)) return "自然生活场景，温和真实";
        if ("tech".equals(style)) return "现代科技风，冷色光影，干净数码氛围";
        return "简洁商务风，背景干净，高级但克制";
    }

    private List<String> defaultRiskTips() {
        return Arrays.asList(
                "AI generated images are only display suggestions. Confirm that the image matches the real product before setting it as the cover.",
                "Do not use AI images for fake parameters, fake certifications, or misleading product claims."
        );
    }
}
