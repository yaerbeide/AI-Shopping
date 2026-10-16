<template>
  <a-layout class="admin-layout">
    <div v-if="isMobile && !collapsed" class="sider-mask" @click="collapsed = true"></div>
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      class="admin-sider"
      :width="260"
      :collapsedWidth="isMobile ? 0 : 80"
      theme="light"
    >
      <div class="logo-wrapper" @click="router.push({ name: 'overview' })">
        <div class="logo-icon">
          <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path
              d="M12 18.5C12 14.3579 15.3579 11 19.5 11H34.5C40.8513 11 46 16.1487 46 22.5V40.5C46 46.8513 40.8513 52 34.5 52H25.5C17.4919 52 11 45.5081 11 37.5V24.5C11 21.4624 11.8065 19.6596 12 18.5Z"
              fill="#1AA7EC"
            />
            <path
              d="M53 23C53 20.2386 50.7614 18 48 18H35C29.4772 18 25 22.4772 25 28V41C25 46.5228 29.4772 51 35 51H40C47.1797 51 53 45.1797 53 38V23Z"
              fill="#22C58E"
            />
            <path
              d="M20.5 26.5C20.5 24.0147 22.5147 22 25 22H35C39.4183 22 43 25.5817 43 30V34C43 38.4183 39.4183 42 35 42H29C24.3056 42 20.5 38.1944 20.5 33.5V26.5Z"
              fill="#ffffff"
              fill-opacity="0.2"
            />
          </svg>
        </div>
        <span v-if="!collapsed" class="logo-text">Groth</span>
      </div>

      <a-menu class="admin-menu" v-model:selectedKeys="selectedKeys" mode="inline" @click="handleClick">
        <a-menu-item key="overview">
          <template #icon><dashboard-outlined /></template>
          <span>仪表盘</span>
        </a-menu-item>

        <a-menu-divider />

        <a-menu-item key="order">
          <template #icon><file-text-outlined /></template>
          <span>订单管理</span>
        </a-menu-item>
        <a-menu-item key="thing">
          <template #icon><shopping-cart-outlined /></template>
          <span>商品管理</span>
        </a-menu-item>
        <a-menu-item key="user">
          <template #icon><user-outlined /></template>
          <span>用户管理</span>
        </a-menu-item>
        <a-menu-item key="comment">
          <template #icon><comment-outlined /></template>
          <span>评论管理</span>
        </a-menu-item>

        <a-menu-divider />

        <a-sub-menu key="config">
          <template #icon><appstore-outlined /></template>
          <template #title>业务配置</template>
          <a-menu-item key="classification">分类管理</a-menu-item>
          <a-menu-item key="tag">标签管理</a-menu-item>
          <a-menu-item key="coupon">优惠券管理</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="marketing">
          <template #icon><fund-outlined /></template>
          <template #title>运营管理</template>
          <a-menu-item key="notice">通知公告</a-menu-item>
          <a-menu-item key="banner">轮播图管理</a-menu-item>
        </a-sub-menu>

        <a-sub-menu key="system">
          <template #icon><setting-outlined /></template>
          <template #title>系统管理</template>
          <a-menu-item key="loginLog">登录日志</a-menu-item>
          <a-menu-item key="opLog">操作日志</a-menu-item>
          <a-menu-item key="errorLog">错误日志</a-menu-item>
          <a-menu-item key="sysInfo">系统信息</a-menu-item>
        </a-sub-menu>
      </a-menu>

      <div class="sider-footer">
        <div class="user-menu-item" @click="handleProfileClick">
          <user-outlined />
          <span v-if="!collapsed">用户信息</span>
        </div>
        <div class="user-menu-item logout" @click="handleLogout">
          <logout-outlined />
          <span v-if="!collapsed">退出登录</span>
        </div>
      </div>
    </a-layout-sider>

    <a-layout class="main-container">
      <!-- 顶部栏 -->
      <a-layout-header class="admin-header">
        <div class="header-left">
          <button class="sider-toggle" type="button" @click="collapsed = !collapsed">
            <menu-unfold-outlined v-if="collapsed" />
            <menu-fold-outlined v-else />
          </button>

          <div class="welcome-text">
            <h2>欢迎您，{{ userStore.admin_user_name || '管理员' }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>

        <div class="header-right">
          <div class="header-icons">
            <div class="icon-btn" @click="handlePreview" title="前台预览">
              <eye-outlined />
            </div>
          </div>

          <a-divider type="vertical" class="header-divider" />

          <div class="search-bar">
            <search-outlined class="search-icon" />
            <input type="text" placeholder="搜索" />
          </div>

          <div class="user-avatar-wrapper" @click="handleProfileClick">
            <img v-if="userAvatar" :src="userAvatar" class="avatar-img" />
            <div v-else class="avatar-placeholder">{{ (userStore.admin_user_name || 'A').charAt(0) }}</div>
          </div>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
  import { useRouter, useRoute } from 'vue-router';
  import { onMounted, ref, onUnmounted } from 'vue';
  import { detailApi } from '/@/api/user';
  import { BASE_URL } from '/@/store/constants';
  import dayjs from 'dayjs';
  import {
    UserOutlined,
    DashboardOutlined,
    ShoppingOutlined,
    FileTextOutlined,
    AppstoreOutlined,
    NotificationOutlined,
    SettingOutlined,
    LogoutOutlined,
    SearchOutlined,
    BellOutlined,
    EyeOutlined,
    PartitionOutlined,
    TagOutlined,
    GiftOutlined,
    ShoppingCartOutlined,
    CommentOutlined,
    FundOutlined,
    MessageOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
  } from '@ant-design/icons-vue';

  import { useUserStore } from '/@/store';

  const userStore = useUserStore();
  const userAvatar = ref('');
  const selectedKeys = ref<string[]>([]);
  const collapsed = ref<boolean>(false);
  const isMobile = ref<boolean>(false);
  const currentTime = ref(dayjs().format('YYYY年 MM月 DD日 HH:mm'));

  const router = useRouter();
  const route = useRoute();

  let timer: any = null;

  const updateTime = () => {
    currentTime.value = dayjs().format('YYYY年 MM月 DD日 HH:mm');
  };

  const updateLayoutMode = () => {
    isMobile.value = window.innerWidth < 768;
    if (isMobile.value) {
      collapsed.value = true;
    }
  };

  const getUserAvatar = () => {
    const userId = userStore.admin_user_id;
    if (!userId) return;

    detailApi({ userId: userId })
      .then((res) => {
        const userData = res.data;
        if (userData && userData.avatar) {
          userAvatar.value = BASE_URL + '/api/staticfiles/avatar/' + userData.avatar;
        }
      })
      .catch((err) => console.log('获取头像失败:', err));
  };

  const handleClick = ({ key }: { key: string }) => {
    router.push({ name: key });
    if (isMobile.value) {
      collapsed.value = true;
    }
  };

  const handlePreview = () => {
    router.push({ name: 'index' });
  };

  const handleProfileClick = () => {
    router.push({ name: 'profile' });
  };

  const handleLogout = () => {
    userStore.adminLogout().then(() => {
      router.push({ name: 'adminLogin' });
    });
  };

  onMounted(() => {
    selectedKeys.value = [route.name as string];
    updateLayoutMode();
    getUserAvatar();
    timer = setInterval(updateTime, 1000 * 60);

    window.addEventListener('avatar-updated', getUserAvatar);
    window.addEventListener('resize', updateLayoutMode);
  });

  onUnmounted(() => {
    if (timer) clearInterval(timer);
    window.removeEventListener('avatar-updated', getUserAvatar);
    window.removeEventListener('resize', updateLayoutMode);
  });
</script>

<style scoped lang="less">
  .admin-layout {
    min-height: 100vh;
    background-color: #f4f7fe;
  }

  .admin-sider {
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.02);
    z-index: 10;
    display: flex;
    flex-direction: column;
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
    background: #fff;

    :deep(.ant-layout-sider-children) {
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
  }

  .sider-mask {
    position: fixed;
    inset: 0;
    z-index: 9;
    background: rgba(11, 18, 32, 0.42);
  }

  .logo-wrapper {
    padding: 18px 22px 14px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;

    .logo-icon {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        display: block;
      }

      svg {
        width: 100%;
        height: 100%;
        display: block;
      }
    }

    .logo-text {
      font-size: 20px;
      font-weight: 800;
      color: #1b2559;
      letter-spacing: -0.5px;
    }
  }

  .admin-menu {
    border-right: none;
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 4px 0 8px;

    &::-webkit-scrollbar {
      width: 0;
      height: 0;
    }

    :deep(.ant-menu-item) {
      height: 43px;
      line-height: 43px;
      margin: 2px 14px !important;
      width: calc(100% - 28px) !important;
      border-radius: 10px;
      color: #a3aed0;
      font-weight: 600;

      .anticon {
        font-size: 16px;
      }

      &:hover {
        color: #4318ff;
        background: #f4f7fe;
      }

      &.ant-menu-item-selected {
        background: #4318ff !important;
        color: #fff !important;
        box-shadow: 0px 10px 20px rgba(67, 24, 255, 0.2);
      }
    }

    :deep(.ant-menu-submenu-title) {
      height: 43px;
      line-height: 43px;
      margin: 2px 14px !important;
      width: calc(100% - 28px) !important;
      border-radius: 10px;
      color: #a3aed0;
      font-weight: 600;

      &:hover {
        color: #4318ff;
      }
    }

    :deep(.ant-menu-divider) {
      margin: 6px 22px !important;
    }

    :deep(.ant-menu-sub.ant-menu-inline) {
      background: transparent;
    }
  }

  .sider-footer {
    padding: 12px 18px 16px;
    border-top: 1px solid #f4f7fe;
    flex-shrink: 0;

    .user-menu-item {
      display: flex;
      align-items: center;
      gap: 12px;
      min-height: 38px;
      padding: 9px 10px;
      border-radius: 10px;
      color: #a3aed0;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;

      .anticon {
        font-size: 16px;
      }

      &:hover {
        background: #f4f7fe;
        color: #1b2559;
      }

      &.logout:hover {
        color: #ff4d4f;
        background: #fff1f0;
      }
    }
  }

  .main-container {
    margin-left: 260px;
    transition: all 0.2s;
  }

  .admin-layout :deep(.ant-layout-sider-collapsed) + .main-container {
    margin-left: 80px;
  }

  .admin-header {
    background: transparent;
    padding: 24px 40px;
    height: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header-left {
      display: flex;
      align-items: center;
      min-width: 0;
      gap: 16px;

      .sider-toggle {
        width: 40px;
        height: 40px;
        border: none;
        border-radius: 12px;
        background: #fff;
        color: #1b2559;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 0 8px 20px rgba(112, 144, 176, 0.12);
        transition: color 0.2s, transform 0.2s;

        &:hover {
          color: #4318ff;
          transform: translateY(-1px);
        }
      }

      .welcome-text {
        min-width: 0;

        h2 {
          font-size: 24px;
          font-weight: 800;
          color: #1b2559;
          margin: 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        p {
          color: #a3aed0;
          margin: 4px 0 0;
          font-weight: 500;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      min-width: 0;

      .header-icons {
        display: flex;
        align-items: center;
        gap: 20px;

        .icon-btn {
          font-size: 20px;
          color: #a3aed0;
          cursor: pointer;
          transition: color 0.3s;
          display: flex;
          align-items: center;

          &:hover {
            color: #1b2559;
          }

          :deep(.ant-badge-dot) {
            width: 8px;
            height: 8px;
            box-shadow: 0 0 0 2px #fff;
          }
        }
      }

      .header-divider {
        height: 24px;
        border-color: #f4f7fe;
        margin: 0 8px;
      }

      .search-bar {
        display: flex;
        align-items: center;
        background: #fff;
        padding: 8px 16px;
        border-radius: 12px;
        width: 280px;
        border: 1px solid #f4f7fe;
        transition: all 0.3s;

        &:focus-within {
          border-color: #4318ff;
          box-shadow: 0 0 0 2px rgba(67, 24, 255, 0.05);
        }

        .search-icon {
          color: #1b2559;
          margin-right: 10px;
          font-size: 16px;
        }

        input {
          border: none;
          background: transparent;
          outline: none;
          width: 100%;
          font-size: 14px;
          font-weight: 500;
          color: #1b2559;

          &::placeholder {
            color: #a3aed0;
          }
        }
      }

      .user-avatar-wrapper {
        cursor: pointer;
        margin-left: 8px;

        .avatar-img {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          object-fit: cover;
          border: 2px solid #fff;
          box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
        }

        .avatar-placeholder {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: #f4f7fe;
          color: #4318ff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
        }
      }
    }
  }

  .admin-content {
    padding: 0 40px 40px;
  }

  @media screen and (max-width: 1200px) {
    .admin-header {
      padding: 22px 28px;

      .header-right .search-bar {
        width: 220px;
      }
    }

    .admin-content {
      padding: 0 28px 32px;
    }
  }

  @media screen and (max-width: 768px) {
    .admin-sider {
      box-shadow: 12px 0 28px rgba(15, 23, 42, 0.18);
    }

    .main-container,
    .admin-layout :deep(.ant-layout-sider-collapsed) + .main-container {
      margin-left: 0;
    }

    .admin-header {
      position: sticky;
      top: 0;
      z-index: 8;
      background: rgba(244, 247, 254, 0.96);
      backdrop-filter: blur(12px);
      padding: 14px 16px;
      gap: 12px;

      .header-left {
        flex: 1;

        .welcome-text {
          h2 {
            font-size: 18px;
          }

          p {
            font-size: 12px;
          }
        }
      }

      .header-right {
        flex-shrink: 0;
        gap: 10px;

        .header-divider,
        .search-bar {
          display: none;
        }

        .user-avatar-wrapper {
          margin-left: 0;
        }
      }
    }

    .admin-content {
      padding: 0 16px 24px;
    }
  }

  @media screen and (max-width: 480px) {
    .admin-header .header-left .welcome-text p {
      display: none;
    }

    .admin-menu {
      :deep(.ant-menu-item),
      :deep(.ant-menu-submenu-title) {
        margin-left: 12px !important;
        margin-right: 12px !important;
        width: calc(100% - 24px) !important;
      }
    }
  }

  /* 过渡动画 */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
</style>
