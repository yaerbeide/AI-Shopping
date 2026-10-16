<template>
  <div class="notice-container">
    <!-- 搜索和筛选区域 -->
    <a-card :bordered="false" class="filter-card">
      <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
        <a-col :xs="24" :sm="12" :md="8" :lg="8" class="filter-col">
          <a-input v-model:value="data.keyword" placeholder="搜索公告标题或内容" allowClear class="search-input" @pressEnter="onSearch">
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-col>
        <a-col :xs="24" :sm="12" :md="16" :lg="16" class="filter-col">
          <div class="filter-btns">
            <a-button type="primary" @click="onSearch" class="btn-search"> <search-outlined />搜索 </a-button>
            <a-button @click="handleReset" class="btn-reset"> <reload-outlined />重置 </a-button>
            <a-button type="primary" @click="handleAdd" class="btn-add"> <plus-outlined />新增公告 </a-button>
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
        :data-source="filteredNotices"
        :scroll="{ x: 1000 }"
        :row-selection="rowSelection"
        :pagination="{
          size: 'default',
          current: data.page,
          pageSize: data.pageSize,
          onChange: (current) => (data.page = current),
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          showTotal: (total) => `共${total}条数据`,
        }"
      >
        <template #bodyCell="{ text, record, index, column }">
          <!-- 标题列 -->
          <template v-if="column.key === 'title'">
            <div class="notice-title">
              <span class="title-text">{{ text }}</span>
              <a-tag v-if="isNew(record)" color="success" class="new-tag">新</a-tag>
            </div>
          </template>

          <!-- 内容列 -->
          <template v-if="column.key === 'content'">
            <div class="notice-content">
              <a-tooltip :title="record.content">
                <div class="content-preview">{{ truncateContent(record.content) }}</div>
              </a-tooltip>
            </div>
          </template>

          <!-- 创建时间列 -->
          <template v-if="column.key === 'createTime'">
            <div class="time-cell">
              <div class="time-value">{{ formatDate(record.createTime) }}</div>
            </div>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'operation'">
            <div class="action-btns">
              <a-button type="primary" size="small" @click="handleEdit(record)" class="btn-edit"> <edit-outlined />编辑 </a-button>
              <a-button type="primary" ghost size="small" @click="handlePreview(record)" class="btn-preview">
                <eye-outlined />预览
              </a-button>
              <a-button danger size="small" @click="showDeleteConfirm(record)" class="btn-delete"> <delete-outlined />删除 </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 公告编辑弹窗 -->
    <a-modal
      :visible="modal.visible"
      :forceRender="true"
      :title="modal.title"
      width="700px"
      ok-text="确认"
      cancel-text="取消"
      @cancel="handleCancel"
      @ok="handleOk"
      class="notice-modal"
    >
      <div class="modal-content">
        <a-form ref="myform" :model="modal.form" :rules="modal.rules" layout="vertical">
          <a-form-item label="公告标题" name="title">
            <a-input placeholder="请输入公告标题" v-model:value="modal.form.title" :maxLength="100" show-count />
          </a-form-item>

          <a-form-item label="公告内容" name="content">
            <a-textarea placeholder="请输入公告内容" :rows="6" v-model:value="modal.form.content" :maxLength="500" show-count />
          </a-form-item>

          <a-form-item label="公告重要性">
            <a-radio-group v-model:value="modal.form.important" button-style="solid">
              <a-radio-button :value="false"> <info-circle-outlined /> 普通 </a-radio-button>
              <a-radio-button :value="true"> <warning-outlined /> 重要 </a-radio-button>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 公告预览弹窗 -->
    <a-modal
      :visible="preview.visible"
      :title="'公告预览'"
      width="600px"
      :footer="null"
      @cancel="preview.visible = false"
      class="preview-modal"
    >
      <div class="preview-content">
        <div class="preview-header">
          <h2 class="preview-title">{{ preview.title }}</h2>
          <div class="preview-meta">
            <span class="preview-time">{{ formatDate(preview.createTime) }}</span>
          </div>
        </div>
        <a-divider />
        <div class="preview-body">
          {{ preview.content }}
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message, Modal } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi } from '/@/api/notice';
  import {
    SoundOutlined,
    EyeOutlined,
    CalendarOutlined,
    NotificationOutlined,
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    DeleteOutlined,
    EditOutlined,
    InfoCircleOutlined,
    WarningOutlined,
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
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      width: 250,
      align: 'left',
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      width: 350,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '发布时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      align: 'center',
      sorter: (a, b) => new Date(a.createTime || 0) - new Date(b.createTime || 0),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 220,
    },
  ]);

  // 页面数据
  const data = reactive({
    noticeList: [],
    loading: false,
    keyword: '',
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 弹窗数据源
  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    form: {
      id: undefined,
      title: undefined,
      content: undefined,
      important: false,
      createTime: undefined,
    },
    rules: {
      title: [{ required: true, message: '请输入公告标题', trigger: 'change' }],
      content: [{ required: true, message: '请输入公告内容', trigger: 'change' }],
    },
  });

  // 预览数据
  const preview = reactive({
    visible: false,
    title: '',
    content: '',
    createTime: '',
  });

  const myform = ref<FormInstance>();

  // 过滤后的公告列表
  const filteredNotices = computed(() => {
    if (!data.keyword) {
      return data.noticeList;
    }

    const keyword = data.keyword.toLowerCase();
    return data.noticeList.filter(
      (item) =>
        (item.title && item.title.toLowerCase().includes(keyword)) || (item.content && item.content.toLowerCase().includes(keyword)),
    );
  });

  // 获取总阅读量（模拟数据）
  const getTotalViews = () => {
    // 实际项目中应该从后端获取真实数据
    return Math.floor(data.noticeList.length * Math.random() * 100) + 50;
  };

  // 获取本周发布的公告数量
  const getThisWeekNotices = () => {
    try {
      const now = new Date();
      const weekStart = new Date(now.setDate(now.getDate() - now.getDay()));
      weekStart.setHours(0, 0, 0, 0);

      return data.noticeList.filter((item) => {
        if (!item.createTime) return false;
        try {
          const createTime = new Date(item.createTime);
          return !isNaN(createTime.getTime()) && createTime >= weekStart;
        } catch {
          return false;
        }
      }).length;
    } catch (e) {
      console.error('计算本周公告错误:', e);
      return 0;
    }
  };

  // 获取重要公告数量
  const getImportantNotices = () => {
    return data.noticeList.filter((item) => item.important).length;
  };

  // 检查公告是否重要
  const isImportant = (record) => {
    return record.important === true;
  };

  // 检查公告是否为新发布（7天内）
  const isNew = (record) => {
    if (!record.createTime) return false;

    try {
      const now = new Date();
      const createTime = new Date(record.createTime);

      if (isNaN(createTime.getTime())) return false;

      const diffDays = Math.floor((now.getTime() - createTime.getTime()) / (1000 * 60 * 60 * 24));

      return diffDays < 7;
    } catch (e) {
      console.error('检查新发布错误:', e);
      return false;
    }
  };

  // 格式化日期
  const formatDate = (dateStr) => {
    if (!dateStr) return '--';

    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return '--';

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (e) {
      console.error('日期格式化错误:', e);
      return '--';
    }
  };

  // 获取时间差描述
  const getTimeAgo = (dateStr) => {
    if (!dateStr) return '';

    try {
      const now = new Date();
      const date = new Date(dateStr);

      if (isNaN(date.getTime())) return '';

      const diffMs = now.getTime() - date.getTime();
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

      if (diffDays === 0) {
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        if (diffHours === 0) {
          const diffMinutes = Math.floor(diffMs / (1000 * 60));
          return `${diffMinutes}分钟前`;
        }
        return `${diffHours}小时前`;
      } else if (diffDays < 30) {
        return `${diffDays}天前`;
      } else {
        const diffMonths = Math.floor(diffDays / 30);
        return `${diffMonths}个月前`;
      }
    } catch (e) {
      console.error('计算时间差异错误:', e);
      return '';
    }
  };

  // 截断内容
  const truncateContent = (content) => {
    if (!content) return '--';
    return content.length > 50 ? content.substring(0, 50) + '...' : content;
  };

  onMounted(() => {
    getDataList();
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
            // 模拟添加创建时间和重要性（实际项目中应该从后端获取）
            if (!item.createTime) {
              const randomDays = Math.floor(Math.random() * 30);
              const date = new Date();
              date.setDate(date.getDate() - randomDays);
              item.createTime = date.toISOString();
            } else if (typeof item.createTime === 'string' && !isValidDate(item.createTime)) {
              // 如果日期字符串无效，使用当前时间
              item.createTime = new Date().toISOString();
            }
            if (item.important === undefined) {
              item.important = Math.random() > 0.7;
            }
          });
          data.noticeList = res.data;
        }
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  // 检查日期字符串是否有效
  const isValidDate = (dateStr) => {
    const date = new Date(dateStr);
    return !isNaN(date.getTime());
  };

  const onSearch = () => {
    data.page = 1;
    getDataList();
  };

  const handleReset = () => {
    data.keyword = '';
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
    modal.title = '新增公告';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    modal.form.important = false;
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '编辑公告';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    for (const key in record) {
      modal.form[key] = record[key];
    }
  };

  const handlePreview = (record: any) => {
    preview.visible = true;
    preview.title = record.title || '';
    preview.content = record.content || '';
    preview.createTime = record.createTime || '';
  };

  const showDeleteConfirm = (record: any) => {
    Modal.confirm({
      title: '确定要删除此公告吗?',
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
      message.warn('请勾选要删除的公告');
      return;
    }

    Modal.confirm({
      title: `确定要删除选中的 ${data.selectedRowKeys.length} 条公告吗?`,
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

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        // 如果是新增添加创建时间
        if (!modal.editFlag) {
          modal.form.createTime = new Date().toISOString();
        }

        if (modal.editFlag) {
          updateApi(modal.form)
            .then((res) => {
              hideModal();
              message.success('更新公告成功');
              getDataList();
            })
            .catch((err) => {
              console.log(err);
              message.error(err.msg || '操作失败');
            });
        } else {
          createApi(modal.form)
            .then((res) => {
              hideModal();
              message.success('新增公告成功');
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
  };

  // 关闭弹窗
  const hideModal = () => {
    modal.visible = false;
  };
</script>

<style scoped lang="less">
  .notice-container {
    min-height: 100%;
    background: #f0f2f5;
    padding: 24px;
  }

  /* 统计卡片样式 */
  .statistics-cards {
    margin-bottom: 24px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;
    background: #fff;
    transition: all 0.3s;
    height: 100%;
    overflow: hidden;
    position: relative;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
    }

    &::after {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 100px;
      height: 100%;
      background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.2) 100%);
      transform: skewX(-20deg) translateX(50px);
    }
  }

  .card-primary {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  }

  .card-success {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  }

  .card-warning {
    background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
  }

  .card-danger {
    background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  }

  .stat-icon {
    font-size: 32px;
    margin-right: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }

  .stat-content {
    flex: 1;
    color: #fff;

    .stat-title {
      font-size: 16px;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 4px;
      line-height: 1.2;
    }

    .stat-desc {
      font-size: 12px;
      opacity: 0.8;
    }
  }

  /* 筛选卡片样式 */
  .filter-card {
    margin-bottom: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .filter-row {
      margin-bottom: 0;
    }

    .filter-col {
      margin-bottom: 16px;
    }

    .search-input {
      width: 100%;
      border-radius: 4px;
    }

    .filter-btns {
      display: flex;
      justify-content: flex-end;
      flex-wrap: wrap;

      button {
        margin-left: 8px;
        margin-bottom: 8px;
        border-radius: 4px;
        display: flex;
        align-items: center;

        .anticon {
          margin-right: 4px;
        }
      }

      .btn-add {
        background: #1890ff;
        border-color: #1890ff;
      }

      .btn-delete {
        background: #ff4d4f;
        border-color: #ff4d4f;
      }
    }
  }

  /* 表格卡片样式 */
  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    :deep(.ant-table) {
      border-radius: 8px;
      overflow: hidden;
    }

    :deep(.ant-table-thead > tr > th) {
      background: #fafafa;
      font-weight: 600;
    }

    :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
      background: rgba(24, 144, 255, 0.05);
    }

    .notice-title {
      display: flex;
      align-items: center;

      .important-icon {
        color: #ff4d4f;
        margin-right: 8px;
        font-size: 16px;
      }

      .title-text {
        flex: 1;
        font-weight: 500;
      }

      .new-tag {
        margin-left: 8px;
        font-size: 12px;
        padding: 0 6px;
        height: 20px;
        line-height: 20px;
      }
    }

    .notice-content {
      .content-preview {
        color: rgba(0, 0, 0, 0.65);
        cursor: pointer;

        &:hover {
          color: #1890ff;
        }
      }
    }

    .time-cell {
      display: flex;
      flex-direction: column;

      .time-value {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .time-ago {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
      }
    }

    .action-btns {
      display: flex;
      justify-content: center;
      gap: 8px;
      flex-wrap: wrap;

      button {
        display: flex;
        align-items: center;
        padding: 0 8px;
        height: 28px;

        .anticon {
          margin-right: 4px;
          font-size: 12px;
        }
      }

      .btn-edit {
        background-color: #1890ff;
        border-color: #1890ff;
      }

      .btn-preview {
        color: #1890ff;
      }

      .btn-delete {
        background-color: #ff4d4f;
        border-color: #ff4d4f;
      }
    }
  }

  /* 弹窗样式 */
  :deep(.notice-modal) {
    .ant-modal-content {
      border-radius: 8px;
      overflow: hidden;
    }

    .ant-modal-header {
      background: #fafafa;
      padding: 16px 24px;
    }

    .ant-modal-title {
      font-weight: 600;
      font-size: 16px;
    }

    .ant-modal-body {
      padding: 24px;
    }

    .ant-form-item-label {
      font-weight: 500;
    }
  }

  /* 预览弹窗样式 */
  :deep(.preview-modal) {
    .ant-modal-content {
      border-radius: 8px;
      overflow: hidden;
    }

    .ant-modal-header {
      background: #fafafa;
      padding: 16px 24px;
    }

    .ant-modal-title {
      font-weight: 600;
      font-size: 16px;
    }

    .ant-modal-body {
      padding: 24px;
    }

    .preview-content {
      .preview-header {
        text-align: center;
        margin-bottom: 16px;

        .preview-title {
          font-size: 20px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .preview-meta {
          color: rgba(0, 0, 0, 0.45);
          font-size: 14px;
        }
      }

      .preview-body {
        font-size: 14px;
        line-height: 1.8;
        color: rgba(0, 0, 0, 0.85);
        white-space: pre-wrap;
      }
    }
  }

  /* 响应式调整 */
  @media screen and (max-width: 768px) {
    .filter-btns {
      justify-content: flex-start;

      button {
        margin-left: 0;
        margin-right: 8px;
      }
    }

    .stat-card {
      .stat-icon {
        width: 48px;
        height: 48px;
        font-size: 24px;
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
        }
      }
    }

    .action-btns {
      flex-direction: column;
      align-items: center;
    }
  }
</style>
