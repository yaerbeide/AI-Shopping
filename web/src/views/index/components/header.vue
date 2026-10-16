<template>
  <div class="main-bar-view">
    <div class="header-container">
      <div class="logo" @click="$router.push({ name: 'portal' })">
        <div class="logo-circle">C</div>
        <span class="logo-text">Capēd</span>
      </div>

      <div class="search-entry">
        <div class="search-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input placeholder="Search products..." ref="keywordRef" @keyup.enter="search" />
        </div>
      </div>

      <div class="right-view">
        <div class="nav-menu">
          <router-link to="/index/portal" class="nav-menu-item" :class="{ active: route.name === 'portal' }">
            产品 <span class="en">Store</span>
          </router-link>
          <router-link to="/index/search" class="nav-menu-item" :class="{ active: route.name === 'search' }">
            探索 <span class="en">Explore</span>
          </router-link>
          <router-link to="/index/cart" class="nav-menu-item" :class="{ active: route.name === 'cartView' }">
            购物车 <span class="en">Cart</span>
          </router-link>
          <router-link to="/index/couponCenter" class="nav-menu-item" :class="{ active: route.name === 'couponCenter' }">
            优惠券 <span class="en">Coupons</span>
          </router-link>
          <div class="nav-menu-item" @click="msgVisible = true"> 消息 <span class="en">Inbox</span> </div>
          <router-link to="/index/usercenter" class="nav-menu-item" :class="{ active: route.name === 'usercenter' }">
            我的 <span class="en">Profile</span>
          </router-link>
        </div>

        <div class="action-icons">
          <div class="icon-item" @click="$router.push({ name: 'cartView' })">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
          </div>

          <template v-if="userStore.user_token">
            <a-dropdown>
              <a class="ant-dropdown-link user-dropdown" @click="(e) => e.preventDefault()">
                <div class="icon-item">
                  <img v-if="userAvatar" :src="userAvatar" class="self-img" />
                  <img v-else :src="AvatarIcon" class="self-img" />
                </div>
              </a>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <a @click="goUserCenter('orderView')">My Orders</a>
                  </a-menu-item>
                  <a-menu-item>
                    <a @click="goUserCenter('userInfoEditView')">Settings</a>
                  </a-menu-item>
                  <a-menu-item>
                    <a class="foot-link" @click="goAdminDashboard">Dashboard</a>
                  </a-menu-item>
                  <a-menu-item>
                    <a @click="quit()">Sign Out</a>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <template v-else>
            <div class="icon-item" @click="goLogin()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
          </template>
        </div>
      </div>
    </div>

    <a-drawer
      placement="right"
      :closable="true"
      :maskClosable="true"
      :visible="msgVisible"
      :width="460"
      :bodyStyle="{ padding: 0 }"
      class="message-drawer"
      @close="onClose"
    >
      <template #title>
        <div class="message-drawer-title">
          <span>我的消息</span>
          <small>INBOX</small>
        </div>
      </template>

      <div class="message-panel">
        <section class="message-summary">
          <div>
            <span class="summary-kicker">NOTIFICATIONS</span>
            <h3>{{ msgData.length }} 条消息</h3>
            <p>{{ latestMessageText }}</p>
          </div>
          <div class="summary-count">
            <strong>{{ msgData.length }}</strong>
            <span>全部</span>
          </div>
        </section>

        <div class="message-tools">
          <button type="button" :class="{ active: messageFilter === 'all' }" @click="messageFilter = 'all'">全部</button>
          <button type="button" :class="{ active: messageFilter === 'latest' }" @click="messageFilter = 'latest'">最新</button>
        </div>

        <a-spin :spinning="loading" style="min-height: 220px">
          <div v-if="visibleMessages.length" class="message-list">
            <article
              class="message-card"
              :class="{ featured: index === 0 }"
              v-for="(item, index) in visibleMessages"
              :key="item.id"
            >
              <div class="message-rank">{{ index + 1 }}</div>
              <div class="message-body">
                <div class="message-card-head">
                  <h4>{{ item.title }}</h4>
                  <span>{{ formatNoticeTime(item) }}</span>
                </div>
                <p>{{ item.content }}</p>
              </div>
            </article>
          </div>

          <div v-else class="message-empty">
            <div class="empty-mark">C</div>
            <h3>暂无消息</h3>
            <p>新的公告和通知会显示在这里。</p>
          </div>
        </a-spin>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { listApi } from '/@/api/notice';
  import { detailApi } from '/@/api/user';
  import { useUserStore } from '/@/store';
  import { BASE_URL } from '/@/store/constants';
  import AvatarIcon from '/@/assets/images/avatar.jpg';
  import { computed, ref, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();

  const keywordRef = ref();

  let loading = ref(false);
  let msgVisible = ref(false);
  let msgData = ref([] as any);
  let userAvatar = ref('');
  let nickName = ref('');
  let messageFilter = ref('all');

  const sortedMessages = computed(() => {
    return [...msgData.value].sort((a, b) => getNoticeTimestamp(b) - getNoticeTimestamp(a));
  });

  const visibleMessages = computed(() => {
    if (messageFilter.value === 'latest') {
      return sortedMessages.value.slice(0, 5);
    }
    return sortedMessages.value;
  });

  const latestMessageText = computed(() => {
    if (!msgData.value.length) return '暂无新的系统通知';
    const latest = sortedMessages.value[0];
    return latest && latest.title ? `最新：${latest.title}` : '有新的商城消息等待查看';
  });

  const getNoticeRawTime = (item: any) => {
    return item?.createTime || item?.create_time || item?.createAt || item?.create_at || item?.time || '';
  };

  const parseNoticeDate = (value: any) => {
    if (!value) return null;
    if (value instanceof Date) return isNaN(value.getTime()) ? null : value;

    const text = String(value).trim();
    if (!text) return null;

    if (/^\d+$/.test(text)) {
      const timestamp = Number(text);
      const millis = text.length <= 10 ? timestamp * 1000 : timestamp;
      const date = new Date(millis);
      return isNaN(date.getTime()) ? null : date;
    }

    const normalized = text.includes('T') ? text : text.replace(/-/g, '/');
    const date = new Date(normalized);
    return isNaN(date.getTime()) ? null : date;
  };

  const getNoticeTimestamp = (item: any) => {
    const date = parseNoticeDate(getNoticeRawTime(item));
    return date ? date.getTime() : 0;
  };

  const formatNoticeTime = (item: any) => {
    const date = parseNoticeDate(getNoticeRawTime(item));
    if (!date) return '暂无时间';

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}`;
  };

  onMounted(() => {
    getMessageList();
    getUserInfo();
  });

  const getUserInfo = () => {
    let userId = userStore.user_id;
    if (!userId) return;

    detailApi({ userId: userId })
      .then((res) => {
        if (res.data) {
          userAvatar.value = BASE_URL + '/api/staticfiles/avatar/' + res.data.avatar;
          nickName.value = res.data.nickname;
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const getMessageList = () => {
    loading.value = true;
    listApi({})
      .then((res) => {
        msgData.value = res.data;
        loading.value = false;
      })
      .catch((err) => {
        console.log(err);
        loading.value = false;
      });
  };
  const search = () => {
    const keyword = keywordRef.value.value;
    if (!keyword) return;

    router.push({ name: 'search', query: { keyword: keyword } });
  };
  const goLogin = () => {
    router.push({ name: 'login' });
  };

  const goUserCenter = (menuName) => {
    router.push({ name: menuName });
  };
  const goAdminDashboard = () => {
    router.push({ name: 'admin' });
  };
  const quit = () => {
    userStore.logout().then((res) => {
      router.push({ name: 'portal' });
    });
  };
  const onClose = () => {
    msgVisible.value = false;
  };
</script>

<style scoped lang="less">
  .main-bar-view {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    background: #fff;
    z-index: 1000;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .header-container {
    max-width: 1400px;
    height: 60px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 40px;
  }

  .logo {
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 10px;

    .logo-circle {
      width: 32px;
      height: 32px;
      background-color: #ffd100;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 18px;
      color: #333;
    }

    .logo-text {
      font-size: 22px;
      font-weight: 700;
      color: #333;
      letter-spacing: -0.5px;
    }
  }

  .search-entry {
    flex: 1;
    max-width: 400px;
    margin: 0 40px;

    .search-wrapper {
      position: relative;
      display: flex;
      align-items: center;
      background-color: #f5f5f5;
      border-radius: 20px;
      padding: 0 15px;
      height: 40px;
      transition: all 0.3s;

      &:focus-within {
        background-color: #eee;
      }

      .search-icon {
        width: 18px;
        height: 18px;
        color: #666;
        margin-right: 10px;
      }

      input {
        width: 100%;
        background: transparent;
        border: none;
        outline: none;
        font-size: 14px;
        color: #333;

        &::placeholder {
          color: #999;
        }
      }
    }
  }

  .right-view {
    display: flex;
    align-items: center;
    gap: 30px;

    .nav-menu {
      display: flex;
      align-items: center;
      gap: 25px;

      .nav-menu-item {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        text-decoration: none;
        cursor: pointer;
        position: relative;
        padding-bottom: 4px;
        display: flex;
        flex-direction: column;
        align-items: center;
        line-height: 1.2;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 0;
          width: 100%;
          height: 2px;
          background-color: #333;
          opacity: 0;
          transform: scaleX(0);
          transform-origin: left center;
          transition: transform 180ms ease, opacity 180ms ease;
        }

        .en {
          font-size: 10px;
          text-transform: uppercase;
          opacity: 0.5;
          font-weight: 600;
          letter-spacing: 0.5px;
          transition: opacity 180ms ease, color 180ms ease;
        }

        &:hover,
        &.active {
          &::after {
            opacity: 1;
            transform: scaleX(1);
          }
        }
      }
    }

    .action-icons {
      display: flex;
      align-items: center;
      gap: 20px;

      .icon-item {
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        transition: background-color 180ms ease, transform 180ms ease, box-shadow 180ms ease;

        &:hover {
          background-color: #f5f5f5;
          transform: translateY(-1px);
        }

        svg {
          width: 22px;
          height: 22px;
          color: #333;
        }

        .self-img {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          object-fit: cover;
          transition: transform 180ms ease;
        }
      }
    }
  }

  :deep(.message-drawer .ant-drawer-content) {
    background: #f7f8fa;
  }

  :deep(.message-drawer .ant-drawer-header) {
    padding: 18px 24px;
    background: #fff;
    border-bottom: 1px solid #edf0f4;
  }

  :deep(.message-drawer .ant-drawer-title) {
    line-height: 1;
  }

  .message-drawer-title {
    display: flex;
    align-items: baseline;
    gap: 10px;

    span {
      color: #101828;
      font-size: 18px;
      font-weight: 800;
    }

    small {
      color: #98a2b3;
      font-size: 11px;
      font-weight: 900;
      letter-spacing: 0.08em;
    }
  }

  .message-panel {
    min-height: 100%;
    padding: 18px;
  }

  .message-summary {
    min-height: 150px;
    border-radius: 22px;
    background:
      linear-gradient(90deg, rgba(16, 24, 40, 0.92), rgba(16, 24, 40, 0.76)),
      linear-gradient(135deg, #222831, #4b5563);
    color: #fff;
    padding: 22px;
    display: flex;
    justify-content: space-between;
    gap: 18px;
    align-items: flex-end;
    box-shadow: 0 18px 42px rgba(16, 24, 40, 0.16);

    h3 {
      margin: 10px 0 8px;
      color: #fff;
      font-size: 30px;
      line-height: 1.2;
      font-weight: 900;
    }

    p {
      margin: 0;
      color: rgba(255, 255, 255, 0.72);
      line-height: 1.6;
      max-width: 260px;
    }
  }

  .summary-kicker {
    color: #ffd100;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.1em;
  }

  .summary-count {
    width: 86px;
    height: 86px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.13);
    display: grid;
    place-items: center;
    align-content: center;
    flex: 0 0 auto;

    strong {
      color: #ffd100;
      font-size: 30px;
      line-height: 1;
      font-weight: 900;
    }

    span {
      margin-top: 6px;
      color: rgba(255, 255, 255, 0.75);
      font-size: 12px;
      font-weight: 700;
    }
  }

  .message-tools {
    display: flex;
    gap: 8px;
    margin: 16px 0 14px;
    padding: 6px;
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 999px;

    button {
      flex: 1;
      height: 36px;
      border: 0;
      border-radius: 999px;
      background: transparent;
      color: #667085;
      font-weight: 800;
      cursor: pointer;
      transition: background 0.2s ease, color 0.2s ease;

      &.active {
        background: #222831;
        color: #fff;
      }
    }
  }

  .message-list {
    display: grid;
    gap: 12px;
    padding-bottom: 18px;
  }

  .message-card {
    display: grid;
    grid-template-columns: 42px minmax(0, 1fr);
    gap: 14px;
    align-items: start;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    background: #fff;
    padding: 16px;
    transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      border-color: #ffd100;
      box-shadow: 0 14px 28px rgba(16, 24, 40, 0.08);
    }

    &.featured {
      background: #fffdf1;
      border-color: #ffe58f;
    }
  }

  .message-rank {
    width: 42px;
    height: 42px;
    border-radius: 14px;
    background: #222831;
    color: #ffd100;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 900;
  }

  .message-body {
    min-width: 0;
  }

  .message-card-head {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    align-items: flex-start;
    margin-bottom: 8px;

    h4 {
      margin: 0;
      color: #101828;
      font-size: 15px;
      line-height: 1.45;
      font-weight: 800;
    }

    span {
      color: #98a2b3;
      font-size: 12px;
      white-space: nowrap;
    }
  }

  .message-body p {
    margin: 0;
    color: #475467;
    line-height: 1.7;
    word-break: break-word;
  }

  .message-empty {
    min-height: 260px;
    display: grid;
    place-items: center;
    align-content: center;
    text-align: center;
    color: #667085;

    .empty-mark {
      width: 58px;
      height: 58px;
      border-radius: 50%;
      background: #ffd100;
      color: #222831;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      font-weight: 900;
      margin-bottom: 14px;
    }

    h3 {
      margin: 0 0 8px;
      color: #101828;
      font-size: 18px;
    }

    p {
      margin: 0;
    }
  }

  .flex-view {
    display: flex;
  }

  @media (max-width: 520px) {
    :deep(.message-drawer .ant-drawer-content-wrapper) {
      width: 100% !important;
    }

    .message-panel {
      padding: 14px;
    }

    .message-summary {
      min-height: 132px;
      padding: 18px;
    }

    .message-summary h3 {
      font-size: 25px;
    }

    .summary-count {
      width: 72px;
      height: 72px;
      border-radius: 20px;
    }
  }
</style>
