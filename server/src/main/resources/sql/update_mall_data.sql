-- 清空原有数据
CREATE TABLE IF NOT EXISTS `b_ai_product_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `thing_id` bigint NOT NULL COMMENT '商品ID，对应 b_thing.id',
  `profile_json` longtext COMMENT 'AI 生成的完整结构化档案 JSON',
  `search_text` text COMMENT '导购召回使用的合并检索文本',
  `keywords` varchar(1000) DEFAULT NULL COMMENT '关键词，逗号分隔',
  `scenes` varchar(1000) DEFAULT NULL COMMENT '适用场景，逗号分隔',
  `target_users` varchar(1000) DEFAULT NULL COMMENT '目标人群，逗号分隔',
  `selling_points` text COMMENT '核心卖点，逗号分隔',
  `specs` text COMMENT '规格参数 JSON',
  `image_summary` text COMMENT '图片/封面识别摘要',
  `content_quality_score` int DEFAULT 0 COMMENT '商品资料完整度评分 0-100',
  `source_hash` varchar(64) DEFAULT NULL COMMENT '商品基础信息指纹',
  `status` varchar(20) DEFAULT 'READY' COMMENT 'READY/FALLBACK/FAILED',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '生成异常信息',
  `generated_at` varchar(32) DEFAULT NULL COMMENT '生成时间戳',
  `create_time` varchar(32) DEFAULT NULL COMMENT '创建时间戳',
  `update_time` varchar(32) DEFAULT NULL COMMENT '更新时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_product_profile_thing_id` (`thing_id`),
  KEY `idx_ai_product_profile_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品 AI 导购档案表';

TRUNCATE TABLE `b_ai_product_profile`;
TRUNCATE TABLE `b_classification`;
TRUNCATE TABLE `b_tag`;
TRUNCATE TABLE `b_thing`;
TRUNCATE TABLE `b_thing_tag`;

-- 重新插入分类数据（更新为综合商城分类）
INSERT INTO `b_classification` VALUES (1, '手机数码', '1723787980769');
INSERT INTO `b_classification` VALUES (2, '电脑办公', '1723787992245');
INSERT INTO `b_classification` VALUES (3, '家用电器', '1723788000389');
INSERT INTO `b_classification` VALUES (4, '服装鞋包', '1723788008595');
INSERT INTO `b_classification` VALUES (5, '美妆个护', '1723788016415');
INSERT INTO `b_classification` VALUES (6, '家居家装', '1723788024440');
INSERT INTO `b_classification` VALUES (7, '食品生鲜', '1723788032052');
INSERT INTO `b_classification` VALUES (8, '运动户外', '1723788042052');
INSERT INTO `b_classification` VALUES (9, '母婴玩具', '1723788052052');
INSERT INTO `b_classification` VALUES (10, '图书音像', '1723788062052');

-- 重新插入标签数据（更新为综合商城标签）
INSERT INTO `b_tag` VALUES (1, '热销', '1723788327337');
INSERT INTO `b_tag` VALUES (2, '新品', '1723788333930');
INSERT INTO `b_tag` VALUES (3, '促销', '1723788341968');
INSERT INTO `b_tag` VALUES (4, '限时特惠', '1723788347848');
INSERT INTO `b_tag` VALUES (5, '品牌精选', '1723788353840');
INSERT INTO `b_tag` VALUES (6, '高端', '1723788361801');
INSERT INTO `b_tag` VALUES (7, '性价比', '1723788369919');
INSERT INTO `b_tag` VALUES (8, '时尚', '1723788376551');
INSERT INTO `b_tag` VALUES (9, '实用', '1723788384799');
INSERT INTO `b_tag` VALUES (10, '智能', '1723788392930');
INSERT INTO `b_tag` VALUES (11, '环保', '1723788402930');
INSERT INTO `b_tag` VALUES (12, '健康', '1723788412930');

-- 重新插入商品数据（更新为综合商城商品）
-- 手机数码类
INSERT INTO `b_thing` VALUES (1, 'iPhone 15 Pro 256GB 钛金属', 'iphone15.jpg', 'iPhone 15 Pro采用钛金属设计，搭载A17 Pro芯片，支持空间视频和空间音频，配备4800万像素主摄，支持USB-C接口，续航能力强大，是苹果最新旗舰手机。', '7999', '0', '1723789315765', '500', '0', '0', NULL, '0', NULL, 1);
INSERT INTO `b_thing` VALUES (2, '华为Mate 60 Pro 512GB 曜金黑', 'huawei_mate60.jpg', '华为Mate 60 Pro采用昆仑玻璃，搭载麒麟9000S芯片，支持卫星通信，配备5000万像素超感知摄像头，支持66W超级快充，是华为最新旗舰手机。', '6999', '0', '1723789373286', '300', '0', '0', NULL, '0', NULL, 1);
INSERT INTO `b_thing` VALUES (3, '三星Galaxy S24 Ultra 512GB 星河黑', 'samsung_s24.jpg', '三星Galaxy S24 Ultra采用钛金属边框，搭载骁龙8 Gen 3处理器，配备2亿像素主摄，支持S Pen触控笔，搭载Galaxy AI人工智能系统，是三星最新旗舰手机。', '8999', '0', '1723789413902', '200', '0', '0', NULL, '0', NULL, 1);

-- 电脑办公类
INSERT INTO `b_thing` VALUES (4, '联想小新Pro 16 2024款 i7/16GB/512GB', 'lenovo_xiaoxin.jpg', '联想小新Pro 16 2024款搭载第13代英特尔酷睿i7处理器，16GB内存，512GB固态硬盘，16英寸2.5K高清屏幕，支持WiFi 6，轻薄便携，是学生和办公人士的理想选择。', '5999', '0', '1723789513902', '150', '0', '0', NULL, '0', NULL, 2);
INSERT INTO `b_thing` VALUES (5, '戴尔XPS 13 Plus i7/16GB/1TB', 'dell_xps.jpg', '戴尔XPS 13 Plus采用全新设计，搭载第13代英特尔酷睿i7处理器，16GB内存，1TB固态硬盘，13.4英寸4K触控屏，支持雷电4接口，轻至1.24kg，是商务人士的理想选择。', '10999', '0', '1723789613902', '100', '0', '0', NULL, '0', NULL, 2);

-- 家用电器类
INSERT INTO `b_thing` VALUES (6, '美的智能变频空调 1.5匹 冷暖', 'midea_ac.jpg', '美的智能变频空调采用全直流变频技术，支持WiFi智能控制，APF能效等级一级，静音设计，快速制冷制热，自清洁功能，是家庭理想的空调选择。', '2499', '0', '1723789713902', '200', '0', '0', NULL, '0', NULL, 3);
INSERT INTO `b_thing` VALUES (7, '海尔滚筒洗衣机 10KG 洗烘一体', 'haier_washer.jpg', '海尔滚筒洗衣机采用直驱变频技术，支持洗烘一体，10KG大容量，智能投放，蒸汽除菌，静音设计，支持APP远程控制，是现代家庭的理想选择。', '3999', '0', '1723789813902', '150', '0', '0', NULL, '0', NULL, 3);

-- 服装鞋包类
INSERT INTO `b_thing` VALUES (8, '耐克Air Jordan 1 高帮篮球鞋', 'nike_aj1.jpg', '耐克Air Jordan 1 高帮篮球鞋采用经典设计，优质皮革鞋面，Air气垫缓震，橡胶外底提供出色抓地力，是篮球爱好者和潮流人士的必备选择。', '1299', '0', '1723789913902', '300', '0', '0', NULL, '0', NULL, 4);
INSERT INTO `b_thing` VALUES (9, '阿迪达斯三叶草经典休闲外套', 'adidas_jacket.jpg', '阿迪达斯三叶草经典休闲外套采用优质面料，经典三条纹设计，舒适贴合，多口袋设计，适合日常穿着和运动场合，是休闲时尚的理想选择。', '899', '0', '1723790013902', '200', '0', '0', NULL, '0', NULL, 4);

-- 美妆个护类
INSERT INTO `b_thing` VALUES (10, '兰蔻小黑瓶精华肌底液 100ml', 'lancome_genifique.jpg', '兰蔻小黑瓶精华肌底液含有7种益生菌提取物，能够强化肌肤屏障，提升肌肤光泽度，改善肤质，适合各种肤质，是护肤的必备选择。', '1050', '0', '1723790113902', '100', '0', '0', NULL, '0', NULL, 5);
INSERT INTO `b_thing` VALUES (11, '雅诗兰黛双棕瓶特润修护精华露 50ml', 'estee_lauder_serum.jpg', '雅诗兰黛双棕瓶特润修护精华露含有ChronoluxCB修护科技，能够修护肌肤，提升弹性，减少细纹，适合各种肤质，是抗老化护肤的理想选择。', '880', '0', '1723790213902', '80', '0', '0', NULL, '0', NULL, 5);

-- 家居家装类
INSERT INTO `b_thing` VALUES (12, '无印良品风格简约沙发 三人座', 'muji_sofa.jpg', '无印良品风格简约沙发采用优质亚麻面料，实木框架，高弹性海绵填充，舒适耐用，简约设计适合各种家居风格，是客厅的理想选择。', '2999', '0', '1723790313902', '50', '0', '0', NULL, '0', NULL, 6);
INSERT INTO `b_thing` VALUES (13, '北欧风格实木餐桌椅组合 一桌四椅', 'nordic_dining.jpg', '北欧风格实木餐桌椅组合采用优质白橡木材质，环保漆面，简约设计，耐用稳固，适合各种家居风格，是餐厅的理想选择。', '3499', '0', '1723790413902', '30', '0', '0', NULL, '0', NULL, 6);

-- 食品生鲜类
INSERT INTO `b_thing` VALUES (14, '五常有机稻花香大米 5kg', 'rice_organic.jpg', '五常有机稻花香大米产自黑龙江五常，采用有机种植方式，不使用化肥农药，口感软糯香甜，营养丰富，是家庭日常食用的理想选择。', '99', '0', '1723790513902', '500', '0', '0', NULL, '0', NULL, 7);
INSERT INTO `b_thing` VALUES (15, '新西兰进口牛排套餐 10片装', 'steak_nz.jpg', '新西兰进口牛排套餐包含西冷、眼肉、菲力等多种部位，草饲牛肉，肉质鲜嫩多汁，富含蛋白质，是牛排爱好者的理想选择。', '299', '0', '1723790613902', '100', '0', '0', NULL, '0', NULL, 7);

-- 运动户外类
INSERT INTO `b_thing` VALUES (16, '特步专业跑步鞋 轻量缓震', 'xtep_running.jpg', '特步专业跑步鞋采用轻量化设计，动态缓震技术，透气网面，防滑橡胶外底，适合日常跑步和健身，是运动爱好者的理想选择。', '299', '0', '1723790713902', '200', '0', '0', NULL, '0', NULL, 8);
INSERT INTO `b_thing` VALUES (17, '探路者户外登山包 40L', 'toread_backpack.jpg', '探路者户外登山包采用耐磨尼龙材质，人体工学背负系统，多口袋设计，防泼水处理，适合户外登山和旅行，是户外爱好者的理想选择。', '399', '0', '1723790813902', '150', '0', '0', NULL, '0', NULL, 8);

-- 母婴玩具类
INSERT INTO `b_thing` VALUES (18, '贝亲奶瓶套装 宽口径 玻璃', 'pigeon_bottle.jpg', '贝亲奶瓶套装采用高硼硅玻璃材质，耐高温，宽口径设计，仿真乳感奶嘴，防胀气系统，易清洁，是婴儿喂养的理想选择。', '199', '0', '1723790913902', '100', '0', '0', NULL, '0', NULL, 9);
INSERT INTO `b_thing` VALUES (19, '费雪益智早教玩具套装', 'fisher_price_toys.jpg', '费雪益智早教玩具套装包含多种感官刺激玩具，有助于宝宝视觉、听觉、触觉发展，安全无毒材质，适合0-3岁宝宝，是早期教育的理想选择。', '299', '0', '1723791013902', '80', '0', '0', NULL, '0', NULL, 9);

-- 图书音像类
INSERT INTO `b_thing` VALUES (20, '计算机网络教程(第4版)', 'computer_network.jpg', '《计算机网络教程》第4版由著名网络专家编写，全面介绍计算机网络基础知识和最新技术，包含大量实例和习题，适合计算机专业学生和IT从业人员学习参考。', '77', '0', '1723791113902', '200', '0', '0', NULL, '0', NULL, 10);
INSERT INTO `b_thing` VALUES (21, '算法精粹：经典计算机科学问题的Python实现', 'algorithm_python.jpg', '《算法精粹》通过Python语言实现经典算法，包括排序、搜索、图论、动态规划等，代码简洁易懂，配有详细解释和图示，适合编程爱好者和计算机专业学生。', '55', '0', '1723791213902', '150', '0', '0', NULL, '0', NULL, 10);

-- 商品标签关联
-- 手机数码
INSERT INTO `b_thing_tag` VALUES (1, 1, 2);  -- iPhone 15 Pro - 新品
INSERT INTO `b_thing_tag` VALUES (2, 1, 6);  -- iPhone 15 Pro - 高端
INSERT INTO `b_thing_tag` VALUES (3, 1, 10); -- iPhone 15 Pro - 智能
INSERT INTO `b_thing_tag` VALUES (4, 2, 1);  -- 华为Mate 60 Pro - 热销
INSERT INTO `b_thing_tag` VALUES (5, 2, 10); -- 华为Mate 60 Pro - 智能
INSERT INTO `b_thing_tag` VALUES (6, 3, 6);  -- 三星Galaxy S24 Ultra - 高端
INSERT INTO `b_thing_tag` VALUES (7, 3, 10); -- 三星Galaxy S24 Ultra - 智能

-- 电脑办公
INSERT INTO `b_thing_tag` VALUES (8, 4, 7);  -- 联想小新Pro - 性价比
INSERT INTO `b_thing_tag` VALUES (9, 4, 9);  -- 联想小新Pro - 实用
INSERT INTO `b_thing_tag` VALUES (10, 5, 6); -- 戴尔XPS - 高端
INSERT INTO `b_thing_tag` VALUES (11, 5, 8); -- 戴尔XPS - 时尚

-- 家用电器
INSERT INTO `b_thing_tag` VALUES (12, 6, 10); -- 美的空调 - 智能
INSERT INTO `b_thing_tag` VALUES (13, 6, 11); -- 美的空调 - 环保
INSERT INTO `b_thing_tag` VALUES (14, 7, 9);  -- 海尔洗衣机 - 实用
INSERT INTO `b_thing_tag` VALUES (15, 7, 10); -- 海尔洗衣机 - 智能

-- 服装鞋包
INSERT INTO `b_thing_tag` VALUES (16, 8, 1); -- 耐克Air Jordan - 热销
INSERT INTO `b_thing_tag` VALUES (17, 8, 8); -- 耐克Air Jordan - 时尚
INSERT INTO `b_thing_tag` VALUES (18, 9, 8); -- 阿迪达斯外套 - 时尚
INSERT INTO `b_thing_tag` VALUES (19, 9, 9); -- 阿迪达斯外套 - 实用

-- 美妆个护
INSERT INTO `b_thing_tag` VALUES (20, 10, 5); -- 兰蔻小黑瓶 - 品牌精选
INSERT INTO `b_thing_tag` VALUES (21, 10, 12); -- 兰蔻小黑瓶 - 健康
INSERT INTO `b_thing_tag` VALUES (22, 11, 5); -- 雅诗兰黛双棕瓶 - 品牌精选
INSERT INTO `b_thing_tag` VALUES (23, 11, 12); -- 雅诗兰黛双棕瓶 - 健康

-- 家居家装
INSERT INTO `b_thing_tag` VALUES (24, 12, 8); -- 无印良品沙发 - 时尚
INSERT INTO `b_thing_tag` VALUES (25, 12, 9); -- 无印良品沙发 - 实用
INSERT INTO `b_thing_tag` VALUES (26, 13, 8); -- 北欧餐桌椅 - 时尚
INSERT INTO `b_thing_tag` VALUES (27, 13, 11); -- 北欧餐桌椅 - 环保

-- 食品生鲜
INSERT INTO `b_thing_tag` VALUES (28, 14, 11); -- 有机大米 - 环保
INSERT INTO `b_thing_tag` VALUES (29, 14, 12); -- 有机大米 - 健康
INSERT INTO `b_thing_tag` VALUES (30, 15, 5); -- 新西兰牛排 - 品牌精选
INSERT INTO `b_thing_tag` VALUES (31, 15, 12); -- 新西兰牛排 - 健康

-- 运动户外
INSERT INTO `b_thing_tag` VALUES (32, 16, 7); -- 特步跑步鞋 - 性价比
INSERT INTO `b_thing_tag` VALUES (33, 16, 9); -- 特步跑步鞋 - 实用
INSERT INTO `b_thing_tag` VALUES (34, 17, 9); -- 探路者登山包 - 实用
INSERT INTO `b_thing_tag` VALUES (35, 17, 11); -- 探路者登山包 - 环保

-- 母婴玩具
INSERT INTO `b_thing_tag` VALUES (36, 18, 5); -- 贝亲奶瓶 - 品牌精选
INSERT INTO `b_thing_tag` VALUES (37, 18, 12); -- 贝亲奶瓶 - 健康
INSERT INTO `b_thing_tag` VALUES (38, 19, 9); -- 费雪玩具 - 实用
INSERT INTO `b_thing_tag` VALUES (39, 19, 12); -- 费雪玩具 - 健康

-- 图书音像
INSERT INTO `b_thing_tag` VALUES (40, 20, 9); -- 计算机网络教程 - 实用
INSERT INTO `b_thing_tag` VALUES (41, 21, 9); -- 算法精粹 - 实用 
