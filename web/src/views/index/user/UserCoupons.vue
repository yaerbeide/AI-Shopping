<script>
  import { ref, onMounted, reactive } from 'vue';
  import { message, Tabs } from 'ant-design-vue';
  import { getUserCoupons, getSoonExpiredCoupons } from '/@/api/userCoupon';
  import { useUserStore } from '/@/store';

  export default {
    name: 'UserCoupons',
    setup() {
      const userStore = useUserStore();
      const coupons = reactive({
        unused: [], // 未使用
        used: [], // 已使用
        expired: [], // 已过期
        soonExpired: [], // 即将过期
      });
      const activeKey = ref('1');
      const loading = ref(false);
      const showExpiringAlert = ref(false);

      // 加载用户优惠券
      const loadUserCoupons = async (status = 1) => {
        if (!userStore.user_id) {
          message.warning('请先登录');
          return;
        }

        loading.value = true;
        try {
          const res = await getUserCoupons(userStore.user_id, status);
          if (res && res.code === 200) {
            const couponsData = res.data || [];

            if (status === 1) {
              // 过滤掉已过期的优惠券，将它们移到已过期列表中
              const now = new Date();
              const validCoupons = [];
              const expiredCoupons = [];

              couponsData.forEach((coupon) => {
                const expiryDate = coupon.endTime ? new Date(coupon.endTime) : null;
                if (expiryDate && now > expiryDate) {
                  // 已过期，但状态还是未使用
                  expiredCoupons.push(coupon);
                } else {
                  // 未过期
                  validCoupons.push(coupon);
                }
              });

              coupons.unused = validCoupons;

              // 如果找到了已过期但标记为未使用的优惠券，加载已过期标签页数据
              if (expiredCoupons.length > 0) {
                // 合并到已过期列表中
                loadUserCoupons(3);
              }
            } else if (status === 2) {
              coupons.used = couponsData;
            } else if (status === 3) {
              coupons.expired = couponsData;
            }
          } else {
            message.error(res?.message || '获取优惠券失败');
          }
        } catch (error) {
          console.error('获取用户优惠券列表失败', error);
          message.error('获取优惠券失败');
        } finally {
          loading.value = false;
        }
      };

      // 加载即将过期的优惠券
      const loadSoonExpiredCoupons = async () => {
        if (!userStore.user_id) {
          return;
        }

        try {
          const res = await getSoonExpiredCoupons(userStore.user_id, 7); // 获取7天内即将过期的优惠券
          if (res && res.code === 200) {
            coupons.soonExpired = res.data || [];

            // 如果有即将过期的优惠券，显示提醒
            if (coupons.soonExpired.length > 0) {
              showExpiringAlert.value = true;
            }
          }
        } catch (error) {
          console.error('获取即将过期优惠券失败', error);
        }
      };

      // 切换选项卡
      const handleTabChange = (key) => {
        activeKey.value = key;
        loadUserCoupons(Number(key));
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

      // 格式化日期
      const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
      };

      // 计算距离过期的天数
      const getDaysUntilExpiry = (expiryDate) => {
        if (!expiryDate) return 0;

        const now = new Date();
        const expiry = new Date(expiryDate);
        const diffTime = expiry - now;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        // 确保不返回负数
        return Math.max(0, diffDays);
      };

      // 判断优惠券是否已过期
      const isCouponExpired = (expiryDate) => {
        if (!expiryDate) return false;

        const now = new Date();
        const expiry = new Date(expiryDate);
        return now > expiry;
      };

      // 关闭过期提醒
      const closeExpiringAlert = () => {
        showExpiringAlert.value = false;
      };

      onMounted(() => {
        loadUserCoupons(1); // 默认加载未使用优惠券
        loadSoonExpiredCoupons(); // 加载即将过期的优惠券
      });

      return {
        coupons,
        activeKey,
        loading,
        showExpiringAlert,
        handleTabChange,
        formatCouponType,
        formatDate,
        getDaysUntilExpiry,
        isCouponExpired,
        closeExpiringAlert,
      };
    },
  };
</script>

<template>
  <div class="user-coupons">
    <h2 class="page-title">我的优惠券</h2>

    <!-- 即将过期提醒 -->
    <a-alert
      v-if="showExpiringAlert && coupons.soonExpired.length > 0"
      type="warning"
      class="expiring-alert"
      closable
      @close="closeExpiringAlert"
    >
      <template #message>
        <div class="alert-content">
          <span class="alert-title">优惠券即将过期提醒</span>
          <span class="alert-desc">您有 {{ coupons.soonExpired.length }} 张优惠券即将过期，请尽快使用！</span>
        </div>
      </template>
      <template #description>
        <div class="expiring-coupons-list">
          <div v-for="(coupon, index) in coupons.soonExpired" :key="coupon.id" class="expiring-coupon-item">
            <span class="coupon-name">{{ coupon.name }}</span>
            <span class="coupon-value">
              {{ coupon.type === 1 ? `满${coupon.threshold}减${coupon.amount}` : `${coupon.discount * 10}折` }}
            </span>
            <span class="expiry-date">
              <template v-if="isCouponExpired(coupon.endTime)"> 已过期 ({{ formatDate(coupon.endTime) }}) </template>
              <template v-else>
                剩余 <span class="days">{{ getDaysUntilExpiry(coupon.endTime) }}</span> 天过期 ({{ formatDate(coupon.endTime) }})
              </template>
            </span>
          </div>
        </div>
      </template>
    </a-alert>

    <a-tabs v-model:activeKey="activeKey" @change="handleTabChange">
      <a-tab-pane key="1" tab="未使用">
        <a-spin :spinning="loading">
          <div v-if="coupons.unused.length === 0" class="empty-tips"> 暂无未使用的优惠券 </div>
          <div v-else class="coupon-items">
            <div v-for="coupon in coupons.unused" :key="coupon.id" class="coupon-item">
              <div class="coupon-left" :class="{ discount: coupon.type === 2 }">
                <div class="coupon-amount">
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
                <div class="coupon-time">{{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}</div>
                <div class="coupon-status" v-if="!isCouponExpired(coupon.endTime)">可使用</div>
                <!-- 添加过期倒计时 -->
                <div v-if="getDaysUntilExpiry(coupon.endTime) <= 7 && !isCouponExpired(coupon.endTime)" class="coupon-expiry-countdown">
                  剩余 <span class="days">{{ getDaysUntilExpiry(coupon.endTime) }}</span> 天过期
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </a-tab-pane>

      <a-tab-pane key="2" tab="已使用">
        <a-spin :spinning="loading">
          <div v-if="coupons.used.length === 0" class="empty-tips"> 暂无已使用的优惠券 </div>
          <div v-else class="coupon-items">
            <div v-for="coupon in coupons.used" :key="coupon.id" class="coupon-item used">
              <div class="coupon-left" :class="{ discount: coupon.type === 2 }">
                <div class="coupon-amount">
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
                <div class="coupon-time">使用时间: {{ formatDate(coupon.usedTime) }}</div>
                <div class="coupon-status">已使用</div>
              </div>
              <div class="coupon-mask">
                <span>已使用</span>
              </div>
            </div>
          </div>
        </a-spin>
      </a-tab-pane>

      <a-tab-pane key="3" tab="已过期">
        <a-spin :spinning="loading">
          <div v-if="coupons.expired.length === 0" class="empty-tips"> 暂无已过期的优惠券 </div>
          <div v-else class="coupon-items">
            <div v-for="coupon in coupons.expired" :key="coupon.id" class="coupon-item expired">
              <div class="coupon-left" :class="{ discount: coupon.type === 2 }">
                <div class="coupon-amount">
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
                <div class="coupon-time">{{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}</div>
                <div class="coupon-status expired">已过期</div>
              </div>
              <div class="coupon-mask">
                <span>已过期</span>
              </div>
            </div>
          </div>
        </a-spin>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<style scoped>
  .user-coupons {
    padding: 24px;
    background: #fff;
  }
  .page-title {
    margin-bottom: 24px;
    font-size: 20px;
    font-weight: bold;
  }
  .coupon-items {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    padding: 16px 0;
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
  .coupon-item.used,
  .coupon-item.expired {
    opacity: 0.8;
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
    justify-content: space-between;
  }
  .coupon-name {
    font-weight: bold;
    font-size: 16px;
  }
  .coupon-time {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
  .coupon-status {
    color: #52c41a;
    margin-top: 8px;
  }
  .coupon-status.expired {
    color: #999;
  }
  .coupon-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    font-size: 24px;
    font-weight: bold;
  }
  .empty-tips {
    text-align: center;
    padding: 40px 0;
    color: #999;
  }

  /* 新增的样式 */
  .expiring-alert {
    margin-bottom: 20px;
  }
  .alert-content {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }
  .alert-title {
    font-weight: bold;
    margin-right: 10px;
  }
  .expiring-coupons-list {
    margin-top: 8px;
  }
  .expiring-coupon-item {
    padding: 8px 0;
    border-bottom: 1px dashed #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
  }
  .expiring-coupon-item:last-child {
    border-bottom: none;
  }
  .coupon-name {
    font-weight: bold;
    margin-right: 10px;
    flex: 1;
  }
  .coupon-value {
    color: #ff4d4f;
    margin-right: 10px;
  }
  .expiry-date {
    color: #999;
  }
  .days {
    color: #ff4d4f;
    font-weight: bold;
  }
  .coupon-expiry-countdown {
    margin-top: 5px;
    font-size: 12px;
    color: #ff4d4f;
  }
  .coupon-expiry-countdown.expired {
    color: #999;
    font-weight: bold;
  }
</style>
