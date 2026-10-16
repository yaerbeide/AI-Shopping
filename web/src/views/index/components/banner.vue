<template>
  <div class="content-wrapper">
    <!-- 顶部轮播与公告区 -->
    <div class="top-section">
      <div class="carousel-wrapper">
        <a-carousel v-if="bannerList.length > 0" autoplay class="custom-carousel">
          <div v-for="(banner, index) in bannerList" :key="index" class="carousel-slide" @click="handleBannerClick(banner)">
            <img :src="banner.image" :alt="banner.title" />
            <div class="banner-info" v-if="banner.title">
              <h3>{{ banner.title }}</h3>
            </div>
          </div>
        </a-carousel>
        <!-- 加载占位 -->
        <div v-else class="carousel-placeholder"></div>
      </div>

      <!-- 精简版公告与用户入口 -->
      <div class="side-panel">
        <div class="user-card">
          <div class="user-info">
            <img :src="userAvatar || AvatarIcon" class="avatar" />
            <div class="welcome">
              <span class="greeting">Hi, {{ nickName || '访客' }}</span>
              <span class="sub">欢迎来到 CAPĒD</span>
            </div>
          </div>
          <div class="quick-actions">
            <button @click="goToUserCenter('orderView')" class="action-btn">
              <shopping-outlined />
              <span>订单 <small>ORDERS</small></span>
            </button>
            <button @click="goToUserCenter('cartView')" class="action-btn">
              <shopping-cart-outlined />
              <span>购物车 <small>CART</small></span>
            </button>
          </div>
        </div>

        <div class="notice-card">
          <div class="card-header">
            <sound-outlined />
            <span>最新公告 <small>LATEST NEWS</small></span>
          </div>
          <div class="notice-content">
            <marquee scrollamount="3">{{ announcement }}</marquee>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门标签 -->
    <div class="tags-section">
      <div class="section-header">
        <h2 class="title">热门搜索 <small>TRENDING TAGS</small></h2>
      </div>
      <div class="tag-list">
        <span
          v-for="item in tagData"
          :key="item.id"
          class="tag-item"
          :class="{ active: selectedTagId === item.id }"
          @click="clickTag(item.id)"
        >
          {{ item.title }}
        </span>
      </div>
    </div>

    <!-- 商品列表展示区 -->
    <div class="products-section">
      <div class="section-header">
        <h2 class="title">{{ currentCategoryName }}</h2>
        <div class="actions">
          <a-button type="text" @click="refreshProducts" class="refresh-btn">
            <template #icon><reload-outlined /></template>
            换一批 <small>REFRESH</small>
          </a-button>
        </div>
      </div>

      <transition-group name="product-soft" tag="div" class="product-grid" v-if="displayProducts.length > 0">
        <div v-for="(product, index) in displayProducts" :key="product.id" class="product-card" @click="handleDetail(product)">
          <div class="image-wrapper">
            <img :src="product.cover" :alt="product.title" loading="lazy" />
            <div class="hover-mask">
              <span class="view-detail">查看详情 <small>QUICK VIEW</small></span>
            </div>
            <div class="hot-badge" v-if="index < 3 && categoryId === '-1'">HOT</div>
          </div>
          <div class="info-wrapper">
            <h3 class="product-title">{{ product.title }}</h3>
            <div class="product-footer">
              <div class="price-box">
                <span class="currency">¥</span>
                <span class="price">{{ product.price }}</span>
              </div>
              <div class="add-cart">
                <plus-outlined />
              </div>
            </div>
          </div>
        </div>
      </transition-group>

      <!-- 无数据展示 -->
      <div v-else class="empty-state">
        <a-empty description="暂无商品 NO PRODUCTS" />
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, computed, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { useUserStore } from '/@/store';
  import { listApi as listThingList } from '/@/api/thing';
  import { listApi as listTagList } from '/@/api/tag';
  import { listApi as listClassificationList } from '/@/api/classification';
  import { BASE_URL } from '/@/store/constants';
  import { detailApi } from '/@/api/user';
  import { listApi as listNoticeApi } from '/@/api/notice';
  import { listApi as listBannerApi } from '/@/api/banner';
  import AvatarIcon from '/@/assets/images/avatar.jpg';
  import { ShoppingOutlined, ShoppingCartOutlined, SoundOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    categoryId: {
      type: String,
      default: '-1',
    },
  });

  const router = useRouter();
  const userStore = useUserStore();

  const bannerList = ref([]);
  const tagData = ref([]);
  const selectedTagId = ref(-1);
  const allProducts = ref([]);
  const displayProducts = ref([]);
  const categories = ref([]);
  const announcement = ref('正在获取最新公告...');
  const userAvatar = ref('');
  const nickName = ref('');

  const currentCategoryName = computed(() => {
    if (props.categoryId === '-1') return '为您推荐 FEATURED FOR YOU';
    const cat = categories.value.find((c) => c.id === props.categoryId);
    return cat ? `${cat.title} PRODUCTS` : '商品列表 PRODUCTS';
  });

  onMounted(() => {
    getBanners();
    getTags();
    getProducts();
    getUserInfo();
    getNotices();
    getCategories();
  });

  // 监听分类变化
  watch(
    () => props.categoryId,
    (newId) => {
      filterProducts(newId);
    },
  );

  const getCategories = () => {
    listClassificationList().then((res) => {
      categories.value = res.data;
    });
  };

  const getBanners = () => {
    listBannerApi()
      .then((res) => {
        if (res.data && res.data.length > 0) {
          bannerList.value = res.data.map((item) => ({
            title: '',
            image: BASE_URL + '/api/staticfiles/banner/' + item.image,
            thingId: item.thingId,
          }));
        } else {
          bannerList.value = [
            { image: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1600&q=80' },
            { image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1600&q=80' },
          ];
        }
      })
      .catch(() => {
        bannerList.value = [{ image: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1600&q=80' }];
      });
  };

  const getProducts = () => {
    listThingList({}).then((res) => {
      allProducts.value = res.data.map((item) => {
        if (item.cover) {
          item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover;
        }
        return item;
      });
      filterProducts(props.categoryId);
    });
  };

  const filterProducts = (catId) => {
    let filtered = allProducts.value;
    if (catId !== '-1') {
      filtered = allProducts.value.filter((p) => p.classificationId === catId);
    }
    // 随机排序并取前 8 个
    displayProducts.value = [...filtered].sort(() => Math.random() - 0.5).slice(0, 8);
  };

  const refreshProducts = () => {
    filterProducts(props.categoryId);
  };

  const getTags = () => {
    listTagList().then((res) => {
      tagData.value = res.data.slice(0, 10);
    });
  };

  const clickTag = (tagId) => {
    selectedTagId.value = tagId === selectedTagId.value ? -1 : tagId;
    router.push({ name: 'search', query: { tag: tagId } });
  };

  const getNotices = () => {
    listNoticeApi().then((res) => {
      if (res.data && res.data.length > 0) {
        const latest = res.data[0];
        announcement.value = latest.title + ': ' + latest.content;
      } else {
        announcement.value = 'Welcome to Capēd Lifestyle Store';
      }
    });
  };

  const getUserInfo = () => {
    let userId = userStore.user_id;
    if (!userId) return;
    detailApi({ userId }).then((res) => {
      if (res.data) {
        userAvatar.value = BASE_URL + '/api/staticfiles/avatar/' + res.data.avatar;
        nickName.value = res.data.nickname;
      }
    });
  };

  const handleDetail = (product) => {
    router.push({ name: 'detail', query: { id: product.id } });
  };

  const handleBannerClick = (banner) => {
    if (banner.thingId) {
      router.push({ name: 'detail', query: { id: banner.thingId } });
    }
  };

  const goToUserCenter = (view) => {
    router.push({ name: view });
  };
</script>

<style scoped lang="less">
  .content-wrapper {
    display: flex;
    flex-direction: column;
    gap: 40px;

    small {
      font-size: 10px;
      opacity: 0.5;
      text-transform: uppercase;
      font-weight: 600;
      letter-spacing: 0.5px;
      margin-left: 4px;
    }
  }

  .top-section {
    display: flex;
    gap: 24px;
    height: 400px;
  }

  .carousel-wrapper {
    flex: 1;
    border-radius: 32px;
    overflow: hidden;
    background: #eee;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
    height: 100%;

    .custom-carousel {
      height: 100%;
      :deep(.slick-slider) {
        height: 100%;
      }
      :deep(.slick-list) {
        height: 100%;
      }
      :deep(.slick-track) {
        height: 100%;
      }
    }

    .carousel-slide {
      height: 400px;
      cursor: pointer;
      position: relative;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .banner-info {
        position: absolute;
        bottom: 40px;
        left: 40px;
        color: #fff;
        text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
        h3 {
          font-size: 32px;
          font-weight: 700;
          margin: 0;
        }
      }
    }

    .carousel-placeholder {
      height: 100%;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
    }
  }

  .side-panel {
    width: 300px;
    display: flex;
    flex-direction: column;
    gap: 20px;

    .user-card {
      background: #fff;
      border-radius: 24px;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 20px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .avatar {
          width: 54px;
          height: 54px;
          border-radius: 50%;
          object-fit: cover;
          border: 2px solid #fff;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .welcome {
          display: flex;
          flex-direction: column;
          .greeting {
            font-weight: 700;
            font-size: 16px;
            color: #333;
          }
          .sub {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .quick-actions {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .action-btn {
          height: 44px;
          border: none;
          background: #f8f9fa;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          font-size: 14px;
          font-weight: 600;
          color: #333;
          cursor: pointer;
          transition: all 0.2s;

          span {
            display: flex;
            flex-direction: column;
            align-items: center;
            line-height: 1.1;

            small {
              margin-left: 0;
            }
          }

          &:hover {
            background: #ffd100;
          }
        }
      }
    }

    .notice-card {
      background: #333;
      border-radius: 24px;
      padding: 20px;
      color: #fff;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        font-weight: 600;
        opacity: 0.8;

        span {
          display: flex;
          align-items: baseline;
          gap: 6px;

          small {
            margin-left: 0;
            color: #fff;
          }
        }
      }

      .notice-content {
        font-size: 14px;
        line-height: 1.6;
        overflow: hidden;
        marquee {
          height: 100%;
        }
      }
    }
  }

  .tags-section {
    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-top: 16px;

      .tag-item {
        padding: 8px 20px;
        background: #fff;
        border-radius: 100px;
        font-size: 14px;
        color: #666;
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid transparent;

        &:hover {
          background: #f0f0f0;
          color: #333;
        }

        &.active {
          background: #333;
          color: #fff;
        }
      }
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;

    .title {
      font-size: 24px;
      font-weight: 700;
      color: #333;
      margin: 0;
      display: flex;
      align-items: baseline;
      gap: 8px;

      small {
        margin-left: 0;
      }
    }

    .refresh-btn {
      color: #666;
      font-weight: 500;
      &:hover {
        color: #ffd100;
      }
    }
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 24px;
  }

  .product-card {
    background: #fff;
    border-radius: 24px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);

      .image-wrapper .hover-mask {
        opacity: 1;
      }

      .add-cart {
        background: #333;
        color: #fff;
      }
    }

    .image-wrapper {
      aspect-ratio: 1;
      position: relative;
      overflow: hidden;
      background: #f8f9fa;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
      }

      .hover-mask {
        position: absolute;
        inset: 0;
        background: rgba(255, 209, 0, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;

        .view-detail {
          background: #fff;
          padding: 8px 20px;
          border-radius: 100px;
          font-weight: 600;
          font-size: 13px;
          box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        }
      }

      .hot-badge {
        position: absolute;
        top: 16px;
        left: 16px;
        background: #ff4d4f;
        color: #fff;
        padding: 4px 10px;
        border-radius: 8px;
        font-size: 11px;
        font-weight: 800;
      }
    }

    .info-wrapper {
      padding: 20px;

      .product-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0 0 12px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .price-box {
          .currency {
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-right: 2px;
          }
          .price {
            font-size: 20px;
            font-weight: 700;
            color: #333;
          }
        }

        .add-cart {
          width: 36px;
          height: 36px;
          border-radius: 12px;
          background: #f5f5f5;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.2s;
          font-size: 18px;
        }
      }
    }
  }

  @keyframes loading {
    0% {
      background-position: 200% 0;
    }
    100% {
      background-position: -200% 0;
    }
  }

  @media (max-width: 768px) {
    .top-section {
      flex-direction: column;
      height: auto;
    }
    .side-panel {
      width: 100%;
    }
    .carousel-wrapper {
      height: 300px;
    }
  }
  .tag-item,
  .refresh-btn,
  .action-btn,
  .product-card,
  .notice-card,
  .user-card {
    transition: background-color 180ms ease, color 180ms ease, border-color 180ms ease, box-shadow 180ms ease,
      transform 180ms ease, opacity 180ms ease;
  }

  .product-grid {
    position: relative;
  }

  .tag-item {
    will-change: transform;
  }

  .tag-item:hover {
    transform: translateY(-2px);
  }

  .tag-item.active {
    box-shadow: 0 10px 22px rgba(16, 24, 40, 0.1);
  }

  .refresh-btn:hover {
    transform: translateY(-1px);
  }

  .product-soft-enter-active,
  .product-soft-leave-active {
    transition: opacity 180ms ease, transform 180ms ease;
  }

  .product-soft-move {
    transition: transform 180ms ease;
  }

  .product-soft-enter-from,
  .product-soft-leave-to {
    opacity: 0;
    transform: translateY(6px);
  }

  .product-soft-leave-active {
    position: absolute;
  }

  @media (prefers-reduced-motion: reduce) {
    .tag-item,
    .refresh-btn,
    .action-btn,
    .product-card,
    .notice-card,
    .user-card,
    .product-soft-enter-active,
    .product-soft-leave-active,
    .product-soft-move {
      transition: none !important;
    }
  }
</style>
