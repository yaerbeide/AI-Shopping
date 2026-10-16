<template>
  <div class="order-container">
    <!-- 顶部统计卡片 -->
    <div class="statistics-cards">
      <a-row :gutter="[24, 24]">
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">订单总数</span>
              <shopping-outlined class="stat-icon-mini" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ totalOrders }}</h2>
              <span class="stat-desc">所有订单数量</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">待支付</span>
              <clock-circle-outlined class="stat-icon-mini warning" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getOrderStatusCount('1') }}</h2>
              <span class="stat-desc">等待买家付款</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">已支付</span>
              <check-circle-outlined class="stat-icon-mini success" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getOrderStatusCount('2') }}</h2>
              <span class="stat-desc">交易已完成</span>
            </div>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">已取消</span>
              <close-circle-outlined class="stat-icon-mini danger" />
            </div>
            <div class="stat-body">
              <h2 class="stat-value">{{ getOrderStatusCount('7') }}</h2>
              <span class="stat-desc">交易已取消</span>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选区域 -->
    <a-card :bordered="false" class="filter-card">
      <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
        <a-col :xs="24" :sm="12" :md="6" :lg="6" class="filter-col">
          <a-input v-model:value="data.keyword" placeholder="搜索订单信息" allowClear class="search-input">
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="4" class="filter-col">
          <a-select v-model:value="data.statusFilter" placeholder="订单状态" style="width: 100%" class="filter-select" allowClear>
            <a-select-option value="">全部状态</a-select-option>
            <a-select-option value="1">待支付</a-select-option>
            <a-select-option value="2">已支付</a-select-option>
            <a-select-option value="3">已取消</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="24" :md="12" :lg="6" class="filter-col">
          <a-range-picker v-model:value="data.dateRange" style="width: 100%" class="filter-picker" />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6" :lg="8" class="filter-col">
          <div class="filter-btns">
            <a-button type="primary" @click="handleSearch" class="btn-search"> <search-outlined />搜索 </a-button>
            <a-button @click="handleReset" class="btn-reset"> <reload-outlined />重置 </a-button>
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
        :data-source="filteredOrders"
        :scroll="{ x: 1200 }"
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
              <div class="product-price">¥{{ record.price || '--' }}</div>
            </div>
          </template>

          <!-- 状态列 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(text)" class="status-tag">
              <template v-if="text === '1'"> <clock-circle-outlined /> 待支付 </template>
              <template v-else-if="text === '2'"> <check-circle-outlined /> 已支付 </template>
              <template v-else> <close-circle-outlined /> 已取消 </template>
            </a-tag>
          </template>

          <!-- 订单时间列 -->
          <template v-if="column.key === 'orderTime'">
            <div class="time-cell">
              <div class="time-value">{{ getFormatTime(text, true) }}</div>
              <div class="time-ago">{{ getTimeAgo(text) }}</div>
            </div>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'operation'">
            <div class="action-btns">
              <a-button type="primary" size="small" @click="handleViewDetails(record)" class="btn-view"> <eye-outlined />详情 </a-button>
              <a-button v-if="record.status === '1'" size="small" @click="confirmCancel(record)" class="btn-cancel">
                <stop-outlined />取消
              </a-button>
              <a-button danger size="small" @click="showDeleteConfirm(record)" class="btn-delete"> <delete-outlined />删除 </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情抽屉 -->
    <a-drawer
      :title="'订单详情 #' + (currentOrder?.id || '')"
      :visible="drawerVisible"
      :width="600"
      @close="drawerVisible = false"
      class="order-drawer"
    >
      <template v-if="currentOrder">
        <a-descriptions bordered :column="1" size="middle">
          <a-descriptions-item label="订单ID">{{ currentOrder.id }}</a-descriptions-item>
          <a-descriptions-item label="用户">{{ currentOrder.username }}</a-descriptions-item>
          <a-descriptions-item label="商品名称">{{ currentOrder.title }}</a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(currentOrder.status)">
              {{ currentOrder.status === '1' ? '待支付' : currentOrder.status === '2' ? '已支付' : '已取消' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="订单金额">¥{{ currentOrder.price || '--' }}</a-descriptions-item>
          <a-descriptions-item label="订单时间">{{ getFormatTime(currentOrder.orderTime, true) }}</a-descriptions-item>
        </a-descriptions>

        <div class="drawer-footer">
          <a-button @click="drawerVisible = false">关闭</a-button>
          <a-button v-if="currentOrder.status === '1'" @click="confirmCancel(currentOrder)" class="btn-cancel"> 取消订单 </a-button>
          <a-button danger @click="showDeleteConfirm(currentOrder)"> 删除订单 </a-button>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message, Modal } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi, cancelApi } from '/@/api/order';
  import { getFormatTime } from '/@/utils';
  import {
    ShoppingOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    SearchOutlined,
    ReloadOutlined,
    DeleteOutlined,
    EyeOutlined,
    StopOutlined,
    UserOutlined,
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
      width: 120,
      align: 'left',
    },
    {
      title: '商品',
      dataIndex: 'title',
      key: 'title',
      width: 300,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '金额',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      align: 'right',
      customRender: ({ text }) => `¥${text || '--'}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      align: 'center',
    },
    {
      title: '订单时间',
      dataIndex: 'orderTime',
      key: 'orderTime',
      width: 180,
      align: 'center',
      sorter: (a, b) => new Date(a.orderTime) - new Date(b.orderTime),
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
    tagList: [],
    loading: false,
    keyword: '',
    statusFilter: '',
    dateRange: null,
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 抽屉相关
  const drawerVisible = ref(false);
  const currentOrder = ref(null);

  // 计算总订单数
  const totalOrders = computed(() => {
    return data.tagList.length;
  });

  // 根据状态筛选订单
  const filteredOrders = computed(() => {
    let result = [...data.tagList];

    // 根据关键字筛选
    if (data.keyword) {
      const keyword = data.keyword.toLowerCase();
      result = result.filter(
        (item) =>
          (item.title && item.title.toLowerCase().includes(keyword)) || (item.username && item.username.toLowerCase().includes(keyword)),
      );
    }

    // 根据状态筛选
    if (data.statusFilter) {
      result = result.filter((item) => item.status === data.statusFilter);
    }

    // 根据日期范围筛选
    if (data.dateRange && data.dateRange[0] && data.dateRange[1]) {
      const startDate = data.dateRange[0].valueOf();
      const endDate = data.dateRange[1].valueOf();
      result = result.filter((item) => {
        const orderTime = new Date(item.orderTime).valueOf();
        return orderTime >= startDate && orderTime <= endDate;
      });
    }

    return result;
  });

  // 获取订单状态数量
  const getOrderStatusCount = (status) => {
    return data.tagList.filter((item) => item.status === status).length;
  };

  // 获取状态颜色
  const getStatusColor = (status) => {
    switch (status) {
      case '1':
        return 'warning';
      case '2':
        return 'success';
      case '3':
        return 'error';
      default:
        return 'default';
    }
  };

  // 获取行样式
  const getRowClassName = (record) => {
    switch (record.status) {
      case '1':
        return 'order-row-pending';
      case '2':
        return 'order-row-paid';
      case '3':
        return 'order-row-cancelled';
      default:
        return '';
    }
  };

  // 获取时间差描述
  const getTimeAgo = (timestamp) => {
    if (!timestamp) return '';

    const now = new Date();
    const orderTime = new Date(timestamp);
    const diffMs = now - orderTime;
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
            // 模拟添加价格数据，实际项目中应该从后端获取
            if (!item.price) {
              item.price = Math.floor(Math.random() * 10000) / 100;
            }
          });
          data.tagList = res.data;
        }
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

  const confirmCancel = (record: any) => {
    cancelApi({ id: record.id })
      .then((res) => {
        getDataList();
        message.success('订单取消成功');
        if (drawerVisible.value) {
          drawerVisible.value = false;
        }
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const showDeleteConfirm = (record: any) => {
    Modal.confirm({
      title: '确定要删除此订单吗?',
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
        getDataList();
        message.success('删除成功');
        if (drawerVisible.value) {
          drawerVisible.value = false;
        }
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleSearch = () => {
    data.page = 1;
    getDataList();
  };

  const handleReset = () => {
    data.keyword = '';
    data.statusFilter = '';
    data.dateRange = null;
    data.page = 1;
    getDataList();
  };

  const handleBatchDelete = () => {
    if (data.selectedRowKeys.length <= 0) {
      message.warn('请勾选要删除的订单');
      return;
    }

    Modal.confirm({
      title: `确定要删除选中的 ${data.selectedRowKeys.length} 个订单吗?`,
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

  const handleViewDetails = (record) => {
    currentOrder.value = record;
    drawerVisible.value = true;
  };
</script>

<style scoped lang="less">
  .order-container {
    min-height: 100%;
  }

  /* 统计卡片样式 */
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

      .btn-delete {
        border-radius: 12px;
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

    .user-cell {
      .username {
        font-weight: 700;
        color: #1b2559;
        font-size: 14px;
      }
    }

    .product-cell {
      .product-name {
        font-weight: 700;
        color: #1b2559;
        font-size: 14px;
      }
      .product-price {
        font-size: 12px;
        color: #a3aed0;
        font-weight: 600;
      }
    }

    .status-tag {
      border: none;
      border-radius: 8px;
      padding: 4px 12px;
      font-weight: 700;
    }

    .time-cell {
      .time-value {
        font-weight: 700;
        color: #1b2559;
        font-size: 13px;
      }
      .time-ago {
        font-size: 12px;
        color: #a3aed0;
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

      .btn-cancel {
        background: #ffb547;
        color: #fff;
        border: none;
        &:hover {
          background: #e6a23c;
          color: #fff;
        }
      }
    }
  }

  .order-drawer {
    :deep(.ant-drawer-content-wrapper) {
      border-radius: 24px 0 0 24px;
      overflow: hidden;
    }

    :deep(.ant-drawer-header) {
      border: none;
      padding: 24px;
      .ant-drawer-title {
        font-size: 20px;
        font-weight: 800;
        color: #1b2559;
      }
    }

    .drawer-footer {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      padding: 20px 24px;
      background: #fff;
      border-top: 1px solid #f4f7fe;
      display: flex;
      justify-content: flex-end;
      gap: 12px;

      button {
        border-radius: 12px;
        height: 44px;
        padding: 0 20px;
        font-weight: 700;
      }

      .btn-cancel {
        background: #ffb547;
        color: #fff;
        border: none;
        &:hover {
          background: #e6a23c;
          color: #fff;
        }
      }
    }
  }
</style>
