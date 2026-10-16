<template>
  <aside class="mine-infos-view">
    <section class="profile-card">
      <img v-if="userAvatar" :src="userAvatar" class="avatar-img" />
      <img v-else :src="AvatarImg" class="avatar-img" />

      <div class="profile-meta">
        <h2 class="nick">{{ nickName || userStore.user_name || defaultNameText }}</h2>
        <span>&#x6B22;&#x8FCE;&#x56DE;&#x6765;</span>
      </div>
    </section>

    <section class="counts">
      <button type="button" class="count-item" @click="clickMenu('collectThingView')">
        <span>&#x6536;&#x85CF;</span>
        <strong>{{ collectCount }}</strong>
      </button>
      <span class="split-line"></span>
      <button type="button" class="count-item" @click="clickMenu('wishThingView')">
        <span>&#x8D2D;&#x7269;&#x8F66;</span>
        <strong>{{ cartCount }}</strong>
      </button>
    </section>

    <nav class="menu-groups">
      <div class="menu-group">
        <div class="group-title">&#x8BA2;&#x5355;&#x4E2D;&#x5FC3;</div>
        <button
          v-for="item in orderMenus"
          :key="item.name"
          type="button"
          class="mine-item"
          :class="{ active: route.name === item.name }"
          @click="clickMenu(item.name)"
        >
          <img :src="item.icon" />
          <span v-html="item.label"></span>
        </button>
      </div>

      <div class="menu-group">
        <div class="group-title">&#x4E2A;&#x4EBA;&#x8BBE;&#x7F6E;</div>
        <button
          v-for="item in settingMenus"
          :key="item.name"
          type="button"
          class="mine-item"
          :class="{ active: route.name === item.name }"
          @click="clickMenu(item.name)"
        >
          <img :src="item.icon" />
          <span v-html="item.label"></span>
        </button>
      </div>
    </nav>
  </aside>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import AvatarImg from '/@/assets/images/avatar.jpg';
  import MyOrderImg from '/@/assets/images/order-icon.svg';
  import CommentIconImg from '/@/assets/images/order-thing-icon.svg';
  import AddressIconImage from '/@/assets/images/order-address-icon.svg';
  import SettingIconImage from '/@/assets/images/setting-icon.svg';
  import SafeIconImage from '/@/assets/images/setting-safe-icon.svg';
  import PushIconImage from '/@/assets/images/setting-push-icon.svg';
  import MessageIconImage from '/@/assets/images/setting-msg-icon.svg';
  import CouponIconImg from '/@/assets/images/coupon-icon.svg';
  import { userCollectListApi } from '/@/api/thingCollect';
  import { detailApi } from '/@/api/user';
  import { BASE_URL } from '/@/store/constants';
  import { useUserStore } from '/@/store';

  const userStore = useUserStore();
  const router = useRouter();
  const route = useRoute();

  const collectCount = ref(0);
  const cartCount = ref(0);
  const userAvatar = ref('');
  const nickName = ref('');
  const defaultNameText = '\u7528\u6237';

  const orderMenus = [
    { name: 'orderView', label: '&#x6211;&#x7684;&#x8BA2;&#x5355;', icon: MyOrderImg },
    { name: 'userCouponsView', label: '&#x6211;&#x7684;&#x4F18;&#x60E0;&#x5238;', icon: CouponIconImg },
    { name: 'commentView', label: '&#x6211;&#x7684;&#x8BC4;&#x8BBA;', icon: CommentIconImg },
    { name: 'addressView', label: '&#x5730;&#x5740;&#x7BA1;&#x7406;', icon: AddressIconImage },
  ];

  const settingMenus = [
    { name: 'userInfoEditView', label: '&#x7F16;&#x8F91;&#x8D44;&#x6599;', icon: SettingIconImage },
    { name: 'securityView', label: '&#x8D26;&#x53F7;&#x5B89;&#x5168;', icon: SafeIconImage },
    { name: 'pushView', label: '&#x63A8;&#x9001;&#x8BBE;&#x7F6E;', icon: PushIconImage },
    { name: 'messageView', label: '&#x6D88;&#x606F;&#x7BA1;&#x7406;', icon: MessageIconImage },
  ];

  onMounted(() => {
    getCollectThingList();
    getCartItemCount();
    getUserInfo();
  });

  const clickMenu = (name: string) => {
    router.push({ name });
  };

  const getUserInfo = () => {
    const userId = userStore.user_id;
    if (!userId) return;

    detailApi({ userId })
      .then((res) => {
        if (res.data) {
          userAvatar.value = res.data.avatar ? `${BASE_URL}/api/staticfiles/avatar/${res.data.avatar}` : '';
          nickName.value = res.data.nickname || userStore.user_name || '';
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const getCollectThingList = () => {
    const userId = userStore.user_id;
    if (!userId) return;

    userCollectListApi({ userId })
      .then((res) => {
        collectCount.value = Array.isArray(res.data) ? res.data.length : 0;
      })
      .catch((err) => {
        console.log(err.msg || err);
      });
  };

  const getCartItemCount = () => {
    try {
      const storedCart = localStorage.getItem('cart');
      cartCount.value = storedCart ? JSON.parse(storedCart).length : 0;
    } catch (error) {
      cartCount.value = 0;
    }
  };
</script>

<style scoped lang="less">
  .mine-infos-view {
    width: 280px;
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 22px;
    overflow: hidden;
    box-shadow: 0 22px 60px rgba(16, 24, 40, 0.07);
  }

  .profile-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 24px;
    border-bottom: 0;
    background:
      linear-gradient(135deg, rgba(16, 24, 40, 0.96), rgba(16, 24, 40, 0.78)),
      linear-gradient(135deg, #222831, #4b5563);
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      right: -34px;
      bottom: -42px;
      width: 118px;
      height: 118px;
      border-radius: 50%;
      background: rgba(255, 209, 0, 0.22);
    }
  }

  .avatar-img {
    width: 66px;
    height: 66px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid rgba(255, 255, 255, 0.72);
    box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
    position: relative;
    z-index: 1;
  }

  .profile-meta {
    min-width: 0;
    position: relative;
    z-index: 1;
  }

  .nick {
    margin: 0;
    color: #fff;
    font-size: 18px;
    line-height: 24px;
    font-weight: 900;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .profile-meta span {
    display: block;
    margin-top: 5px;
    color: rgba(255, 255, 255, 0.68);
    font-size: 13px;
  }

  .counts {
    display: flex;
    align-items: center;
    margin: 18px;
    padding: 12px 0;
    background: #fff8d7;
    border: 1px solid #ffeaa3;
    border-radius: 16px;
  }

  .count-item {
    flex: 1;
    display: grid;
    gap: 4px;
    border: 0;
    background: transparent;
    color: #6b7280;
    cursor: pointer;
  }

  .count-item strong {
    color: #222831;
    font-size: 19px;
    font-weight: 900;
  }

  .split-line {
    width: 1px;
    height: 32px;
    background: #e2e8f0;
  }

  .menu-groups {
    padding: 2px 14px 20px;
  }

  .menu-group + .menu-group {
    margin-top: 18px;
    padding-top: 16px;
    border-top: 1px solid #edf0f4;
  }

  .group-title {
    padding: 0 10px 8px;
    color: #98a2b3;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.04em;
  }

  .mine-item {
    width: 100%;
    height: 42px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0 10px;
    border: 0;
    border-radius: 13px;
    background: transparent;
    color: #344054;
    font-size: 14px;
    text-align: left;
    cursor: pointer;
    transition: background-color 0.2s ease, color 0.2s ease;
  }

  .mine-item:hover {
    background: #f7f8fa;
  }

  .mine-item.active {
    background: #222831;
    color: #fff;
    box-shadow: 0 12px 24px rgba(16, 24, 40, 0.16);
  }

  .mine-item img {
    width: 18px;
    height: 18px;
  }

  .mine-item.active img {
    filter: brightness(0) invert(1);
  }

  @media (max-width: 900px) {
    .mine-infos-view {
      width: 100%;
    }

    .menu-groups {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px;
    }

    .menu-group + .menu-group {
      margin-top: 0;
      padding-top: 0;
      border-top: 0;
    }
  }

  @media (max-width: 560px) {
    .menu-groups {
      grid-template-columns: 1fr;
    }
  }
</style>
