<template>
  <div class="coupon-center">
    <IndexHeader />

    <main class="coupon-page">
      <section class="coupon-hero">
        <div>
          <div class="eyebrow">COUPON CENTER</div>
          <h1>优惠券中心</h1>
          <p>领取专属优惠，在结算时自动抵扣更划算。</p>
        </div>
        <div class="hero-actions">
          <button type="button" @click="goShopping">去逛商品</button>
          <button type="button" class="light" @click="goMyCoupons">我的优惠券</button>
        </div>
      </section>

      <section class="coupon-overview">
        <div class="overview-card dark">
          <span>AVAILABLE</span>
          <strong>{{ coupons.length }}</strong>
          <p>当前可领取</p>
        </div>
        <div class="overview-card">
          <span>RECEIVED</span>
          <strong>{{ receivedCount }}</strong>
          <p>已领取</p>
        </div>
        <div class="overview-card">
          <span>BEST VALUE</span>
          <strong>{{ bestCouponText }}</strong>
          <p>最高优惠</p>
        </div>
      </section>

      <section class="coupon-content">
        <div class="section-head">
          <div>
            <h2>精选优惠</h2>
            <p>每张券都有使用门槛和有效期，领取后可在下单时选择。</p>
          </div>
          <div class="filter-tabs">
            <button type="button" :class="{ active: filterType === 'all' }" @click="filterType = 'all'">全部</button>
            <button type="button" :class="{ active: filterType === 'cash' }" @click="filterType = 'cash'">满减</button>
            <button type="button" :class="{ active: filterType === 'discount' }" @click="filterType = 'discount'">折扣</button>
          </div>
        </div>

        <a-spin :spinning="loading" style="min-height: 320px">
          <div v-if="filteredCoupons.length === 0 && !loading" class="empty-state">
            <div class="empty-icon">
              <gift-outlined />
            </div>
            <h3>暂无可领取的优惠券</h3>
            <p>稍后再来看看，或者先去浏览商品。</p>
            <button type="button" @click="goShopping">去购物</button>
          </div>

          <div v-else class="coupon-grid">
            <article
              v-for="coupon in filteredCoupons"
              :key="coupon.id"
              class="coupon-card"
              :class="{ discount: coupon.type === 2, received: coupon.received }"
            >
              <div class="coupon-value">
                <span class="tag">{{ coupon.type === 2 ? 'DISCOUNT' : 'COUPON' }}</span>
                <div class="amount">
                  <template v-if="coupon.type === 1">
                    <small>￥</small>{{ coupon.amount }}
                  </template>
                  <template v-else-if="coupon.type === 2">
                    {{ formatDiscount(coupon.discount) }}<small>折</small>
                  </template>
                </div>
                <p>{{ coupon.threshold > 0 ? `满 ${coupon.threshold} 元可用` : '无门槛' }}</p>
              </div>

              <div class="coupon-info">
                <div>
                  <h3>{{ coupon.name }}</h3>
                  <p class="date">
                    <calendar-outlined />
                    {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
                  </p>
                </div>

                <div class="coupon-bottom">
                  <span class="status" :class="{ done: coupon.received }">
                    {{ coupon.received ? '已领取' : '可领取' }}
                  </span>
                  <a-button
                    type="primary"
                    :disabled="coupon.received"
                    :loading="receiving[coupon.id]"
                    @click="handleReceive(coupon)"
                  >
                    {{ coupon.received ? '已领取' : '立即领取' }}
                  </a-button>
                </div>
              </div>
            </article>
          </div>
        </a-spin>
      </section>
    </main>
  </div>
</template>

<script setup>
  import { computed, ref, onMounted, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import {
    CalendarOutlined,
    GiftOutlined,
  } from '@ant-design/icons-vue';
  import IndexHeader from './components/header.vue';
  import { useUserStore } from '@/store';
  import { receiveCoupon, getUserCoupons } from '/@/api/userCoupon';
  import { getAvailableCoupons } from '/@/api/coupon';

  const router = useRouter();
  const userStore = useUserStore();
  const coupons = ref([]);
  const loading = ref(false);
  const receiving = reactive({});
  const filterType = ref('all');

  const receivedCount = computed(() => coupons.value.filter((item) => item.received).length);

  const filteredCoupons = computed(() => {
    if (filterType.value === 'cash') {
      return coupons.value.filter((item) => item.type === 1);
    }
    if (filterType.value === 'discount') {
      return coupons.value.filter((item) => item.type === 2);
    }
    return coupons.value;
  });

  const bestCouponText = computed(() => {
    if (!coupons.value.length) return '--';
    const cashValues = coupons.value.filter((item) => item.type === 1).map((item) => Number(item.amount || 0));
    const discountValues = coupons.value.filter((item) => item.type === 2).map((item) => Number(item.discount || 1));
    const maxCash = cashValues.length ? Math.max(...cashValues) : 0;
    const minDiscount = discountValues.length ? Math.min(...discountValues) : 1;
    if (maxCash > 0) return `￥${maxCash}`;
    if (minDiscount < 1) return `${formatDiscount(minDiscount)}折`;
    return '--';
  });

  const formatDate = (dateString) => {
    if (!dateString) return '--';
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };

  const formatDiscount = (discount) => {
    const value = Number(discount || 0) * 10;
    return Number.isInteger(value) ? value : value.toFixed(1);
  };

  const fetchAvailableCoupons = async () => {
    loading.value = true;
    try {
      const res = await getAvailableCoupons();

      if (res && res.code === 200) {
        const availableCoupons = res.data || [];

        if (userStore.user_id) {
          try {
            const userCouponsRes = await getUserCoupons(userStore.user_id, -1);
            if (userCouponsRes && userCouponsRes.code === 200) {
              const userCouponIds = (userCouponsRes.data || []).map((uc) => (uc.coupon ? uc.coupon.id : uc.couponId));
              coupons.value = availableCoupons.map((coupon) => ({
                ...coupon,
                received: userCouponIds.includes(coupon.id),
              }));
            } else {
              markAllUnreceived(availableCoupons);
            }
          } catch (error) {
            console.error('获取用户优惠券列表失败', error);
            markAllUnreceived(availableCoupons);
          }
        } else {
          markAllUnreceived(availableCoupons);
        }
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

  const markAllUnreceived = (availableCoupons) => {
    coupons.value = availableCoupons.map((item) => ({
      ...item,
      received: false,
    }));
  };

  const handleReceive = async (coupon) => {
    if (!userStore.user_id) {
      message.warning('请先登录');
      return;
    }

    receiving[coupon.id] = true;
    try {
      const res = await receiveCoupon(userStore.user_id, coupon.id);
      if (res && res.code === 200) {
        message.success('领取成功');
        coupon.received = true;
      } else {
        message.error(res?.message || '领取失败');
      }
    } catch (error) {
      console.error('领取优惠券失败', error);
      message.error('领取失败');
    } finally {
      receiving[coupon.id] = false;
    }
  };

  const goShopping = () => {
    router.push({ name: 'portal' });
  };

  const goMyCoupons = () => {
    router.push({ name: 'userCouponsView' });
  };

  onMounted(() => {
    fetchAvailableCoupons();
  });
</script>

<style scoped lang="less">
  .coupon-center {
    min-height: 100vh;
    background:
      radial-gradient(circle at 8% 8%, rgba(255, 209, 0, 0.2), transparent 24%),
      linear-gradient(180deg, #f6f8fa 0%, #fff 55%);
  }

  .coupon-page {
    width: min(1320px, calc(100% - 48px));
    margin: 0 auto;
    padding: 112px 0 80px;
  }

  .coupon-hero {
    min-height: 220px;
    border-radius: 32px;
    padding: 40px;
    color: #fff;
    background:
      linear-gradient(90deg, rgba(24, 27, 34, 0.94), rgba(24, 27, 34, 0.66)),
      url('/src/assets/images/banner-02.webp') center/cover;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 24px;
    box-shadow: 0 24px 56px rgba(15, 23, 42, 0.16);

    .eyebrow {
      color: #ffd100;
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.1em;
    }

    h1 {
      margin: 8px 0;
      color: #fff;
      font-size: 44px;
      font-weight: 900;
    }

    p {
      margin: 0;
      color: rgba(255, 255, 255, 0.76);
      font-weight: 600;
    }
  }

  .hero-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    button {
      height: 44px;
      border: 0;
      border-radius: 999px;
      background: #ffd100;
      color: #24272d;
      padding: 0 20px;
      font-weight: 900;
      cursor: pointer;

      &.light {
        background: rgba(255, 255, 255, 0.16);
        color: #fff;
        backdrop-filter: blur(12px);
      }
    }
  }

  .coupon-overview {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 20px;
    margin: 26px 0;
  }

  .overview-card {
    background: #fff;
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 16px 44px rgba(15, 23, 42, 0.06);

    &.dark {
      background: #24272d;
      color: #fff;

      span,
      p {
        color: rgba(255, 255, 255, 0.64);
      }

      strong {
        color: #ffd100;
      }
    }

    span {
      color: #9aa0aa;
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.08em;
    }

    strong {
      display: block;
      color: #24272d;
      font-size: 34px;
      font-weight: 900;
      margin: 8px 0 4px;
    }

    p {
      color: #8b929d;
      margin: 0;
      font-weight: 700;
    }
  }

  .coupon-content {
    background: #fff;
    border-radius: 28px;
    padding: 30px;
    box-shadow: 0 18px 48px rgba(15, 23, 42, 0.07);
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 24px;

    h2 {
      margin: 0 0 6px;
      color: #24272d;
      font-size: 28px;
      font-weight: 900;
    }

    p {
      color: #8b929d;
      margin: 0;
      font-weight: 600;
    }
  }

  .filter-tabs {
    display: flex;
    gap: 8px;
    padding: 5px;
    border-radius: 999px;
    background: #f5f6f8;

    button {
      height: 36px;
      border: 0;
      border-radius: 999px;
      background: transparent;
      color: #767d88;
      padding: 0 18px;
      font-weight: 900;
      cursor: pointer;

      &.active {
        background: #24272d;
        color: #fff;
      }
    }
  }

  .coupon-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 22px;
  }

  .coupon-card {
    position: relative;
    min-height: 190px;
    border-radius: 26px;
    overflow: hidden;
    display: grid;
    grid-template-columns: 190px minmax(0, 1fr);
    background: #f8f9fb;
    box-shadow: inset 0 0 0 1px #edf0f5;
    transition: transform 0.22s, box-shadow 0.22s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 20px 46px rgba(15, 23, 42, 0.1);
    }

    &.received {
      opacity: 0.68;
    }
  }

  .coupon-value {
    position: relative;
    padding: 24px;
    background: #ff4d57;
    color: #fff;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    &::after {
      content: '';
      position: absolute;
      top: 18px;
      right: -1px;
      bottom: 18px;
      border-right: 2px dashed rgba(255, 255, 255, 0.55);
    }

    .discount & {
      background: #16b887;
    }

    .tag {
      color: rgba(255, 255, 255, 0.78);
      font-size: 11px;
      font-weight: 900;
      letter-spacing: 0.08em;
    }

    .amount {
      font-size: 42px;
      font-weight: 900;
      line-height: 1;

      small {
        font-size: 17px;
        margin-right: 2px;
      }
    }

    p {
      margin: 0;
      color: rgba(255, 255, 255, 0.86);
      font-weight: 800;
    }
  }

  .coupon-info {
    padding: 24px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;

    h3 {
      color: #24272d;
      font-size: 20px;
      font-weight: 900;
      margin: 0 0 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .date {
      color: #8b929d;
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-weight: 700;
    }
  }

  .coupon-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
  }

  .status {
    height: 30px;
    border-radius: 999px;
    background: #fff;
    color: #ff4d57;
    padding: 0 12px;
    display: inline-flex;
    align-items: center;
    font-weight: 900;
    font-size: 12px;

    &.done {
      color: #8b929d;
    }
  }

  :deep(.ant-btn-primary) {
    height: 42px;
    border-radius: 999px;
    background: #24272d;
    border-color: #24272d;
    font-weight: 900;
    box-shadow: 0 10px 22px rgba(36, 39, 45, 0.16);
  }

  .empty-state {
    min-height: 360px;
    border-radius: 24px;
    background: #f8f9fb;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;

    .empty-icon {
      width: 76px;
      height: 76px;
      border-radius: 26px;
      background: #ffd100;
      color: #24272d;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
    }

    h3 {
      margin: 20px 0 8px;
      color: #24272d;
      font-size: 26px;
      font-weight: 900;
    }

    p {
      color: #8b929d;
      margin-bottom: 22px;
    }

    button {
      height: 42px;
      border: 0;
      border-radius: 999px;
      background: #24272d;
      color: #fff;
      padding: 0 20px;
      font-weight: 900;
      cursor: pointer;
    }
  }

  @media (max-width: 1060px) {
    .coupon-grid {
      grid-template-columns: 1fr;
    }

    .coupon-overview {
      grid-template-columns: 1fr 1fr;
    }
  }

  @media (max-width: 760px) {
    .coupon-page {
      width: min(100% - 28px, 1320px);
      padding-top: 96px;
    }

    .coupon-hero {
      padding: 28px;
      align-items: flex-start;
      flex-direction: column;

      h1 {
        font-size: 34px;
      }
    }

    .coupon-overview {
      grid-template-columns: 1fr;
    }

    .coupon-content {
      padding: 20px;
      border-radius: 24px;
    }

    .section-head {
      align-items: flex-start;
      flex-direction: column;
    }

    .coupon-card {
      grid-template-columns: 1fr;
    }

    .coupon-value {
      min-height: 150px;

      &::after {
        left: 18px;
        right: 18px;
        top: auto;
        bottom: -1px;
        border-right: 0;
        border-bottom: 2px dashed rgba(255, 255, 255, 0.55);
      }
    }

    .coupon-bottom {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
