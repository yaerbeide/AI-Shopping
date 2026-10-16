<script>
  import { ref, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { getAvailableCoupons } from '/@/api/coupon';
  import { receiveCoupon } from '/@/api/userCoupon';
  import { useUserStore } from '/@/store';

  export default {
    name: 'CouponList',
    props: {
      // 是否在弹窗中显示
      isModal: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['refresh'],
    setup(props, { emit }) {
      const userStore = useUserStore();
      const coupons = ref([]);
      const loading = ref(false);

      // 加载可用的优惠券
      const loadCoupons = async () => {
        loading.value = true;
        try {
          const res = await getAvailableCoupons();
          if (res && res.code === 200) {
            coupons.value = res.data || [];
          } else {
            message.error(res?.message || '获取优惠券失败');
          }
        } catch (error) {
          console.error('获取优惠券列表失败', error);
          message.error('获取优惠券失败');
        } finally {
          loading.value = false;
        }
      };

      // 领取优惠券
      const handleReceiveCoupon = async (couponId) => {
        if (!userStore.isLogin) {
          message.warning('请先登录');
          return;
        }

        try {
          const res = await receiveCoupon(userStore.userInfo.id, couponId);
          if (res && res.code === 200) {
            message.success(res.message || '领取成功');
            emit('refresh');
          } else {
            message.error(res?.message || '领取失败');
          }
        } catch (error) {
          console.error('领取优惠券失败', error);
          message.error('领取失败');
        }
      };

      // 格式化优惠券类型
      const formatCouponType = (coupon) => {
        if (coupon.type === 1) {
          return `满${coupon.threshold}减${coupon.amount}`;
        } else if (coupon.type === 2) {
          return `${coupon.discount * 10}折`;
        }
        return '未知类型';
      };

      onMounted(() => {
        loadCoupons();
      });

      return {
        coupons,
        loading,
        handleReceiveCoupon,
        formatCouponType,
      };
    },
  };
</script>

<template>
  <div :class="['coupon-list', { 'in-modal': isModal }]">
    <a-spin :spinning="loading">
      <div class="coupon-list-header">
        <h3>优惠券中心</h3>
      </div>
      <div class="coupon-list-content">
        <div v-if="coupons.length === 0" class="empty-tips"> 暂无可用优惠券 </div>
        <div v-else class="coupon-items">
          <div v-for="coupon in coupons" :key="coupon.id" class="coupon-item">
            <div class="coupon-left">
              <div class="coupon-amount" :class="{ discount: coupon.type === 2 }">
                <template v-if="coupon.type === 1">
                  <span class="amount-value">{{ coupon.amount }}</span>
                  <span class="amount-unit">元</span>
                </template>
                <template v-else-if="coupon.type === 2">
                  <span class="amount-value">{{ coupon.discount * 10 }}</span>
                  <span class="amount-unit">折</span>
                </template>
              </div>
              <div class="coupon-threshold" v-if="coupon.threshold > 0">满{{ coupon.threshold }}元可用</div>
              <div class="coupon-threshold" v-else>无门槛</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.name }}</div>
              <div class="coupon-time">{{ coupon.startTime }} ~ {{ coupon.endTime }}</div>
              <a-button type="primary" size="small" class="receive-btn" @click="handleReceiveCoupon(coupon.id)"> 立即领取 </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
  .coupon-list {
    width: 100%;
  }
  .coupon-list.in-modal {
    padding: 0 16px;
  }
  .coupon-list-header {
    margin-bottom: 16px;
  }
  .coupon-items {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  .coupon-item {
    width: calc(100% - 16px);
    height: 120px;
    display: flex;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
    border-radius: 4px;
    overflow: hidden;
    position: relative;
  }
  .coupon-left {
    width: 120px;
    background: #ff4d4f;
    color: #fff;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    position: relative;
  }
  .coupon-left::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    border-right: 1px dashed #fff;
  }
  .coupon-left.discount {
    background: #52c41a;
  }
  .coupon-amount {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 8px;
  }
  .amount-value {
    font-size: 24px;
    margin-right: 4px;
  }
  .coupon-threshold {
    font-size: 12px;
  }
  .coupon-right {
    flex: 1;
    padding: 16px;
    display: flex;
    flex-direction: column;
  }
  .coupon-name {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 8px;
  }
  .coupon-time {
    font-size: 12px;
    color: #999;
    margin-bottom: 16px;
  }
  .receive-btn {
    align-self: flex-end;
    margin-top: auto;
  }
  .empty-tips {
    text-align: center;
    color: #999;
    padding: 32px 0;
    width: 100%;
  }
</style>
