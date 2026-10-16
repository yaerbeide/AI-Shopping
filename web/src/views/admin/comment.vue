<template>
  <div class="comment-container">
    <!-- 搜索和筛选区域 -->
    <a-card :bordered="false" class="filter-card">
      <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
        <a-col :xs="24" :sm="12" :md="6" :lg="4" class="filter-col">
          <a-input v-model:value="data.keyword" placeholder="搜索评价内容或商品" allowClear class="search-input" @pressEnter="onSearch">
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="4" class="filter-col">
          <a-select
            v-model:value="data.ratingFilter"
            placeholder="评价等级"
            style="width: 100%"
            class="filter-select"
            allowClear
            @change="onSearch"
          >
            <a-select-option value="">全部评价</a-select-option>
            <a-select-option value="positive">好评</a-select-option>
            <a-select-option value="neutral">中评</a-select-option>
            <a-select-option value="negative">差评</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="16" :lg="16" class="filter-col">
          <div class="filter-btns">
            <a-button type="primary" @click="onSearch" class="btn-search"> <search-outlined />搜索 </a-button>
            <a-button @click="handleReset" class="btn-reset"> <reload-outlined />重置 </a-button>
            <a-button type="primary" @click="handleAdd" class="btn-add"> <plus-outlined />模拟评价 </a-button>
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
        :data-source="filteredComments"
        :scroll="{ x: 1200 }"
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
        :rowClassName="getRowClassName"
      >
        <template #bodyCell="{ text, record, index, column }">
          <!-- 用户列 -->
          <template v-if="column.key === 'username'">
            <div class="user-cell">
              <span class="username">{{ text }}</span>
            </div>
          </template>

          <!-- 商品列 -->
          <template v-if="column.key === 'title'">
            <div class="product-cell">
              <div class="product-name" :title="record.title">{{ record.title }}</div>
              <a-tag color="processing" class="product-tag">商品</a-tag>
            </div>
          </template>

          <!-- 评价内容列 -->
          <template v-if="column.key === 'content'">
            <div class="comment-content">
              <a-rate :value="getCommentRating(record)" disabled allow-half :count="5" />
              <div class="content-text">
                <a-tooltip :title="record.content">
                  {{ truncateContent(record.content) }}
                </a-tooltip>
              </div>
            </div>
          </template>

          <!-- 评价时间列 -->
          <template v-if="column.key === 'commentTime'">
            <div class="time-cell">
              <div class="time-value">{{ getFormatTime(text, true) }}</div>
              <div class="time-ago">{{ getTimeAgo(text) }}</div>
            </div>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'operation'">
            <div class="action-btns">
              <a-button type="primary" size="small" @click="handleViewComment(record)" class="btn-view"> <eye-outlined />查看 </a-button>
              <a-button danger size="small" @click="showDeleteConfirm(record)" class="btn-delete"> <delete-outlined />删除 </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 评价详情抽屉 -->
    <a-drawer :title="'评价详情'" :visible="drawer.visible" :width="600" @close="drawer.visible = false" class="comment-drawer">
      <template v-if="drawer.currentComment">
        <a-descriptions bordered :column="1" size="middle">
          <a-descriptions-item label="用户名">{{ drawer.currentComment.username }}</a-descriptions-item>
          <a-descriptions-item label="商品名称">{{ drawer.currentComment.title }}</a-descriptions-item>
          <a-descriptions-item label="评分">
            <a-rate :value="getCommentRating(drawer.currentComment)" disabled allow-half :count="5" />
          </a-descriptions-item>
          <a-descriptions-item label="评价内容">{{ drawer.currentComment.content }}</a-descriptions-item>
          <a-descriptions-item label="评价时间">{{ getFormatTime(drawer.currentComment.commentTime, true) }}</a-descriptions-item>
        </a-descriptions>

        <div class="drawer-footer">
          <a-button @click="drawer.visible = false">关闭</a-button>
          <a-button danger @click="showDeleteConfirm(drawer.currentComment)"> 删除评价 </a-button>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message, Modal } from 'ant-design-vue';
  import { createApi, listApi, deleteApi } from '/@/api/comment';
  import { BASE_URL } from '/@/store/constants';
  import { getFormatTime } from '/@/utils';
  import {
    CommentOutlined,
    LikeOutlined,
    DislikeOutlined,
    CalendarOutlined,
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    DeleteOutlined,
    EyeOutlined,
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
      title: '用户',
      dataIndex: 'username',
      key: 'username',
      width: 100,
      align: 'left',
    },
    {
      title: '商品名称',
      dataIndex: 'title',
      key: 'title',
      width: 250,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '评论内容',
      dataIndex: 'content',
      key: 'content',
      width: 150,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '评论时间',
      dataIndex: 'commentTime',
      key: 'commentTime',
      width: 250,
      align: 'center',
      sorter: (a, b) => new Date(a.commentTime || 0) - new Date(b.commentTime || 0),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 180,
    },
  ]);

  // 页面数据
  const data = reactive({
    list: [],
    loading: false,
    currentAdminUserName: '',
    keyword: '',
    ratingFilter: '',
    dateRange: [],
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 抽屉数据
  const drawer = reactive({
    visible: false,
    currentComment: null,
  });

  // 过滤后的评价列表
  const filteredComments = computed(() => {
    let result = [...data.list];

    // 根据关键字筛选
    if (data.keyword) {
      const keyword = data.keyword.toLowerCase();
      result = result.filter(
        (item) =>
          (item.content && item.content.toLowerCase().includes(keyword)) ||
          (item.title && item.title.toLowerCase().includes(keyword)) ||
          (item.username && item.username.toLowerCase().includes(keyword)),
      );
    }

    // 根据评分筛选
    if (data.ratingFilter) {
      result = result.filter((item) => {
        const rating = getCommentRating(item);
        switch (data.ratingFilter) {
          case 'positive':
            return rating >= 4;
          case 'neutral':
            return rating >= 2 && rating < 4;
          case 'negative':
            return rating < 2;
          default:
            return true;
        }
      });
    }

    // 根据日期范围筛选
    if (data.dateRange && data.dateRange[0] && data.dateRange[1]) {
      const startDate = data.dateRange[0].valueOf();
      const endDate = data.dateRange[1].valueOf();
      result = result.filter((item) => {
        if (!item.commentTime) return false;
        try {
          const commentTime = new Date(item.commentTime).valueOf();
          return !isNaN(commentTime) && commentTime >= startDate && commentTime <= endDate;
        } catch {
          return false;
        }
      });
    }

    return result;
  });

  // 获取好评数量
  const getPositiveComments = () => {
    return data.list.filter((item) => getCommentRating(item) >= 4).length;
  };

  // 获取差评数量
  const getNegativeComments = () => {
    return data.list.filter((item) => getCommentRating(item) < 2).length;
  };

  // 获取本周评价数量
  const getThisWeekComments = () => {
    try {
      const now = new Date();
      const weekStart = new Date(now.setDate(now.getDate() - now.getDay()));
      weekStart.setHours(0, 0, 0, 0);

      return data.list.filter((item) => {
        if (!item.commentTime) return false;
        try {
          const commentTime = new Date(item.commentTime);
          return !isNaN(commentTime.getTime()) && commentTime >= weekStart;
        } catch {
          return false;
        }
      }).length;
    } catch (e) {
      console.error('计算本周评价错误:', e);
      return 0;
    }
  };

  // 获取评价评分（模拟数据）
  const getCommentRating = (record) => {
    // 实际项目中应该从后端获取真实数据
    // 这里根据评价内容长度和内容模拟生成评分
    if (!record || !record.content) return 3;

    // 使用评价ID的最后一位数字作为评分基础
    const idLastDigit = record.id ? parseInt(record.id.toString().slice(-1)) : 5;

    // 根据内容特征调整评分
    let rating = (idLastDigit % 5) + 1; // 1-5分

    // 如果内容包含特定词汇，调整评分
    const content = record.content.toLowerCase();
    if (content.includes('好') || content.includes('满意') || content.includes('赞')) {
      rating = Math.min(5, rating + 1);
    }
    if (content.includes('差') || content.includes('不满') || content.includes('退')) {
      rating = Math.max(1, rating - 1);
    }

    return rating;
  };

  // 截断内容
  const truncateContent = (content) => {
    if (!content) return '--';
    return content.length > 30 ? content.substring(0, 30) + '...' : content;
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

  // 获取行样式
  const getRowClassName = (record) => {
    const rating = getCommentRating(record);
    if (rating <= 2) return 'negative-comment-row';
    if (rating >= 4) return 'positive-comment-row';
    return '';
  };

  onMounted(() => {
    getList();
  });

  const getList = () => {
    data.loading = true;
    listApi({
      keyword: data.keyword,
    })
      .then((res) => {
        data.loading = false;
        console.log(res);
        res.data.forEach((item: any, index: any) => {
          item.index = index + 1;
          if (item.image) {
            item.image = BASE_URL + item.image;
          }

          // 模拟添加评价时间（实际项目中应该从后端获取）
          if (!item.commentTime) {
            const randomDays = Math.floor(Math.random() * 30);
            const date = new Date();
            date.setDate(date.getDate() - randomDays);
            item.commentTime = date.toISOString();
          }
        });
        data.list = res.data;
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  const rowSelection = ref({
    onChange: (selectedRowKeys: (string | number)[], selectedRows: any[]) => {
      console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
      data.selectedRowKeys = selectedRowKeys;
    },
  });

  const handleAdd = () => {
    // 模拟添加评价
    createApi({})
      .then((res) => {
        message.success('模拟新增评价成功');
        getList();
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleViewComment = (record) => {
    drawer.currentComment = record;
    drawer.visible = true;
  };

  const showDeleteConfirm = (record) => {
    Modal.confirm({
      title: '确定要删除此评价吗?',
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
        getList();
        if (drawer.visible && drawer.currentComment?.id === record.id) {
          drawer.visible = false;
        }
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleBatchDelete = () => {
    console.log(data.selectedRowKeys);
    if (data.selectedRowKeys.length <= 0) {
      message.warn('请勾选要删除的评价');
      return;
    }

    Modal.confirm({
      title: `确定要删除选中的 ${data.selectedRowKeys.length} 条评价吗?`,
      content: '删除后无法恢复，请谨慎操作',
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        deleteApi({ ids: data.selectedRowKeys.join(',') })
          .then((res) => {
            message.success('批量删除成功');
            data.selectedRowKeys = [];
            getList();
          })
          .catch((err) => {
            message.error(err.msg || '操作失败');
          });
      },
    });
  };

  const onSearch = () => {
    data.page = 1;
    getList();
  };

  const handleReset = () => {
    data.keyword = '';
    data.ratingFilter = '';
    data.dateRange = [];
    data.page = 1;
    getList();
  };
</script>

<style scoped lang="less">
  .comment-container {
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

    .filter-select {
      width: 100%;
      border-radius: 4px;
    }

    .date-picker {
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
        color: #fff;
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

    :deep(.ant-table-row.positive-comment-row) {
      background-color: rgba(82, 196, 26, 0.05);
    }

    :deep(.ant-table-row.negative-comment-row) {
      background-color: rgba(255, 77, 79, 0.05);
    }

    .user-cell {
      display: flex;
      align-items: center;

      .user-avatar {
        background-color: #1890ff;
        color: #fff;
        margin-right: 8px;
      }

      .username {
        font-weight: 500;
      }
    }

    .product-cell {
      display: flex;
      flex-direction: column;

      .product-name {
        font-weight: 500;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .product-tag {
        align-self: flex-start;
        font-size: 12px;
        padding: 0 6px;
        height: 20px;
        line-height: 20px;
      }
    }

    .comment-content {
      display: flex;
      flex-direction: column;

      .content-text {
        margin-top: 8px;
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

      .btn-view {
        background-color: #1890ff;
        border-color: #1890ff;
      }

      .btn-delete {
        background-color: #ff4d4f;
        border-color: #ff4d4f;
        color: #fff;
      }
    }
  }

  /* 抽屉样式 */
  :deep(.comment-drawer) {
    .ant-drawer-header {
      background: #fafafa;
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;
    }

    .ant-drawer-title {
      font-weight: 600;
      font-size: 16px;
    }

    .ant-drawer-body {
      padding: 24px;
    }

    .ant-descriptions-item-label {
      width: 100px;
      font-weight: 500;
    }
  }

  .drawer-footer {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #f0f0f0;
    padding: 10px 16px;
    text-align: right;
    left: 0;
    background: #fff;
    border-radius: 0 0 4px 4px;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  /* 响应式样式 */
  @media (max-width: 576px) {
    .filter-btns {
      justify-content: flex-start;
    }

    .statistics-cards .ant-col {
      margin-bottom: 16px;
    }

    .action-btns {
      flex-direction: column;
      align-items: center;
    }
  }
</style>
