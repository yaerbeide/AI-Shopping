# AI-Shopping 智慧零售运营平台

> AI 驱动的全栈电商系统 | Spring Boot + Vue 3 前后端分离

## 📖 项目简介

本项目是一个完整的 B2C 电商商城管理系统，采用前后端分离架构，包含用户端商城和管理端后台两大模块。系统支持商品管理、订单处理、用户管理、优惠券营销等完整电商业务流程，并集成 AI 大模型能力，实现了智能商品档案生成、AI 导购推荐和智能文案生成等功能。

## 🛠️ 技术栈

### 后端
- **框架**：Spring Boot 2.5.5
- **ORM**：MyBatis-Plus 3.5.2
- **数据库**：MySQL 8.0 + HikariCP 连接池
- **工具**：Lombok、Gson、Commons-Lang3
- **AI 集成**：DeepSeek / 豆包多模态 / 即梦生图（火山方舟 API）

### 前端
- **框架**：Vue 3.2 + TypeScript
- **UI 组件库**：Ant Design Vue 3.2
- **状态管理**：Pinia 2.0
- **路由**：Vue Router 4.1
- **HTTP**：Axios 1.2
- **构建工具**：Vite 4.0
- **样式**：Less

## ✨ 核心功能

### 用户端（C 端）
- 商品浏览、搜索、分类、详情
- 购物车、下单、支付流程
- 用户中心（资料、地址、收藏、心愿单）
- 优惠券领取与使用
- 商品评论与点赞
- AI 智能导购推荐

### 管理后台（B 端）
- 商品管理（增删改查、上下架、标签）
- 订单管理（状态流转）
- 用户管理与权限控制
- 分类 / 标签 / Banner / 广告管理
- 优惠券配置（满减券 / 折扣券）
- 数据概览（销量统计、PV/UV、热门商品）
- 系统监控（操作日志、错误日志、登录日志）

### AI 智能模块
- **AI 商品档案生成**：自动生成商品摘要、关键词、适用场景、目标人群、核心卖点
- **AI 商品封面生成**：根据商品描述自动生成封面图
- **AI 商品文案生成**：一键生成营销文案
- **AI 智能导购**：自然语言提问，AI 推荐匹配商品
- **多模态视觉识别**：识别商品图片品类、外观、可见文字
- **三级降级策略**：视觉模型 → 文本模型 → 本地规则兜底
- **智能缓存**：基于 sourceHash 指纹，商品信息不变时不重复调用 AI

## 🏗️ 架构设计

- **MVC 分层架构**：Controller / Service / Mapper 三层分离
- **自定义注解权限**：`@Access` 注解 + 拦截器，管理员/普通用户两级权限
- **统一响应封装**：APIResponse 统一返回格式
- **全局异常处理**：CustomExceptionHandler 统一异常捕获
- **操作日志拦截**：全接口请求记录（IP、方法、URL、耗时）

## 📁 项目结构

```
shopping-manager/
├── server/              # 后端 Spring Boot 项目
│   ├── src/main/java/com/gk/study/
│   │   ├── ai/          # AI 模块（大模型集成）
│   │   ├── controller/  # 控制层
│   │   ├── service/     # 业务层
│   │   ├── mapper/      # 数据访问层
│   │   ├── entity/      # 实体类
│   │   ├── common/      # 通用类
│   │   ├── config/      # 配置类
│   │   ├── interceptor/ # 拦截器
│   │   ├── permission/  # 权限注解
│   │   └── utils/       # 工具类
│   └── src/main/resources/
│       ├── sql/         # 数据库脚本
│       └── application.properties
└── web/                 # 前端 Vue 3 项目
    ├── src/
    │   ├── api/         # 接口封装
    │   ├── views/       # 页面组件
    │   ├── router/      # 路由配置
    │   ├── store/       # Pinia 状态管理
    │   └── utils/       # 工具函数
    └── package.json
```

## 🚀 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端启动
```bash
cd server
# 1. 创建数据库并导入 sql/Shopping_mall.sql
# 2. 修改 application.properties 中的数据库连接信息
# 3. 启动
mvn spring-boot:run
```
后端默认端口：`9100`

### 前端启动
```bash
cd web
npm install
npm run dev
```
前端默认端口：`5173`

## 📊 数据库

系统共 15+ 张业务数据表，核心表包括：
- `b_user` - 用户表
- `b_thing` - 商品表
- `b_order` - 订单表
- `b_classification` - 分类表
- `b_tag` / `b_thing_tag` - 标签及关联
- `b_coupon` / `b_user_coupon` - 优惠券及用户关联
- `b_address` - 收货地址
- `b_comment` - 评论
- `b_banner` / `b_ad` - 轮播图/广告
- `b_op_log` - 操作日志
- `b_ai_product_profile` - AI 商品档案

## 📝 License

MIT
