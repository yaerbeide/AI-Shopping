<template>
  <div class="content-list">
    <div class="list-title">我的订单</div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <a-input-search
        v-model:value="searchText"
        placeholder="输入订单号或商品名称搜索"
        style="width: 300px; margin-right: 16px"
        @search="onSearch"
        enter-button
      />
      <a-button @click="resetSearch" style="margin-right: 16px">重置</a-button>
      <a-button type="primary" @click="getOrderList" :loading="loading">
        <template #icon><ReloadOutlined /></template>
        刷新订单
      </a-button>
    </div>

    <a-tabs default-active-key="1" @change="onTabChange" class="custom-tabs">
      <a-tab-pane key="1" tab="全部" />
      <a-tab-pane key="2" tab="待付款" />
      <a-tab-pane key="3" tab="已支付" />
    </a-tabs>
    <div class="list-content">
      <div v-if="filteredOrderData.length === 0" class="empty-state">
        <a-empty description="未找到相关订单" />
      </div>
      <div class="order-item-view" v-for="(item, index) in filteredOrderData" :key="index">
        <div class="header flex-view">
          <div class="left">
            <span class="text">订单号</span>
            <span class="num mg-4">#</span>
            <span class="num">{{ item.orderNumber }}</span>
            <span class="time">{{ getFormatTime(item.orderTime, true) }}</span>
          </div>
          <div class="right">
            <a-popconfirm v-if="item.status === '1'" title="确定取消订单？" ok-text="是" cancel-text="否" @confirm="handleCancel(item)">
              <a-button type="primary" size="small" ghost style="margin-right: 12px">取消</a-button>
            </a-popconfirm>
            <a-button v-if="item.status === '1'" type="primary" size="small" style="margin-right: 24px" @click="handlePay(item)">
              去支付
            </a-button>
            <span class="text">订单状态</span>
            <span :class="['state', item.status === '1' ? 'state-pending' : item.status === '2' ? 'state-paid' : 'state-canceled']">
              {{ item.status === '1' ? '待支付' : item.status === '2' ? '已支付' : '已取消' }}
            </span>
          </div>
        </div>
        <div class="content flex-view">
          <div class="left-list">
            <div class="list-item flex-view" @click="handleDetail(item.thingId)">
              <img :src="item.cover" class="thing-img" />
              <div class="detail flex-between flex-view">
                <div class="flex-between flex-top flex-view">
                  <h2 class="name">{{ item.title }}</h2>
                  <span class="count">x{{ item.count }}</span>
                </div>
                <div class="flex-between flex-center flex-view">
                  <span class="type"></span>
                  <span class="price">¥{{ item.price }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="right-info">
            <p class="title">收货信息</p>
            <p class="name">{{ item.receiverName }}{{ item.receiverPhone }} </p>
            <p class="text mg">{{ item.receiverAddress }} </p>
            <p class="title">快递单号</p>
            <p class="text"> </p>
            <p class="title">备注信息</p>
            <p class="text">{{ item.remark }} </p>
          </div>
        </div>
        <div class="bottom flex-view">
          <div class="left">
            <span class="text">共{{ item.count }}件商品</span>
            <span class="open" @click="handleDetail(item.thingId)">商品详情</span>
          </div>
          <div class="right flex-view">
            <span class="text">总计</span>
            <span class="num">¥ {{ item.price * item.count }}</span>
            <span class="text">优惠</span>
            <span class="num">¥0</span>
            <span class="text">实际支付</span>
            <span class="money">¥ {{ item.price * item.count }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { message } from 'ant-design-vue';
  import { getFormatTime } from '/@/utils/';
  import { userOrderListApi } from '/@/api/order';
  import { cancelUserOrderApi } from '/@/api/order';
  import { BASE_URL } from '/@/store/constants';
  import { useUserStore } from '/@/store';
  import { useRouter, useRoute } from 'vue-router';
  import { ref, computed } from 'vue';
  import { ReloadOutlined } from '@ant-design/icons-vue';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();

  const loading = ref(false);
  const orderData = ref([]);
  const orderStatus = ref('');
  const searchText = ref('');

  onMounted(() => {
    getOrderList();
  });

  const onTabChange = (key) => {
    console.log(key);
    if (key === '1') {
      orderStatus.value = '';
    }
    if (key === '2') {
      orderStatus.value = '1';
    }
    if (key === '3') {
      orderStatus.value = '2';
    }
    getOrderList();
  };
  const getOrderList = () => {
    loading.value = true;
    let userId = userStore.user_id;
    userOrderListApi({ userId: userId, status: orderStatus.value })
      .then((res) => {
        res.data.forEach((item, index) => {
          if (item.cover) {
            item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover;
          }
        });
        orderData.value = res.data;
        loading.value = false;
      })
      .catch((err) => {
        console.log(err);
        loading.value = false;
      });
  };
  const handleDetail = (thingId) => {
    // 跳转新页面
    let text = router.resolve({ name: 'detail', query: { id: thingId } });
    window.open(text.href, '_blank');
  };
  const handleCancel = (item) => {
    cancelUserOrderApi({
      id: item.id,
    })
      .then((res) => {
        message.success('取消成功');
        getOrderList();
      })
      .catch((err) => {
        message.error(err.msg || '取消失败');
      });
  };
  const handlePay = (item) => {
    console.log('创建响应:', item);
    // 跳转到支付页面
    router.push({
      name: 'pay',
      query: {
        amount: item.price * item.count,
        orderNum: item.orderNumber,
        orderId: item.id,
      },
    });
  };
  const resetSearch = () => {
    searchText.value = '';
  };

  const onSearch = (value) => {
    searchText.value = value;
  };

  const filteredOrderData = computed(() => {
    if (!orderData.value || !searchText.value) {
      return orderData.value;
    }
    const searchQuery = searchText.value.toLowerCase().trim();
    return orderData.value.filter((item) => {
      return (
        (item.orderNumber && item.orderNumber.toLowerCase().includes(searchQuery)) ||
        (item.title && item.title.toLowerCase().includes(searchQuery))
      );
    });
  });
</script>
<style scoped lang="less">
  .flex-view {
    display: flex;
  }

  .content-list {
    flex: 1;

    .list-title {
      color: #152844;
      font-weight: 600;
      font-size: 20px;
      line-height: 24px;
      height: 24px;
      margin-bottom: 16px;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 0;
        width: 40px;
        height: 3px;
        background: #4684e2;
        border-radius: 2px;
      }
    }

    .search-container {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      padding: 16px;
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }
  }

  .custom-tabs {
    margin-bottom: 16px;

    :deep(.ant-tabs-nav::before) {
      border-bottom-color: #eaeaea;
    }

    :deep(.ant-tabs-tab) {
      padding: 8px 16px;
      font-size: 14px;
      transition: all 0.3s;

      &:hover {
        color: #4684e2;
      }
    }

    :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
      color: #4684e2;
      font-weight: 500;
    }

    :deep(.ant-tabs-ink-bar) {
      background-color: #4684e2;
      height: 3px;
      border-radius: 3px 3px 0 0;
    }
  }

  .empty-state {
    padding: 40px 0;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    text-align: center;
  }

  .order-item-view {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-top: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    .header {
      border-bottom: 1px solid #eaeaea;
      padding-bottom: 16px;
      justify-content: space-between;
      font-size: 14px;

      .text {
        color: #6f6f6f;
      }

      .mg-4 {
        margin-left: 4px;
      }

      .num {
        font-weight: 500;
        color: #152844;
      }

      .time {
        margin-left: 16px;
        color: #a1adc5;
      }

      .state {
        font-weight: 600;
        margin-left: 10px;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
      }

      .state-pending {
        color: #ff7b31;
        background-color: rgba(255, 123, 49, 0.1);
      }

      .state-paid {
        color: #4684e2;
        background-color: rgba(70, 132, 226, 0.1);
      }

      .state-canceled {
        color: #999;
        background-color: rgba(153, 153, 153, 0.1);
      }
    }

    .content {
      padding: 16px 0;
      overflow: hidden;

      .left-list {
        overflow: hidden;
        height: 132px;
        flex: 2;
        padding-right: 16px;

        .list-item {
          height: 70px;
          margin-bottom: 12px;
          overflow: hidden;
          cursor: pointer;
          padding: 10px;
          border-radius: 6px;
          transition: all 0.2s;

          &:hover {
            background-color: #f5f7fa;
          }
        }

        .thing-img {
          width: 50px;
          height: 100%;
          margin-right: 12px;
          border-radius: 4px;
          object-fit: cover;
        }

        .detail {
          flex: 1;
          flex-direction: column;
        }

        .flex-between {
          justify-content: space-between;
        }

        .flex-top {
          align-items: flex-start;
        }

        .name {
          color: #152844;
          font-weight: 600;
          font-size: 14px;
          line-height: 18px;
          max-width: 80%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .count {
          color: #484848;
          font-size: 12px;
          background-color: #f0f2f5;
          padding: 2px 6px;
          border-radius: 4px;
        }

        .flex-center {
          align-items: center;
        }

        .type {
          color: #6f6f6f;
          font-size: 12px;
        }

        .price {
          color: #ff7b31;
          font-weight: 600;
          font-size: 14px;
        }
      }

      .right-info {
        flex: 1;
        border-left: 1px solid #eaeaea;
        padding-left: 16px;
        line-height: 22px;
        font-size: 14px;

        .title {
          color: #6f6f6f;
          font-weight: 500;
          margin-bottom: 4px;
        }

        .name {
          color: #152844;
          margin-bottom: 4px;
        }

        .text {
          color: #484848;
          margin-bottom: 12px;
        }

        .mg {
          margin-bottom: 8px;
        }
      }
    }

    .bottom {
      align-items: center;
      border-top: 1px solid #eaeaea;
      justify-content: space-between;
      font-size: 14px;
      padding-top: 16px;

      .text {
        color: #6f6f6f;
      }

      .open {
        color: #4684e2;
        margin-left: 8px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          color: #2b5db0;
          text-decoration: underline;
        }
      }

      .right {
        align-items: center;
      }

      .text {
        color: #6f6f6f;
      }

      .num {
        color: #152844;
        margin: 0 40px 0 8px;
      }

      .money {
        font-weight: 600;
        font-size: 18px;
        color: #ff7b31;
        margin-left: 8px;
      }
    }
  }

  @media (max-width: 768px) {
    .order-item-view {
      .content {
        flex-direction: column;

        .left-list {
          height: auto;
          padding-right: 0;
          margin-bottom: 16px;
        }

        .right-info {
          border-left: none;
          border-top: 1px solid #eaeaea;
          padding-left: 0;
          padding-top: 16px;
        }
      }

      .bottom {
        flex-direction: column;
        align-items: flex-start;

        .left {
          margin-bottom: 12px;
        }

        .right {
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
  }
</style>
