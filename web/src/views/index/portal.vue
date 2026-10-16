<template>
  <div class="portal-container">
    <Header />
    <div class="portal-content">
      <!-- 左侧探索侧边栏 -->
      <aside class="explore-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">探索 <small>EXPLORE</small></span>
        </div>
        <nav class="category-nav">
          <div
            v-for="item in categoryData"
            :key="item.id"
            class="category-item"
            :class="{ active: selectedCategoryId === item.id }"
            @click="selectCategory(item)"
          >
            <div class="category-icon">
              <component :is="getCategoryIcon(item.title)" />
            </div>
            <span class="category-name">{{ item.title }}</span>
          </div>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <Banner :categoryId="selectedCategoryId" />
      </main>
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import Header from './components/header.vue';
  import Banner from './components/banner.vue';
  import { listApi as listClassificationList } from '/@/api/classification';
  import {
    AppstoreOutlined,
    ShoppingOutlined,
    LaptopOutlined,
    HomeOutlined,
    ReadOutlined,
    CoffeeOutlined,
    SkinOutlined,
    GiftOutlined,
  } from '@ant-design/icons-vue';

  const categoryData = ref([]);
  const selectedCategoryId = ref('-1');

  onMounted(() => {
    getCategories();
  });

  const getCategories = () => {
    listClassificationList()
      .then((res) => {
        categoryData.value = [{ id: '-1', title: 'All Products' }, ...res.data];
      })
      .catch((err) => {
        console.error('获取分类失败:', err);
      });
  };

  const selectCategory = (category) => {
    if (selectedCategoryId.value === category.id) return;
    selectedCategoryId.value = category.id;
  };

  const getCategoryIcon = (title) => {
    if (title === 'All Products' || title === '全部') return AppstoreOutlined;
    if (title.includes('手机') || title.includes('数码')) return LaptopOutlined;
    if (title.includes('服饰') || title.includes('女装') || title.includes('男装')) return SkinOutlined;
    if (title.includes('家居') || title.includes('家具')) return HomeOutlined;
    if (title.includes('图书') || title.includes('教育')) return ReadOutlined;
    if (title.includes('食品') || title.includes('零食')) return CoffeeOutlined;
    if (title.includes('礼品')) return GiftOutlined;
    return ShoppingOutlined;
  };
</script>

<style scoped lang="less">
  .portal-container {
    background-color: #f8f9fa;
    min-height: 100vh;
    padding-top: 80px; // 为固定定位的 Header 留出空间
  }

  .portal-content {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    gap: 30px;
    padding: 20px 40px;
  }

  .explore-sidebar {
    width: 240px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 24px;
    padding: 24px;
    height: fit-content;
    position: sticky;
    top: 100px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

    .sidebar-header {
      margin-bottom: 24px;
      padding-left: 8px;

      .sidebar-title {
        font-size: 20px;
        font-weight: 700;
        color: #333;
        display: flex;
        flex-direction: column;

        small {
          font-size: 10px;
          opacity: 0.5;
          text-transform: uppercase;
          margin-top: -2px;
          letter-spacing: 0.5px;
        }
      }
    }

    .category-nav {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .category-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        border-radius: 16px;
        cursor: pointer;
        transition: background-color 180ms ease, color 180ms ease, transform 180ms ease, box-shadow 180ms ease;
        color: #666;

        .category-icon {
          font-size: 18px;
          display: flex;
          align-items: center;
        }

        .category-name {
          font-size: 15px;
          font-weight: 500;
        }

        &:hover {
          background-color: #f5f5f5;
          color: #333;
          transform: translateX(3px);
        }

        &.active {
          background-color: #ffd100;
          color: #333;
          box-shadow: 0 10px 22px rgba(255, 209, 0, 0.18);

          .category-icon {
            color: #333;
          }
        }
      }
    }
  }

  .main-content {
    flex: 1;
    min-width: 0; // 防止 flex 项目溢出
    padding-bottom: 60px;
  }

  // 响应式处理
  @media (max-width: 1200px) {
    .explore-sidebar {
      display: none; // 在小屏幕上隐藏侧边栏，或者改为抽屉
    }

    .portal-content {
      padding: 20px;
    }
  }
</style>
