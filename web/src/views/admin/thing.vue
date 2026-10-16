<template>
  <div class="product-container">
    <!-- 顶部统计卡片 -->
    <div class="statistics-cards">
      <a-row :gutter="[24, 24]">
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">商品总数</span>
              <shopping-outlined class="stat-icon-mini" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ totalProducts }}</h2>
              <span class="stat-desc">所有商品数量</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">已上架</span>
              <check-circle-outlined class="stat-icon-mini success" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getProductStatusCount('0') }}</h2>
              <span class="stat-desc">可购买商品</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">已下架</span>
              <stop-outlined class="stat-icon-mini warning" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getProductStatusCount('1') }}</h2>
              <span class="stat-desc">不可购买商品</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">库存不足</span>
              <warning-outlined class="stat-icon-mini danger" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getLowStockCount() }}</h2>
              <span class="stat-desc">库存低于10件</span>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选区域 -->
    <a-card :bordered="false" class="filter-card">
      <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
        <a-col :xs="24" :sm="12" :md="6" :lg="6" class="filter-col">
          <a-input
            v-model:value="data.keyword"
            placeholder="搜索商品名称"
            allowClear
            class="search-input"
            @change="onSearchChange"
            @pressEnter="onSearch"
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="4" class="filter-col">
          <a-select
            v-model:value="data.statusFilter"
            placeholder="商品状态"
            style="width: 100%"
            class="filter-select"
            allowClear
            @change="onSearch"
          >
            <a-select-option value="">全部状态</a-select-option>
            <a-select-option value="0">已上架</a-select-option>
            <a-select-option value="1">已下架</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="4" class="filter-col">
          <a-select
            v-model:value="data.classFilter"
            placeholder="商品分类"
            style="width: 100%"
            class="filter-select"
            allowClear
            @change="onSearch"
          >
            <a-select-option value="">全部分类</a-select-option>
            <a-select-option v-for="item in modal.cData" :key="item.id" :value="item.id">{{ item.title }}</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="10" class="filter-col">
          <div class="filter-btns">
            <a-button type="primary" @click="onSearch" class="btn-search"> <search-outlined />搜索 </a-button>
            <a-button @click="handleReset" class="btn-reset"> <reload-outlined />重置 </a-button>
            <a-button type="primary" @click="handleAdd" class="btn-add"> <plus-outlined />新增商品 </a-button>
            <a-button danger @click="handleBatchDelete" :disabled="data.selectedRowKeys.length === 0" class="btn-delete">
              <delete-outlined />批量删除
            </a-button>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        size="middle"
        rowKey="id"
        :loading="data.loading"
        :columns="columns"
        :data-source="filteredProducts"
        :scroll="{ x: 1480 }"
        :row-selection="rowSelection"
        :pagination="{
          size: 'default',
          current: data.page,
          pageSize: data.pageSize,
          onChange: (page, pageSize) => {
            data.page = page;
            data.pageSize = pageSize;
          },
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          showTotal: (total) => `共${total}条数据`,
        }"
        :rowClassName="getRowClassName"
      >
        <template #bodyCell="{ text, record, column }">
          <!-- 商品名称列 -->
          <template v-if="column.key === 'title'">
            <div class="product-cell">
              <div class="product-image">
                <img v-if="record.cover" :src="getImageUrl(record.cover)" alt="商品图片" />
                <div v-else class="no-image">
                  <file-image-outlined />
                </div>
              </div>
              <div class="product-info">
                <div class="product-name">{{ text }}</div>
                <div class="product-category" v-if="getCategoryName(record.classificationId)">
                  <tag-outlined /> {{ getCategoryName(record.classificationId) }}
                </div>
              </div>
            </div>
          </template>

          <!-- 价格列 -->
          <template v-if="column.key === 'price'">
            <div class="price-tag">¥{{ text }}</div>
          </template>

          <!-- 状态列 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="text === '0' ? 'success' : 'error'" class="status-tag">
              <template v-if="text === '0'"> <check-circle-outlined /> 已上架 </template>
              <template v-else> <stop-outlined /> 已下架 </template>
            </a-tag>
          </template>

          <!-- 库存列 -->
          <template v-if="column.key === 'repertory'">
            <a-progress :percent="getStockPercent(text)" :status="getStockStatus(text)" :stroke-color="getStockColor(text)" size="small" />
            <div class="stock-count">
              <span :class="{ 'stock-warning': text < 10 }">{{ text }}</span> 件
            </div>
          </template>

          <!-- 简介列 -->
          <template v-if="column.key === 'description'">
            <div class="description-cell" :title="text || '--'">
              {{ text || '--' }}
            </div>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'operation'">
            <div class="action-btns">
              <a-button size="small" @click="handleOpenAiProfile(record)" class="btn-profile"> <file-search-outlined />AI档案 </a-button>
              <a-button type="primary" size="small" @click="handleEdit(record)" class="btn-edit"> <edit-outlined />编辑 </a-button>
              <a-button danger size="small" @click="showDeleteConfirm(record)" class="btn-delete"> <delete-outlined />删除 </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!--商品编辑弹窗-->
    <a-modal
      v-model:visible="modal.visible"
      :forceRender="true"
      :title="modal.title"
      width="880px"
      ok-text="确认"
      cancel-text="取消"
      @cancel="handleCancel"
      @ok="handleOk"
      class="product-modal"
    >
      <div class="modal-content">
        <a-form ref="myform" :model="modal.form" :rules="modal.rules" layout="vertical">
          <a-tabs>
            <a-tab-pane key="1" tab="基本信息">
              <a-row :gutter="24">
                <a-col :span="24">
                  <a-form-item label="商品名称" name="title">
                    <a-input placeholder="请输入商品名称" v-model:value="modal.form.title" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="商品分类" name="classificationId">
                    <a-select
                      placeholder="请选择分类"
                      allowClear
                      :options="modal.cData"
                      :field-names="{ label: 'title', value: 'id' }"
                      v-model:value="modal.form.classificationId"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="商品标签">
                    <a-select mode="multiple" placeholder="请选择标签" allowClear v-model:value="modal.form.tags">
                      <a-select-option v-for="item in modal.tagData" :key="item.id" :value="item.id">{{ item.title }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="商品价格" name="price">
                    <a-input-number
                      placeholder="请输入价格"
                      :min="0"
                      :precision="2"
                      v-model:value="modal.form.price"
                      style="width: 100%"
                      addon-before="¥"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="库存数量" name="repertory">
                    <a-input-number
                      placeholder="请输入库存"
                      :min="0"
                      v-model:value="modal.form.repertory"
                      style="width: 100%"
                      addon-after="件"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="商品状态" name="status">
                    <a-radio-group v-model:value="modal.form.status" button-style="solid">
                      <a-radio-button value="0"> <check-circle-outlined /> 上架 </a-radio-button>
                      <a-radio-button value="1"> <stop-outlined /> 下架 </a-radio-button>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <div class="ai-copy-box">
                    <div class="ai-copy-info">
                      <strong>AI 商品文案助手</strong>
                      <span>输入关键词后生成标题、详情、卖点、规格、分类和标签建议。</span>
                    </div>
                    <div class="ai-copy-actions">
                      <a-input
                        v-model:value="aiCopy.keywords"
                        placeholder="关键词，如：轻薄,办公,长续航"
                        allow-clear
                        @pressEnter="handleAiCopyGenerate"
                      />
                      <a-button type="primary" :loading="aiCopy.loading" @click="handleAiCopyGenerate">
                        AI 生成文案
                      </a-button>
                    </div>
                  </div>
                </a-col>
              </a-row>
            </a-tab-pane>
            <a-tab-pane key="2" tab="商品详情">
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="商品封面">
                    <div class="upload-container">
                      <a-upload-dragger
                        name="file"
                        accept="image/*"
                        :multiple="false"
                        :before-upload="beforeUpload"
                        v-model:file-list="fileList"
                        class="upload-area"
                      >
                        <div class="upload-content">
                          <template v-if="modal.form.coverUrl">
                            <img :src="modal.form.coverUrl" class="preview-image" />
                          </template>
                          <template v-else>
                            <p class="upload-icon">
                              <file-image-outlined />
                            </p>
                            <p class="upload-text">点击或拖拽图片上传商品封面</p>
                          </template>
                        </div>
                      </a-upload-dragger>
                    </div>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="商品简介">
                    <a-textarea placeholder="请输入商品简介" v-model:value="modal.form.description" :rows="6" :maxlength="500" show-count />
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <div class="ai-image-box">
                    <div class="ai-image-head">
                      <div>
                        <strong>AI 商品图生成</strong>
                        <span>根据商品名称、简介和卖点生成封面候选图。</span>
                      </div>
                      <a-button type="primary" :loading="aiImage.loading" @click="handleAiImageGenerate">AI 生成图片</a-button>
                    </div>
                    <div class="ai-image-controls">
                      <a-select v-model:value="aiImage.usage" placeholder="图片用途">
                        <a-select-option value="cover">商品封面</a-select-option>
                        <a-select-option value="poster">宣传海报</a-select-option>
                        <a-select-option value="detail">详情首图</a-select-option>
                      </a-select>
                      <a-select v-model:value="aiImage.style" placeholder="图片风格">
                        <a-select-option value="clean-business">简洁商务</a-select-option>
                        <a-select-option value="ecommerce-poster">电商海报</a-select-option>
                        <a-select-option value="premium">高端质感</a-select-option>
                        <a-select-option value="lifestyle">生活场景</a-select-option>
                        <a-select-option value="tech">科技感</a-select-option>
                      </a-select>
                      <a-select v-model:value="aiImage.ratio" placeholder="画面比例">
                        <a-select-option value="1:1">1:1</a-select-option>
                        <a-select-option value="4:3">4:3</a-select-option>
                        <a-select-option value="16:9">16:9</a-select-option>
                      </a-select>
                      <a-select v-model:value="aiImage.count" placeholder="生成数量">
                        <a-select-option :value="1">1 张</a-select-option>
                        <a-select-option :value="2">2 张</a-select-option>
                        <a-select-option :value="4">4 张</a-select-option>
                      </a-select>
                    </div>
                    <a-alert
                      v-if="aiImage.result && aiImage.result.fallback"
                      type="warning"
                      show-icon
                      message="AI 图片生成未启用或调用失败，请检查 ai.image.* 配置。"
                      class="ai-image-alert"
                    />
                    <div v-if="aiImage.images.length" class="ai-image-result">
                      <div v-for="(item, index) in aiImage.images" :key="index" class="ai-image-card">
                        <img :src="getAiImageSrc(item)" alt="AI 生成商品图" />
                        <div class="ai-image-card-actions">
                          <a-button size="small" type="primary" @click="applyAiImage(item, index)">设为封面</a-button>
                        </div>
                      </div>
                    </div>
                    <p class="ai-image-tip">AI 生成图仅作为展示建议，请确认图片与实际商品一致后再设为封面。</p>
                  </div>
                </a-col>
              </a-row>
            </a-tab-pane>
          </a-tabs>
        </a-form>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="aiCopy.previewVisible"
      title="AI 商品文案预览"
      width="760px"
      ok-text="应用到商品表单"
      cancel-text="关闭"
      @ok="applyAiCopy"
    >
      <div v-if="aiCopy.result" class="ai-copy-preview">
        <a-alert
          v-if="aiCopy.result.fallback"
          type="warning"
          show-icon
          message="当前为规则兜底文案，请人工核对后再应用。"
          style="margin-bottom: 16px"
        />

        <section>
          <h3>商品标题</h3>
          <p>{{ aiCopy.result.title }}</p>
        </section>

        <section>
          <h3>核心卖点</h3>
          <ul>
            <li v-for="item in aiCopy.result.highlights || []" :key="item">{{ item }}</li>
          </ul>
        </section>

        <section>
          <h3>详情文案</h3>
          <p>{{ aiCopy.result.description }}</p>
        </section>

        <section v-if="aiCopy.result.specs">
          <h3>规格参数建议</h3>
          <div class="spec-list">
            <div v-for="(value, key) in aiCopy.result.specs" :key="key">
              <span>{{ key }}</span>
              <strong>{{ value }}</strong>
            </div>
          </div>
        </section>

        <section v-if="aiCopy.result.classificationSuggestion">
          <h3>分类建议</h3>
          <p>
            {{ aiCopy.result.classificationSuggestion.title }}
            <span v-if="aiCopy.result.classificationSuggestion.confidence">
              （置信度 {{ Math.round(aiCopy.result.classificationSuggestion.confidence * 100) }}%）
            </span>
          </p>
        </section>

        <section v-if="aiCopy.result.tagSuggestions && aiCopy.result.tagSuggestions.length">
          <h3>标签建议</h3>
          <div class="chip-list">
            <span v-for="item in aiCopy.result.tagSuggestions" :key="item">{{ item }}</span>
          </div>
        </section>

        <section v-if="aiCopy.result.seoKeywords && aiCopy.result.seoKeywords.length">
          <h3>SEO 关键词</h3>
          <div class="chip-list muted">
            <span v-for="item in aiCopy.result.seoKeywords" :key="item">{{ item }}</span>
          </div>
        </section>

        <section v-if="aiCopy.result.riskTips && aiCopy.result.riskTips.length">
          <h3>风险提示</h3>
          <ul>
            <li v-for="item in aiCopy.result.riskTips" :key="item">{{ item }}</li>
          </ul>
        </section>
      </div>
    </a-modal>

    <a-modal v-model:visible="aiProfile.visible" title="商品 AI 档案" width="820px" :footer="null" class="ai-profile-modal">
      <div class="ai-profile-panel">
        <div class="ai-profile-head">
          <div>
            <span class="profile-kicker">AI PRODUCT PROFILE</span>
            <h3>{{ aiProfile.product?.title || '当前商品' }}</h3>
            <p>用于智能导购召回和推荐理由生成，商品信息越完整，档案越准确。</p>
          </div>
          <div class="profile-actions">
            <a-button :loading="aiProfile.loading" @click="handleGenerateProfile(false)">
              <thunderbolt-outlined />生成档案
            </a-button>
            <a-button type="primary" :loading="aiProfile.loading" @click="handleGenerateProfile(true)">重新生成</a-button>
          </div>
        </div>

        <a-spin :spinning="aiProfile.loading">
          <a-empty v-if="!aiProfile.data" description="暂无 AI 档案，请点击生成档案" />
          <div v-else class="profile-content">
            <a-alert
              v-if="aiProfile.data.profile && aiProfile.data.profile.status === 'FALLBACK'"
              type="warning"
              show-icon
              message="当前档案由规则兜底生成，建议配置模型 API 后重新生成。"
              style="margin-bottom: 14px"
            />

            <div class="profile-score-card">
              <span>资料完整度</span>
              <strong>{{ aiProfile.data.contentQualityScore || 0 }}</strong>
              <em>/100</em>
            </div>

            <section v-if="aiProfile.data.summary">
              <h4>导购摘要</h4>
              <p>{{ aiProfile.data.summary }}</p>
            </section>

            <section>
              <h4>关键词</h4>
              <div class="profile-chip-list">
                <span v-for="item in aiProfile.data.keywords || []" :key="item">{{ item }}</span>
              </div>
            </section>

            <section>
              <h4>适用场景</h4>
              <div class="profile-chip-list dark">
                <span v-for="item in aiProfile.data.scenes || []" :key="item">{{ item }}</span>
              </div>
            </section>

            <section>
              <h4>目标人群</h4>
              <div class="profile-chip-list warm">
                <span v-for="item in aiProfile.data.targetUsers || []" :key="item">{{ item }}</span>
              </div>
            </section>

            <section>
              <h4>核心卖点</h4>
              <ul>
                <li v-for="item in aiProfile.data.sellingPoints || []" :key="item">{{ item }}</li>
              </ul>
            </section>

            <section v-if="aiProfile.data.specs">
              <h4>规格摘要</h4>
              <div class="profile-spec-grid">
                <div v-for="(value, key) in aiProfile.data.specs" :key="key">
                  <span>{{ key }}</span>
                  <strong>{{ value }}</strong>
                </div>
              </div>
            </section>

            <section v-if="aiProfile.data.imageSummary">
              <h4>图片摘要</h4>
              <p>{{ aiProfile.data.imageSummary }}</p>
            </section>

            <section v-if="aiProfile.data.guideHints && aiProfile.data.guideHints.length">
              <h4>导购提示</h4>
              <ul>
                <li v-for="item in aiProfile.data.guideHints" :key="item">{{ item }}</li>
              </ul>
            </section>
          </div>
        </a-spin>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message, Modal } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi } from '/@/api/thing';
  import { productCopyApi, productImageGenerateApi, productProfileDetailApi, productProfileGenerateApi } from '/@/api/ai';
  import { listApi as listClassificationApi } from '/@/api/classification';
  import { listApi as listTagApi } from '/@/api/tag';
  import { BASE_URL } from '/@/store/constants';
  import {
    FileImageOutlined,
    ShoppingOutlined,
    CheckCircleOutlined,
    StopOutlined,
    WarningOutlined,
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    DeleteOutlined,
    EditOutlined,
    TagOutlined,
    FileSearchOutlined,
    ThunderboltOutlined,
  } from '@ant-design/icons-vue';
  import { computed, onMounted, reactive, ref } from 'vue';

  const columns = reactive([
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      width: 80,
      align: 'center',
    },
    {
      title: '商品信息',
      dataIndex: 'title',
      key: 'title',
      width: 300,
      ellipsis: true,
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      align: 'right',
      sorter: (a, b) => a.price - b.price,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      align: 'center',
      filters: [
        { text: '已上架', value: '0' },
        { text: '已下架', value: '1' },
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: '库存',
      dataIndex: 'repertory',
      key: 'repertory',
      width: 150,
      align: 'center',
      sorter: (a, b) => a.repertory - b.repertory,
    },
    {
      title: '简介',
      dataIndex: 'description',
      key: 'description',
      width: 250,
      ellipsis: true,
      customRender: ({ text }) => text || '--',
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 270,
    },
  ]);

  const beforeUpload = (file: File) => {
    // 改文件名
    const fileName = new Date().getTime().toString() + '.' + file.type.substring(6);
    const copyFile = new File([file], fileName);
    console.log(copyFile);
    modal.form.imageFile = copyFile;

    // 创建预览URL
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      modal.form.coverUrl = reader.result as string;
    };

    return false;
  };

  // 文件列表
  const fileList = ref<any[]>([]);

  // 页面数据
  const data = reactive({
    dataList: [],
    loading: false,
    keyword: '',
    statusFilter: '',
    classFilter: '',
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 计算总商品数
  const totalProducts = computed(() => {
    return data.dataList.length;
  });

  // 根据状态筛选商品
  const filteredProducts = computed(() => {
    let result = [...data.dataList];

    // 根据关键字筛选
    if (data.keyword) {
      const keyword = data.keyword.toLowerCase();
      result = result.filter(
        (item) =>
          (item.title && item.title.toLowerCase().includes(keyword)) ||
          (item.description && item.description.toLowerCase().includes(keyword)),
      );
    }

    // 根据状态筛选
    if (data.statusFilter) {
      result = result.filter((item) => item.status === data.statusFilter);
    }

    // 根据分类筛选
    if (data.classFilter) {
      result = result.filter((item) => item.classificationId === data.classFilter);
    }

    return result;
  });

  // 获取商品状态数量
  const getProductStatusCount = (status) => {
    return data.dataList.filter((item) => item.status === status).length;
  };

  // 获取库存不足商品数量
  const getLowStockCount = () => {
    return data.dataList.filter((item) => item.repertory < 10 && item.status === '0').length;
  };

  // 获取行样式
  const getRowClassName = (record) => {
    if (record.status === '1') {
      return 'product-row-inactive';
    }
    if (record.repertory < 10) {
      return 'product-row-low-stock';
    }
    return '';
  };

  // 获取库存百分比
  const getStockPercent = (stock) => {
    if (stock <= 0) return 0;
    if (stock >= 100) return 100;
    return stock;
  };

  // 获取库存状态
  const getStockStatus = (stock) => {
    if (stock <= 0) return 'exception';
    if (stock < 10) return 'warning';
    return 'normal';
  };

  // 获取库存颜色
  const getStockColor = (stock) => {
    if (stock <= 0) return '#ff4d4f';
    if (stock < 10) return '#faad14';
    if (stock < 50) return '#1890ff';
    return '#52c41a';
  };

  // 获取图片URL
  const getImageUrl = (cover) => {
    return cover ? `${BASE_URL}/api/staticfiles/image/${cover}` : '';
  };

  // 获取分类名称
  const getCategoryName = (id) => {
    const category = modal.cData.find((item) => item.id === id);
    return category ? category.title : '';
  };

  // 弹窗数据源
  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    cData: [],
    tagData: [{}],
    form: {
      id: undefined,
      title: undefined,
      classificationId: undefined,
      tags: [],
      repertory: undefined,
      price: undefined,
      status: undefined,
      cover: undefined,
      coverUrl: undefined,
      imageFile: undefined,
      description: undefined,
    },
    rules: {
      title: [{ required: true, message: '请输入商品名称', trigger: 'change' }],
      classificationId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
      repertory: [{ required: true, message: '请输入库存数量', trigger: 'change' }],
      price: [{ required: true, message: '请输入商品价格', trigger: 'change' }],
      status: [{ required: true, message: '请选择商品状态', trigger: 'change' }],
    },
  });

  const aiCopy = reactive({
    keywords: '',
    loading: false,
    previewVisible: false,
    result: null as any,
  });

  const aiImage = reactive({
    usage: 'cover',
    style: 'clean-business',
    ratio: '1:1',
    count: 1,
    loading: false,
    result: null as any,
    images: [] as any[],
  });

  const aiProfile = reactive({
    visible: false,
    loading: false,
    product: null as any,
    data: null as any,
  });

  const myform = ref<FormInstance>();

  onMounted(() => {
    getDataList();
    getCDataList();
    getTagDataList();
  });

  const getDataList = () => {
    data.loading = true;
    listApi({
      keyword: data.keyword,
    })
      .then((res) => {
        data.loading = false;
        console.log(res);
        if (res.data) {
          res.data.forEach((item: any, index: any) => {
            item.index = index + 1;
          });
          data.dataList = res.data;
        }
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  const getCDataList = () => {
    listClassificationApi({}).then((res) => {
      modal.cData = res.data || [];
    });
  };

  const getTagDataList = () => {
    listTagApi({}).then((res) => {
      if (res.data) {
        res.data.forEach((item, index) => {
          item.index = index + 1;
        });
        modal.tagData = res.data;
      }
    });
  };

  const onSearchChange = (e: Event) => {
    data.keyword = e?.target?.value;
    console.log(data.keyword);
  };

  const onSearch = () => {
    getDataList();
  };

  const handleReset = () => {
    data.keyword = '';
    data.statusFilter = '';
    data.classFilter = '';
    data.page = 1;
    getDataList();
  };

  const rowSelection = ref({
    onChange: (selectedRowKeys: (string | number)[], selectedRows: any[]) => {
      console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
      data.selectedRowKeys = selectedRowKeys;
    },
  });

  const handleAdd = () => {
    resetModal();
    modal.visible = true;
    modal.editFlag = false;
    modal.title = '新增商品';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    modal.form.status = '0'; // 默认上架
    modal.form.cover = undefined;
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '编辑商品';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    for (const key in record) {
      if (record[key]) {
        modal.form[key] = record[key];
      }
    }
    if (modal.form.cover) {
      modal.form.coverUrl = BASE_URL + '/api/staticfiles/image/' + modal.form.cover;
      modal.form.cover = undefined;
    }
  };

  const showDeleteConfirm = (record: any) => {
    Modal.confirm({
      title: '确定要删除此商品吗?',
      content: '删除后无法恢复，请谨慎操作',
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        confirmDelete(record);
      },
    });
  };

  const confirmDelete = (record: any) => {
    console.log('delete', record);
    deleteApi({ ids: record.id })
      .then((res) => {
        message.success('删除成功');
        getDataList();
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleBatchDelete = () => {
    console.log(data.selectedRowKeys);
    if (data.selectedRowKeys.length <= 0) {
      message.warn('请勾选要删除的商品');
      return;
    }

    Modal.confirm({
      title: `确定要删除选中的 ${data.selectedRowKeys.length} 个商品吗?`,
      content: '删除后无法恢复，请谨慎操作',
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        deleteApi({ ids: data.selectedRowKeys.join(',') })
          .then((res) => {
            message.success('批量删除成功');
            data.selectedRowKeys = [];
            getDataList();
          })
          .catch((err) => {
            message.error(err.msg || '操作失败');
          });
      },
    });
  };

  const handleOpenAiProfile = (record: any) => {
    aiProfile.visible = true;
    aiProfile.product = record;
    aiProfile.data = null;
    if (!record || !record.id) return;

    aiProfile.loading = true;
    productProfileDetailApi({ thingId: record.id })
      .then((res) => {
        aiProfile.data = res.data || null;
      })
      .catch((err) => {
        console.log(err);
        message.error(err.msg || 'AI 档案读取失败');
      })
      .finally(() => {
        aiProfile.loading = false;
      });
  };

  const handleGenerateProfile = (force = false) => {
    if (!aiProfile.product || !aiProfile.product.id) {
      message.warning('请先选择商品');
      return;
    }

    aiProfile.loading = true;
    productProfileGenerateApi({
      thingId: aiProfile.product.id,
      force,
    })
      .then((res) => {
        aiProfile.data = res.data;
        message.success(force ? 'AI 档案已重新生成' : 'AI 档案已生成');
      })
      .catch((err) => {
        console.log(err);
        message.error(err.msg || 'AI 档案生成失败');
      })
      .finally(() => {
        aiProfile.loading = false;
      });
  };

  const handleAiCopyGenerate = () => {
    if (!modal.visible) return;
    if (!modal.form.title && !aiCopy.keywords) {
      message.warning('请先输入商品名称或 AI 关键词');
      return;
    }

    aiCopy.loading = true;
    productCopyApi({
      productName: modal.form.title,
      keywords: aiCopy.keywords,
      price: modal.form.price,
      classificationId: modal.form.classificationId,
      description: modal.form.description,
    })
      .then((res) => {
        aiCopy.result = res.data;
        aiCopy.previewVisible = true;
      })
      .catch((err) => {
        console.log(err);
        message.error(err.msg || 'AI 文案生成失败');
      })
      .finally(() => {
        aiCopy.loading = false;
      });
  };

  const applyAiCopy = () => {
    const result = aiCopy.result;
    if (!result) return;

    if (result.title) {
      modal.form.title = result.title;
    }
    if (result.description) {
      modal.form.description = result.description;
    }
    if (result.classificationSuggestion && result.classificationSuggestion.id) {
      const exists = modal.cData.some((item: any) => String(item.id) === String(result.classificationSuggestion.id));
      if (exists) {
        modal.form.classificationId = result.classificationSuggestion.id;
      }
    }

    const matchedTagIds = matchTagSuggestionIds(result.tagSuggestions || []);
    if (matchedTagIds.length) {
      modal.form.tags = matchedTagIds;
    }

    aiCopy.previewVisible = false;
    message.success('AI 文案已应用到当前商品表单，请核对后保存');
  };

  const handleAiImageGenerate = () => {
    if (!modal.visible) return;
    if (!modal.form.title && !modal.form.description && !aiCopy.keywords) {
      message.warning('请先填写商品名称、简介或 AI 关键词');
      return;
    }

    aiImage.loading = true;
    productImageGenerateApi({
      productName: modal.form.title,
      keywords: aiCopy.keywords,
      price: modal.form.price,
      classificationId: modal.form.classificationId,
      classificationTitle: getCategoryName(modal.form.classificationId),
      description: modal.form.description,
      highlights: aiCopy.result?.highlights || [],
      specs: aiCopy.result?.specs || {},
      usage: aiImage.usage,
      style: aiImage.style,
      ratio: aiImage.ratio,
      count: aiImage.count,
    })
      .then((res) => {
        aiImage.result = res.data;
        aiImage.images = res.data?.images || [];
        if (aiImage.images.length) {
          message.success('AI 商品图已生成，请选择一张设为封面');
        } else {
          message.warning('暂未生成可用图片，请检查图像模型配置');
        }
      })
      .catch((err) => {
        console.log(err);
        message.error(err.msg || 'AI 图片生成失败');
      })
      .finally(() => {
        aiImage.loading = false;
      });
  };

  const getAiImageSrc = (item: any) => {
    if (!item) return '';
    if (item.url) return item.url;
    if (item.base64 && item.base64.startsWith('data:image/')) return item.base64;
    if (item.base64) return `data:image/png;base64,${item.base64}`;
    return '';
  };

  const applyAiImage = async (item: any, index: number) => {
    const src = getAiImageSrc(item);
    if (!src) {
      message.warning('当前图片地址无效');
      return;
    }
    try {
      const file = await imageSourceToFile(src, `ai-product-cover-${Date.now()}-${index}.png`);
      modal.form.imageFile = file;
      modal.form.coverUrl = URL.createObjectURL(file);
      fileList.value = [
        {
          uid: `ai-${Date.now()}`,
          name: file.name,
          status: 'done',
          url: modal.form.coverUrl,
        },
      ];
      message.success('AI 商品图已设为封面，请保存商品后生效');
    } catch (error) {
      console.log(error);
      message.error('设置 AI 商品图失败，请重新生成后再试');
    }
  };

  const imageSourceToFile = async (src: string, fileName: string) => {
    if (src.startsWith('data:image/')) {
      const response = await fetch(src);
      const blob = await response.blob();
      return new File([blob], fileName, { type: blob.type || 'image/png' });
    }
    const response = await fetch(src);
    const blob = await response.blob();
    return new File([blob], fileName, { type: blob.type || 'image/png' });
  };

  const matchTagSuggestionIds = (suggestions: string[]) => {
    const ids: any[] = [];
    suggestions.forEach((name) => {
      const matched = modal.tagData.find((tag: any) => tag && tag.title && String(tag.title).toLowerCase() === String(name).toLowerCase());
      if (matched && matched.id && !ids.includes(matched.id)) {
        ids.push(matched.id);
      }
    });
    return ids;
  };

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        const formData = new FormData();
        if (modal.editFlag) {
          formData.append('id', modal.form.id);
        }
        formData.append('title', modal.form.title);
        if (modal.form.classificationId) {
          formData.append('classificationId', modal.form.classificationId);
        }
        if (modal.form.tags) {
          modal.form.tags.forEach(function (value) {
            if (value) {
              formData.append('tags[]', value);
            }
          });
        }
        if (modal.form.imageFile) {
          formData.append('imageFile', modal.form.imageFile);
        }
        formData.append('description', modal.form.description || '');
        formData.append('price', modal.form.price || '');
        if (modal.form.repertory >= 0) {
          formData.append('repertory', modal.form.repertory);
        }
        if (modal.form.status) {
          formData.append('status', modal.form.status);
        }
        if (modal.editFlag) {
          updateApi(formData)
            .then((res) => {
              hideModal();
              message.success('更新商品成功');
              getDataList();
            })
            .catch((err) => {
              console.log(err);
              message.error(err.msg || '操作失败');
            });
        } else {
          createApi(formData)
            .then((res) => {
              hideModal();
              message.success('新增商品成功');
              getDataList();
            })
            .catch((err) => {
              console.log(err);
              message.error(err.msg || '操作失败');
            });
        }
      })
      .catch((err) => {
        console.log('表单校验失败');
      });
  };

  const handleCancel = () => {
    hideModal();
  };

  // 恢复表单初始状态
  const resetModal = () => {
    myform.value?.resetFields();
    fileList.value = [];
    aiCopy.keywords = '';
    aiCopy.result = null;
    aiCopy.previewVisible = false;
    aiImage.usage = 'cover';
    aiImage.style = 'clean-business';
    aiImage.ratio = '1:1';
    aiImage.count = 1;
    aiImage.result = null;
    aiImage.images = [];
  };

  // 关闭弹窗
  const hideModal = () => {
    modal.visible = false;
  };
</script>

<style scoped lang="less">
  .product-container {
    min-height: 100%;
  }

  .ai-copy-box {
    margin-top: 8px;
    padding: 16px;
    border: 1px solid #e6ebf5;
    border-radius: 14px;
    background: #f8faff;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(320px, 520px);
    gap: 16px;
    align-items: center;

    .ai-copy-info {
      display: flex;
      flex-direction: column;
      gap: 4px;

      strong {
        color: #1b2559;
        font-size: 15px;
        font-weight: 800;
      }

      span {
        color: #7b8499;
        font-size: 13px;
      }
    }

    .ai-copy-actions {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 10px;

      :deep(.ant-input) {
        border-radius: 10px;
      }

      :deep(.ant-btn) {
        border-radius: 10px;
        font-weight: 700;
      }
    }
  }

  .ai-copy-preview {
    display: grid;
    gap: 14px;

    section {
      padding: 14px;
      border: 1px solid #edf0f6;
      border-radius: 12px;
      background: #fbfcff;

      h3 {
        margin: 0 0 8px;
        color: #1b2559;
        font-size: 14px;
        font-weight: 800;
      }

      p,
      ul {
        margin: 0;
        color: #404a63;
        line-height: 1.7;
      }

      ul {
        padding-left: 18px;
      }
    }

    .spec-list {
      display: grid;
      gap: 8px;

      div {
        display: grid;
        grid-template-columns: 120px minmax(0, 1fr);
        gap: 12px;
      }

      span {
        color: #7b8499;
      }

      strong {
        color: #34405c;
        font-weight: 600;
      }
    }

    .chip-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      span {
        padding: 4px 10px;
        border-radius: 999px;
        background: #eef4ff;
        color: #2f6fed;
        font-size: 12px;
        font-weight: 700;
      }

      &.muted span {
        background: #f2f4f8;
        color: #667085;
      }
    }
  }

  /* 统计卡片样式 */
  .ai-profile-modal {
    :deep(.ant-modal-content) {
      border-radius: 22px;
      overflow: hidden;
    }

    :deep(.ant-modal-header) {
      padding: 22px 26px 8px;
      border-bottom: none;
    }

    :deep(.ant-modal-body) {
      padding: 18px 26px 26px;
    }
  }

  .ai-profile-panel {
    .ai-profile-head {
      display: flex;
      justify-content: space-between;
      gap: 18px;
      padding: 18px;
      border-radius: 18px;
      background: linear-gradient(135deg, #111827 0%, #2b3342 100%);
      color: #fff;
      margin-bottom: 18px;

      h3 {
        margin: 4px 0 8px;
        color: #fff;
        font-size: 22px;
        font-weight: 900;
      }

      p {
        margin: 0;
        color: rgba(255, 255, 255, 0.72);
        line-height: 1.7;
      }
    }

    .profile-kicker {
      color: #ffd100;
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.16em;
    }

    .profile-actions {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      flex-shrink: 0;

      :deep(.ant-btn) {
        border-radius: 10px;
        font-weight: 800;
      }
    }

    .profile-content {
      display: grid;
      gap: 14px;

      section {
        padding: 15px;
        border: 1px solid #edf0f6;
        border-radius: 14px;
        background: #fbfcff;

        h4 {
          margin: 0 0 10px;
          color: #1b2559;
          font-size: 14px;
          font-weight: 900;
        }

        p,
        ul {
          margin: 0;
          color: #404a63;
          line-height: 1.75;
        }

        ul {
          padding-left: 18px;
        }
      }
    }

    .profile-score-card {
      height: 86px;
      padding: 0 18px;
      border-radius: 16px;
      background: #f4f7fe;
      display: flex;
      align-items: center;
      gap: 8px;

      span {
        color: #667085;
        font-weight: 800;
        margin-right: auto;
      }

      strong {
        color: #1b2559;
        font-size: 38px;
        line-height: 1;
        font-weight: 900;
      }

      em {
        color: #98a2b3;
        font-style: normal;
        font-weight: 800;
      }
    }

    .profile-chip-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      span {
        padding: 6px 12px;
        border-radius: 999px;
        background: #eef4ff;
        color: #2f6fed;
        font-size: 12px;
        font-weight: 800;
      }

      &.dark span {
        background: #f2f4f7;
        color: #344054;
      }

      &.warm span {
        background: #fff7e6;
        color: #b76e00;
      }
    }

    .profile-spec-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px;

      div {
        padding: 12px;
        border-radius: 12px;
        background: #f7f9fc;
        display: grid;
        gap: 4px;
      }

      span {
        color: #98a2b3;
        font-size: 12px;
        font-weight: 800;
      }

      strong {
        color: #344054;
        font-weight: 700;
      }
    }
  }

  .statistics-cards {
    margin-bottom: 24px;
  }

  .stat-card {
    background: #fff;
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.12);
    height: 100%;

    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .stat-title {
        font-size: 14px;
        color: #a3aed0;
        font-weight: 700;
      }

      .stat-icon-mini {
        font-size: 20px;
        color: #4318ff;

        &.success {
          color: #05cd99;
        }
        &.warning {
          color: #ffb547;
        }
        &.danger {
          color: #ee5d50;
        }
      }
    }

    .stat-body {
      .stat-value {
        font-size: 28px;
        font-weight: 800;
        color: #1b2559;
        margin: 0;
        line-height: 1.2;
      }

      .stat-desc {
        font-size: 12px;
        color: #a3aed0;
        font-weight: 500;
        margin-top: 4px;
        display: block;
      }
    }
  }

  /* 筛选卡片样式 */
  .filter-card {
    margin-bottom: 24px;
    border-radius: 20px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.08);

    .filter-btns {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      flex-wrap: wrap;

      button {
        display: flex;
        align-items: center;
        gap: 4px;
        border-radius: 12px;
        height: 40px;
        padding: 0 20px;
        font-weight: 600;
      }

      .btn-search {
        background: #4318ff;
        border: none;
        &:hover {
          background: #3311cc;
        }
      }

      .btn-reset {
        background: #f4f7fe;
        color: #1b2559;
        border: none;
        &:hover {
          background: #e9edf7;
        }
      }

      .btn-add {
        background: #05cd99;
        border: none;
        &:hover {
          background: #04b386;
        }
      }
    }
  }

  /* 表格区域 */
  .table-card {
    border-radius: 20px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.08);

    :deep(.ant-table) {
      background: transparent;
    }

    :deep(.ant-table-thead > tr > th) {
      background: #f4f7fe;
      color: #a3aed0;
      font-weight: 700;
      text-transform: uppercase;
      font-size: 12px;
      border: none;
      padding: 16px;
    }

    :deep(.ant-table-tbody > tr > td) {
      border-bottom: 1px solid #f4f7fe;
      padding: 16px;
    }

    .product-cell {
      display: flex;
      align-items: center;

      .product-image {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        overflow: hidden;
        margin-right: 12px;
        background: #f4f7fe;
        display: flex;
        align-items: center;
        justify-content: center;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .no-image {
          color: #a3aed0;
          font-size: 20px;
        }
      }

      .product-info {
        .product-name {
          font-weight: 700;
          color: #1b2559;
          font-size: 14px;
        }
        .product-category {
          font-size: 12px;
          color: #a3aed0;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .price-tag {
      font-weight: 800;
      color: #4318ff;
      font-size: 16px;
    }

    .status-tag {
      border: none;
      border-radius: 8px;
      padding: 4px 12px;
      font-weight: 700;
    }

    .stock-count {
      font-size: 12px;
      color: #a3aed0;
      margin-top: 4px;

      .stock-warning {
        color: #ee5d50;
        font-weight: 700;
      }
    }

    .action-btns {
      display: flex;
      gap: 8px;
      justify-content: center;

      button {
        border-radius: 10px;
        font-weight: 600;
      }

      .btn-profile {
        background: #f4f7fe;
        color: #1b2559;
        border-color: #e0e5f2;

        &:hover {
          color: #4318ff;
          border-color: #4318ff;
          background: #fff;
        }
      }
    }
  }

  /* 弹窗样式 */
  .product-modal {
    :deep(.ant-modal-content) {
      border-radius: 24px;
      padding: 12px;
      box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.15);
    }

    :deep(.ant-modal-header) {
      border: none;
      background: transparent;
      padding: 24px 24px 0;

      .ant-modal-title {
        font-size: 24px;
        font-weight: 800;
        color: #1b2559;
      }
    }

    :deep(.ant-modal-body) {
      padding: 24px;
    }

    :deep(.ant-modal-footer) {
      border: none;
      padding: 0 24px 24px;

      .ant-btn {
        height: 44px;
        border-radius: 12px;
        padding: 0 24px;
        font-weight: 700;
      }

      .ant-btn-primary {
        background: #4318ff;
      }
    }

    .upload-container {
      .upload-area {
        :deep(.ant-upload-drag) {
          border: 2px dashed #e0e5f2;
          background: #f4f7fe;
          border-radius: 20px;
          transition: all 0.3s;

          &:hover {
            border-color: #4318ff;
          }
        }

        .upload-content {
          padding: 20px;
          display: flex;
          flex-direction: column;
          align-items: center;

          .preview-image {
            width: 100%;
            max-height: 180px;
            object-fit: contain;
            border-radius: 12px;
          }

          .upload-icon {
            font-size: 48px;
            color: #4318ff;
            margin-bottom: 12px;
          }

          .upload-text {
            color: #1b2559;
            font-weight: 600;
          }
        }
      }
    }
  }

  /* 响应式调整 */
  @media screen and (max-width: 768px) {
    .stat-card {
      padding: 16px;
      .stat-body .stat-value {
        font-size: 20px;
      }
    }
  }
.table-card {
  overflow: hidden;

  :deep(.ant-table) {
    table-layout: fixed;
  }

  :deep(.ant-table-cell) {
    vertical-align: middle;
  }
}

.product-cell {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  min-width: 0;
  max-width: 100%;
}

.product-image {
  width: 56px;
  height: 56px;
  flex: 0 0 56px;
  border-radius: 12px;
  overflow: hidden;
  background: #f4f6fb;

  img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }
}

.product-info {
  min-width: 0;
  overflow: hidden;
}

.product-name {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.description-cell {
  width: 100%;
  max-width: 300px;
  min-width: 0;
  display: -webkit-box;
  overflow: hidden;
  color: #10204d;
  line-height: 1.6;
  word-break: break-word;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.action-btns {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8px;
  min-width: 188px;
}

.ai-image-box {
  margin-top: 6px;
  padding: 18px;
  border: 1px solid #e3e8f2;
  border-radius: 12px;
  background: #f8fafd;
}

.ai-image-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;

  > div {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  strong {
    color: #07194a;
    font-size: 16px;
    font-weight: 800;
  }

  span {
    color: #6f7f99;
    font-size: 13px;
  }
}

.ai-image-controls {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.ai-image-alert {
  margin-bottom: 14px;
}

.ai-image-result {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 12px;
}

.ai-image-card {
  padding: 10px;
  border: 1px solid #e4e9f3;
  border-radius: 12px;
  background: #fff;
}

.ai-image-card img {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: block;
  object-fit: cover;
  border-radius: 10px;
  background: #eef2f8;
}

.ai-image-card-actions {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.ai-image-tip {
  margin: 0;
  color: #7a879c;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .ai-image-head {
    align-items: stretch;
    flex-direction: column;
  }

  .ai-image-controls {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .ai-image-controls {
    grid-template-columns: 1fr;
  }
}

</style>
