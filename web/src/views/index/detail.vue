<template>
  <div class="detail">
    <Header />

    <main class="detail-content">
      <section class="detail-hero">
        <div class="hero-gallery">
          <div class="gallery-frame">
            <img :src="detailData.cover" alt="商品图片" />
          </div>
          <div class="gallery-note">
            <span>官方展示图</span>
            <span>下单前请核对规格与库存</span>
          </div>
        </div>

        <div class="hero-info">
          <div class="crumb-line">
            <span>Caped Select</span>
            <strong>{{ classificationTitle }}</strong>
          </div>
          <div class="thing-state">
            <span class="state">商品状态</span>
            <span>上市销售</span>
          </div>
          <h1 class="thing-name">{{ detailData.title }}</h1>
          <p class="thing-subtitle">{{ productSummary }}</p>

          <div class="price-panel">
            <div>
              <span class="price-label">到手价</span>
              <div class="price-container">
                <span class="a-price-symbol">¥</span>
                <span class="a-price">{{ detailData.price }}</span>
              </div>
            </div>
            <div class="stock-pill" :class="stockStateClass">
              {{ stockStateText }}
            </div>
          </div>

          <div class="meta-grid">
            <div>
              <span>分类</span>
              <strong>{{ classificationTitle }}</strong>
            </div>
            <div>
              <span>库存</span>
              <strong>{{ detailData.repertory || 0 }} 件</strong>
            </div>
            <div>
              <span>已收藏</span>
              <strong>{{ detailData.collectCount || 0 }}</strong>
            </div>
          </div>

          <div class="service-row">
            <span v-for="item in serviceTags" :key="item">{{ item }}</span>
          </div>

          <div class="action-row">
            <button class="buy-btn" @click="handleOrder(detailData)">
              <img :src="AddIcon" />
              <span>立即购买</span>
            </button>
            <button class="cart-btn" @click="addToCart()">加入购物车</button>
          </div>
        </div>

        <aside class="hero-actions">
          <button class="action-item" type="button" @click="addToCart()">
            <img :src="WantIcon" />
            <span>购物车</span>
            <strong>{{ cartItemCount }}</strong>
          </button>
          <button class="action-item" type="button" @click="collect()">
            <img :src="RecommendIcon" />
            <span>收藏</span>
            <strong>{{ detailData.collectCount || 0 }}</strong>
          </button>
          <button class="action-item" type="button" @click="share()">
            <img :src="ShareIcon" />
            <span>分享</span>
            <img :src="WeiboShareIcon" class="share-mark" />
          </button>
        </aside>
      </section>

      <section class="content-layout">
        <div class="main-content">
          <div class="order-view main-tab">
            <div class="tab-container">
              <span
                class="tab"
                :class="selectTabIndex === index ? 'tab-select' : ''"
                v-for="(item, index) in tabData"
                :key="index"
                @click="selectTab(index)"
              >
                {{ item }}
                <span v-if="selectTabIndex === index" class="tab-underline"></span>
              </span>
            </div>
          </div>

          <div class="thing-intro" :class="selectTabIndex === 0 ? '' : 'hide'">
            <div class="section-head">
              <span>DETAILS</span>
              <h2>商品详情</h2>
            </div>
            <div class="highlight-grid">
              <div class="highlight-card" v-for="item in productHighlights" :key="item.title">
                <span>{{ item.label }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.text }}</p>
              </div>
            </div>
            <div class="description-card">
              <h3>商品介绍</h3>
              <p class="text">{{ productDescription }}</p>
            </div>
            <div class="notice-strip">
              <strong>购买提示</strong>
              <span>图片、价格和库存以当前页面展示为准，提交订单前建议再次核对商品信息。</span>
            </div>
          </div>

          <div class="thing-specs" :class="selectTabIndex === 1 ? '' : 'hide'">
            <div class="section-head">
              <span>SPECS</span>
              <h2>规格参数</h2>
            </div>
            <div class="spec-grid">
              <div class="spec-item" v-for="item in productSpecs" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <div class="service-panel">
              <h3>服务说明</h3>
              <div class="service-list">
                <div v-for="item in serviceDescriptions" :key="item.title">
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.text }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="thing-comment" :class="selectTabIndex === 2 ? '' : 'hide'">
            <div class="section-head">
              <span>REVIEWS</span>
              <h2>用户评价</h2>
            </div>
            <div class="publish flex-view">
              <img :src="AvatarIcon" class="mine-img" />
              <input placeholder="说点什么..." class="content-input" ref="commentRef" />
              <button class="send-btn" @click="sendComment()">发送</button>
            </div>
            <div class="tab-view flex-view">
              <div class="count-text">共有{{ commentData.length }}条评论</div>
              <div class="tab-box flex-view" v-if="commentData.length > 0">
                <span :class="sortIndex === 0 ? 'tab-select' : ''" @click="sortCommentList('recent')">最新</span>
                <div class="line"></div>
                <span :class="sortIndex === 1 ? 'tab-select' : ''" @click="sortCommentList('hot')">热门</span>
              </div>
            </div>
            <div class="comments-list">
              <div class="comment-item" v-for="item in commentData" :key="item.id">
                <div class="flex-item flex-view">
                  <img :src="AvatarIcon" class="avator" />
                  <div class="person">
                    <div class="name">{{ item.username }}</div>
                    <div class="time">{{ item.commentTime }}</div>
                  </div>
                  <div class="float-right">
                    <span @click="like(item.id)">推荐</span>
                    <span class="num">{{ item.likeCount }}</span>
                  </div>
                </div>
                <p class="comment-content">{{ item.content }}</p>
              </div>
              <div class="infinite-loading-container" v-if="commentData.length === 0">
                <div class="infinite-status-prompt">
                  <div class="no-results">
                    <p>暂无评论，快来发表第一条评论吧！</p>
                  </div>
                </div>
              </div>
              <div class="infinite-loading-container" v-else>
                <div class="infinite-status-prompt">
                  <div class="no-results">
                    <p>没有更多了</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <aside class="recommend">
          <div class="title">热门推荐</div>
          <div class="things">
            <div class="thing-item" v-for="item in recommendData" :key="item.id" @click="handleDetail(item)">
              <div class="img-view">
                <img :src="item.cover" :alt="item.title" />
              </div>
              <div class="info-view">
                <h3 class="thing-name">{{ item.title.substring(0, 18) }}</h3>
                <span class="price">
                  <span class="a-price-symbol">¥</span>
                  <span class="a-price">{{ item.price }}</span>
                </span>
              </div>
            </div>
          </div>
        </aside>
      </section>
    </main>
  </div>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import Header from '/@/views/index/components/header.vue';
  import AddIcon from '/@/assets/images/add.svg';
  import WantIcon from '/@/assets/images/want-read-hover.svg';
  import RecommendIcon from '/@/assets/images/recommend-hover.svg';
  import ShareIcon from '/@/assets/images/share-icon.svg';
  import WeiboShareIcon from '/@/assets/images/wb-share.svg';
  import AvatarIcon from '/@/assets/images/avatar.jpg';
  import { detailApi, listApi as listThingList } from '/@/api/thing';
  import { listThingCommentsApi, createApi as createCommentApi, likeApi } from '/@/api/comment';
  import { wishApi } from '/@/api/thingWish';
  import { collectApi } from '/@/api/thingCollect';
  import { BASE_URL } from '/@/store/constants';
  import { useRoute, useRouter } from 'vue-router/dist/vue-router';
  import { useUserStore } from '/@/store';
  import { getFormatTime } from '/@/utils';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();

  let thingId = ref('');
  let detailData = ref({});
  let tabData = ref(['详情介绍', '规格参数', '用户评价']);
  let selectTabIndex = ref(0);
  let cartItemCount = ref(0);
  let classificationLoading = ref(false);

  let commentData = ref([]);
  let recommendData = ref([]);
  let sortIndex = ref(0);
  let order = ref('recent'); // 默认排序最新

  let commentRef = ref();

  onMounted(() => {
    thingId.value = route.query.id.trim();
    getThingDetail();
    getRecommendThing();
    getCommentList();
    updateCartItemCount();
  });

  watch(
    () => route.query.id,
    (id) => {
      if (!id || String(id) === String(thingId.value)) return;
      thingId.value = String(id).trim();
      selectTabIndex.value = 0;
      getThingDetail();
      getCommentList();
      updateCartItemCount();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
  );

  const classificationTitle = computed(() => {
    if (detailData.value.classification_title) return detailData.value.classification_title;
    if (detailData.value.classificationId && classificationLoading.value) return '加载中...';
    return '暂无分类';
  });

  const stockAmount = computed(() => Number(detailData.value.repertory || 0));

  const stockStateText = computed(() => {
    if (stockAmount.value > 10) return '现货充足';
    if (stockAmount.value > 0) return '库存紧张';
    return '暂时无货';
  });

  const stockStateClass = computed(() => {
    if (stockAmount.value > 10) return 'full';
    if (stockAmount.value > 0) return 'low';
    return 'none';
  });

  const productDescription = computed(() => {
    if (detailData.value.description) return detailData.value.description;
    return `${detailData.value.title || '该商品'}已在商城上架，适合有相关需求的用户选购。下单前建议核对商品图片、价格、库存和规格信息。`;
  });

  const productSummary = computed(() => {
    const title = detailData.value.title || '这款商品';
    return `${title} 属于${classificationTitle.value}，当前${stockStateText.value}，适合结合预算、用途和库存状态综合选择。`;
  });

  const productHighlights = computed(() => [
    {
      label: '精选卖点',
      title: '清晰展示',
      text: '商品图片、价格、分类和库存集中呈现，减少来回查找信息的成本。',
    },
    {
      label: '库存提示',
      title: stockStateText.value,
      text: stockAmount.value > 0 ? `当前库存 ${stockAmount.value} 件，支持继续下单。` : '当前库存不足，建议先收藏或查看推荐商品。',
    },
    {
      label: '适用分类',
      title: classificationTitle.value,
      text: '后续可接入更细的规格字段，让分类、参数和详情介绍直接联动。',
    },
  ]);

  const productSpecs = computed(() => [
    { label: '商品名称', value: detailData.value.title || '-' },
    { label: '商品分类', value: classificationTitle.value },
    { label: '销售状态', value: '上市销售' },
    { label: '当前库存', value: `${stockAmount.value} 件` },
    { label: '参考价格', value: `¥${detailData.value.price || '-'}` },
    { label: '商品编号', value: detailData.value.id || '-' },
  ]);

  const serviceTags = computed(() => ['现货优先', '下单前可核对', '支持收藏', '购物车同步']);

  const serviceDescriptions = computed(() => [
    { title: '价格说明', text: '当前价格以页面展示为准，提交订单前请再次确认金额。' },
    { title: '库存说明', text: '库存会随购买行为变化，若显示库存紧张，建议尽快完成确认。' },
    { title: '规格说明', text: '当前版本基于商品基础字段展示，后续可扩展颜色、型号、尺寸、售后等结构化规格。' },
  ]);

  const selectTab = (index) => {
    selectTabIndex.value = index;
  };

  const getThingDetail = () => {
    detailApi({ id: thingId.value })
      .then((res) => {
        detailData.value = res.data;
        detailData.value.cover = BASE_URL + '/api/staticfiles/image/' + detailData.value.cover;

        // 如果分类标题不存在，获取分类信息
        if (!detailData.value.classification_title && detailData.value.classificationId) {
          getClassificationInfo(detailData.value.classificationId);
        }

        updateCartItemCount();
        console.log('商品详情数据:', detailData.value);
      })
      .catch((err) => {
        message.error('获取详情失败');
      });
  };

  // 获取分类信息
  const getClassificationInfo = (classificationId) => {
    classificationLoading.value = true;
    import('/@/api/classification')
      .then(({ listApi }) => {
        listApi({})
          .then((res) => {
            if (res.data && res.data.length > 0) {
              const classification = res.data.find((item) => item.id === classificationId);
              if (classification) {
                detailData.value.classification_title = classification.title;
              }
            }
            classificationLoading.value = false;
          })
          .catch((err) => {
            console.error('获取分类信息失败:', err);
            classificationLoading.value = false;
          });
      })
      .catch((err) => {
        console.error('导入分类API失败:', err);
        classificationLoading.value = false;
      });
  };

  const addToCart = () => {
    let userId = userStore.user_id;
    if (userId) {
      // 从本地存储获取购物车数据
      let cartItems = [];
      const storedCart = localStorage.getItem('cart');
      if (storedCart) {
        cartItems = JSON.parse(storedCart);
      }

      // 检查商品是否已在购物车中
      const existingItem = cartItems.find((item) => item.id === detailData.value.id);

      if (existingItem) {
        // 如果商品已存在，增加数量
        existingItem.quantity += 1;
        message.success('商品数量已增加');
      } else {
        // 如果商品不存在，添加到购物车
        const newItem = {
          id: detailData.value.id,
          title: detailData.value.title,
          price: detailData.value.price,
          quantity: 1,
          spec: '默认规格',
          cover: detailData.value.cover,
          checked: true,
        };
        cartItems.push(newItem);
        message.success('商品已添加到购物车');
      }

      // 保存到本地存储
      localStorage.setItem('cart', JSON.stringify(cartItems));
      updateCartItemCount();
    } else {
      message.warn('请先登录');
      router.push({ name: 'login' });
    }
  };
  const collect = () => {
    let userId = userStore.user_id;
    if (userId) {
      // 创建FormData对象，以匹配后端接收方式
      const formData = new FormData();
      formData.append('userId', userId);
      formData.append('thingId', thingId.value);

      collectApi(formData)
        .then((res) => {
          message.success(res.msg);
          getThingDetail();
        })
        .catch((err) => {
          console.error('收藏失败:', err);
          message.error(err.msg || '收藏失败，请稍后再试');
        });
    } else {
      message.warn('请先登录');
      router.push({ name: 'login' });
    }
  };
  const share = () => {
    let content = '分享一个非常好玩的网站 ' + window.location.href;
    let shareHref = 'http://service.weibo.com/share/share.php?title=' + content;
    window.open(shareHref);
  };
  const handleOrder = (detailData) => {
    console.log('立即购买商品信息:', detailData);
    const userId = userStore.user_id;

    // 把商品信息存储到localStorage，便于确认订单页面获取
    const checkoutItems = [
      {
        id: detailData.id,
        title: detailData.title,
        cover: detailData.cover,
        price: detailData.price,
        quantity: 1,
      },
    ];
    localStorage.setItem('checkoutItems', JSON.stringify(checkoutItems));

    router.push({
      name: 'confirm',
      query: {
        id: detailData.id,
        title: detailData.title,
        cover: detailData.cover,
        price: detailData.price,
      },
    });
  };
  const getRecommendThing = () => {
    listThingList({ sort: 'recommend' })
      .then((res) => {
        res.data.forEach((item, index) => {
          if (item.cover) {
            item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover;
          }
        });
        console.log(res);
        recommendData.value = res.data.slice(0, 6);
      })
      .catch((err) => {
        console.log(err);
      });
  };
  const handleDetail = (item) => {
    router.push({ name: 'detail', query: { id: item.id } });
  };
  const sendComment = () => {
    console.log(commentRef.value);
    let text = commentRef.value.value.trim();
    console.log(text);
    if (text.length <= 0) {
      return;
    }
    commentRef.value.value = '';
    let userId = userStore.user_id;
    if (userId) {
      createCommentApi({ content: text, thingId: thingId.value, userId: userId })
        .then((res) => {
          getCommentList();
        })
        .catch((err) => {
          console.log(err);
        });
    } else {
      message.warn('请先登录！');
      router.push({ name: 'login' });
    }
  };
  const like = (commentId) => {
    likeApi({ id: commentId })
      .then((res) => {
        getCommentList();
      })
      .catch((err) => {
        console.log(err);
      });
  };
  const getCommentList = () => {
    listThingCommentsApi({ thingId: thingId.value, order: order.value })
      .then((res) => {
        res.data.forEach((item) => {
          item.commentTime = getFormatTime(item.commentTime, true);
        });
        commentData.value = res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  };
  const sortCommentList = (sortType) => {
    if (sortType === 'recent') {
      sortIndex.value = 0;
    } else {
      sortIndex.value = 1;
    }
    order.value = sortType;
    getCommentList();
  };

  // 更新购物车商品数量
  const updateCartItemCount = () => {
    const storedCart = localStorage.getItem('cart');
    if (storedCart) {
      const cartItems = JSON.parse(storedCart);
      // 找到当前商品在购物车中的数量
      const currentItem = cartItems.find((item) => item.id === detailData.value.id);
      cartItemCount.value = currentItem ? currentItem.quantity : 0;
    } else {
      cartItemCount.value = 0;
    }
  };
</script>
<style scoped lang="less">
  .hide {
    display: none;
  }

  .detail-content {
    display: flex;
    flex-direction: column;
    width: 1100px;
    margin: 40px auto 40px;
  }

  .flex-view {
    display: flex;
  }

  .hidden-lg {
    display: none !important;
  }

  .thing-infos-view {
    display: flex;
    margin: 40px 0;
    overflow: hidden;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    padding: 30px;

    .thing-infos {
      flex: 1;
      display: flex;
    }

    .mobile-share-box {
      height: 38px;
      background: transparent;
      padding: 0 16px;
      margin: 12px 0;
      font-size: 0;
      align-items: center;
      justify-content: space-between;

      .state {
        width: 64px;
        height: 24px;
        line-height: 24px;
        background: rgba(70, 132, 226, 0.1);
        border-radius: 4px;
        font-weight: 500;
        font-size: 12px;
        color: #4684e2;
        text-align: center;
      }

      .share-img {
        background: #fff;
        width: 38px;
        height: 38px;
        border-radius: 50%;
        text-align: center;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        img {
          position: relative;
          top: 4px;
          width: 24px;
        }
      }
    }

    .thing-img-box {
      flex: 0 0 300px;
      margin: 0 40px 0 0;
      position: relative;
      overflow: hidden;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      background: #f8f9fa;
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        max-width: 100%;
        max-height: 100%;
        display: block;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }
      }
    }

    .thing-info-box {
      text-align: left;
      padding: 0;
      margin: 0;
    }

    .thing-state {
      height: 26px;
      line-height: 26px;

      .state {
        font-weight: 500;
        color: #4684e2;
        background: rgba(70, 132, 226, 0.1);
        border-radius: 4px;
        padding: 5px 12px;
        margin-right: 16px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(70, 132, 226, 0.2);
        }
      }

      span {
        font-size: 14px;
        color: #152844;
      }
    }

    .thing-name {
      line-height: 32px;
      margin: 16px 0;
      color: #0f1111;
      font-size: 24px !important;
      font-weight: 600 !important;
      letter-spacing: -0.5px;
    }

    .translators,
    .authors {
      line-height: 18px;
      font-size: 14px;
      margin: 16px 0;
      align-items: flex-start;
      justify-content: flex-start;
      padding: 10px 0;
      border-top: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;

      .name {
        color: #315c9e;
        white-space: normal;
        font-weight: 500;

        &.loading {
          color: #999;
          font-style: italic;
          position: relative;

          &::after {
            content: '...';
            position: absolute;
            animation: ellipsis 1.5s infinite;
          }
        }
      }
    }

    @keyframes ellipsis {
      0% {
        content: '.';
      }
      33% {
        content: '..';
      }
      66% {
        content: '...';
      }
    }

    .tags {
      position: absolute;
      bottom: 20px;
      margin-top: 16px;

      .category-box {
        color: #152844;
        font-size: 14px;

        .title {
          color: #787878;
        }
      }
    }

    .thing-counts {
      flex: 0 0 235px;
      margin-left: 20px;
      border-left: 1px solid #f0f0f0;
      padding-left: 20px;
    }

    .pointer {
      cursor: pointer;
    }

    .count-item {
      height: 64px;
      align-items: center;
      cursor: pointer;
      transition: all 0.2s ease;
      border-radius: 8px;
      padding: 0 10px;

      &:hover {
        background-color: #f5f7fa;
      }
    }

    .count-img {
      flex: 0 0 32px;
      margin-right: 24px;
      font-size: 0;

      img {
        width: 100%;
        display: block;
      }
    }

    .count-box {
      position: relative;
      border-bottom: 1px solid #f0f0f0;
      align-items: center;
      justify-content: space-between;
      flex: 1;
      height: 100%;
    }

    .count-text-box {
      font-size: 0;

      .count-title {
        color: #152844;
        font-weight: 600;
        font-size: 16px;
        line-height: 18px;
        display: block;
        height: 18px;
      }
    }

    .count-num-box {
      font-weight: 600;
      font-size: 20px;
      line-height: 24px;
      color: #152844;
    }
  }

  .buy-btn {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4684e2, #5a6acd);
    border-radius: 6px;
    text-align: center;
    color: #fff;
    font-size: 16px;
    height: 44px;
    width: 160px;
    outline: none;
    border: none;
    margin-top: 24px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(70, 132, 226, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(70, 132, 226, 0.4);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 8px rgba(70, 132, 226, 0.3);
    }

    img {
      width: 16px;
      margin-right: 8px;
      vertical-align: middle;
    }

    span {
      vertical-align: middle;
    }
  }

  .buy-way {
    overflow: hidden;

    .title {
      font-weight: 600;
      font-size: 18px;
      height: 26px;
      line-height: 26px;
      color: #152844;
      margin-bottom: 12px;
    }
  }

  .thing-content-view {
    margin-top: 20px;
    padding-bottom: 50px;
    display: flex;
    gap: 30px;
  }

  .main-content {
    flex: 1;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    padding: 30px;

    .text {
      color: #484848;
      font-size: 16px;
      line-height: 1.8;
      padding: 20px;
      margin: 0;
      white-space: pre-wrap;
      background: #f9f9f9;
      border-radius: 8px;
    }
  }

  .main-tab {
    border-bottom: 1px solid #eaeaea;
    margin-bottom: 20px;
  }

  .order-view {
    position: relative;
    color: #6c6c6c;
    font-size: 14px;
    line-height: 40px;

    .title {
      margin-right: 8px;
    }

    .tab-container {
      display: flex;
      position: relative;
    }

    .tab {
      margin-right: 30px;
      cursor: pointer;
      color: #5f77a6;
      font-size: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      padding: 0 10px;
      position: relative;

      &:hover {
        color: #4684e2;
      }
    }

    .tab-select {
      color: #152844;
      font-weight: 600;
    }

    .tab-underline {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 4px;
      background: #4684e2;
      border-radius: 2px;
    }
  }

  .recommend {
    flex: 0 0 280px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    padding: 20px;
    height: fit-content;

    .title {
      font-weight: 600;
      font-size: 18px;
      line-height: 26px;
      color: #152844;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eaeaea;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        width: 40px;
        height: 3px;
        background: #4684e2;
        border-radius: 2px;
      }
    }

    .things {
      max-height: 500px;
      overflow-y: auto;
      padding-right: 5px;

      /* 自定义滚动条样式 */
      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb:hover {
        background: #a8a8a8;
      }

      .thing-item {
        position: relative;
        width: 100%;
        height: fit-content;
        overflow: hidden;
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-5px);

          img {
            transform: scale(1.05);
          }
        }

        &:last-child {
          border-bottom: none;
          margin-bottom: 0;
        }

        .img-view {
          height: 160px;
          width: 100%;
          border-radius: 8px;
          overflow: hidden;
          background: #f8f9fa;
          display: flex;
          align-items: center;
          justify-content: center;

          img {
            max-height: 160px;
            max-width: 100%;
            overflow: hidden;
            margin: 0 auto;
            object-fit: contain;
            transition: transform 0.3s ease;
          }
        }

        .info-view {
          overflow: hidden;
          padding: 12px 0;

          .thing-name {
            line-height: 1.4;
            margin-top: 8px;
            color: #152844;
            font-size: 15px;
            font-weight: 500;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .price {
            color: #ff7b31;
            font-size: 18px;
            line-height: 20px;
            margin-top: 8px;
            font-weight: 600;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .translators {
            color: #6f6f6f;
            font-size: 12px;
            line-height: 14px;
            margin-top: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }

  .thing-comment {
    .title {
      font-weight: 600;
      font-size: 18px;
      line-height: 22px;
      color: #152844;
      margin: 30px 0 20px;
      position: relative;
      padding-left: 12px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 2px;
        width: 4px;
        height: 18px;
        background: #4684e2;
        border-radius: 2px;
      }
    }

    .publish {
      align-items: center;
      background: #f8f9fb;
      padding: 15px;
      border-radius: 8px;

      .mine-img {
        flex: 0 0 40px;
        margin-right: 12px;
        border-radius: 50%;
        width: 40px;
        height: 40px;
      }

      .content-input {
        flex: 1;
        background: #fff;
        border-radius: 4px;
        height: 40px;
        line-height: 40px;
        color: #484848;
        padding: 5px 15px;
        white-space: nowrap;
        outline: none;
        border: 1px solid #eaeaea;
        transition: all 0.3s ease;

        &:focus {
          border-color: #4684e2;
          box-shadow: 0 0 0 2px rgba(70, 132, 226, 0.2);
        }
      }

      .send-btn {
        margin-left: 10px;
        background: #4684e2;
        border-radius: 4px;
        flex: 0 0 80px;
        color: #fff;
        font-size: 14px;
        text-align: center;
        height: 40px;
        line-height: 40px;
        outline: none;
        border: 0px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #3a75d0;
        }
      }
    }

    .tab-view {
      justify-content: space-between;
      font-size: 14px;
      align-items: center;
      margin: 24px 0;
      padding: 10px 0;
      border-bottom: 1px solid #eaeaea;

      .count-text {
        color: #484848;
        float: left;
        font-weight: 500;
      }

      .tab-box {
        color: #5f77a6;
        align-items: center;

        .tab-select {
          color: #4684e2;
          font-weight: 500;
        }

        span {
          cursor: pointer;
          padding: 4px 12px;
          border-radius: 4px;
          transition: all 0.3s ease;

          &:hover {
            background: rgba(70, 132, 226, 0.1);
          }
        }
      }

      .line {
        width: 1px;
        height: 12px;
        margin: 0 12px;
        background: #eaeaea;
      }
    }
  }

  .comments-list {
    .comment-item {
      .flex-item {
        align-items: center;
        padding-top: 16px;

        .avator {
          flex: 0 0 40px;
          width: 40px;
          height: 40px;
          margin-right: 12px;
          border-radius: 50%;
          cursor: pointer;
        }

        .person {
          flex: 1;
        }

        .name {
          color: #152844;
          font-weight: 600;
          font-size: 14px;
          line-height: 22px;
          height: 22px;
          cursor: pointer;
        }

        .time {
          color: #5f77a6;
          font-size: 12px;
          line-height: 16px;
          height: 16px;
          margin-top: 2px;
        }

        .float-right {
          color: #4684e2;
          font-size: 14px;
          float: right;

          span {
            margin-left: 19px;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
              color: #3a75d0;
              text-decoration: underline;
            }
          }

          .num {
            color: #152844;
            margin-left: 6px;
            cursor: auto;
            text-decoration: none;
          }
        }
      }
    }
  }

  .comment-content {
    margin-top: 8px;
    color: #484848;
    font-size: 14px;
    line-height: 1.6;
    padding: 10px 15px 16px;
    border-bottom: 1px solid #eaeaea;
    margin-left: 52px;
    overflow: hidden;
    word-break: break-word;
    background: #f9f9fb;
    border-radius: 0 8px 8px 8px;
  }

  .infinite-loading-container {
    clear: both;
    text-align: center;
    padding: 20px 0;
  }

  .a-price-symbol {
    top: -0.5em;
    font-size: 14px;
    color: #ff7b31;
  }

  .a-price {
    color: #ff7b31;
    font-size: 28px;
    font-weight: 600;
  }

  .detail {
    min-height: 100vh;
    background:
      linear-gradient(180deg, #f7f8fa 0%, #ffffff 42%),
      radial-gradient(circle at 12% 18%, rgba(255, 209, 0, 0.12), transparent 26%);
  }

  .detail-content {
    width: min(1360px, calc(100% - 48px));
    margin: 112px auto 72px;
    display: block;
  }

  .detail-hero {
    display: grid;
    grid-template-columns: minmax(320px, 420px) minmax(0, 1fr) 230px;
    gap: 28px;
    align-items: stretch;
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 24px;
    padding: 28px;
    box-shadow: 0 24px 70px rgba(16, 24, 40, 0.08);
  }

  .hero-gallery {
    display: grid;
    gap: 14px;
    align-content: start;
  }

  .gallery-frame {
    aspect-ratio: 1 / 1;
    border-radius: 20px;
    background: #f5f6f8;
    border: 1px solid #edf0f4;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      width: 88%;
      height: 88%;
      object-fit: contain;
      transition: transform 0.25s ease;
    }

    &:hover img {
      transform: scale(1.035);
    }
  }

  .gallery-note {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    color: #7a8494;
    font-size: 13px;
    background: #fafafa;
    border-radius: 12px;
    padding: 12px 14px;
  }

  .hero-info {
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .crumb-line {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #8a93a3;
    font-size: 13px;
    margin-bottom: 14px;

    span {
      color: #222831;
      font-weight: 800;
    }

    strong {
      font-weight: 600;
      color: #6f7683;
    }
  }

  .thing-state {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #667085;
    font-size: 14px;

    .state {
      color: #222831;
      background: #ffd100;
      border-radius: 999px;
      padding: 6px 12px;
      font-weight: 700;
    }
  }

  .thing-name {
    margin: 18px 0 10px;
    color: #101828;
    font-size: 32px !important;
    line-height: 1.25;
    font-weight: 800 !important;
    letter-spacing: 0;
  }

  .thing-subtitle {
    margin: 0 0 18px;
    color: #667085;
    font-size: 15px;
    line-height: 1.8;
  }

  .price-panel {
    display: flex;
    justify-content: space-between;
    gap: 18px;
    align-items: center;
    background: #fff8d7;
    border: 1px solid #ffeaa3;
    border-radius: 18px;
    padding: 18px 20px;
    margin-bottom: 18px;
  }

  .price-label {
    display: block;
    color: #8a6f00;
    font-size: 13px;
    font-weight: 700;
    margin-bottom: 2px;
  }

  .price-panel .a-price-symbol {
    color: #ff4d3f;
    font-size: 18px;
    font-weight: 800;
  }

  .price-panel .a-price {
    color: #ff4d3f;
    font-size: 42px;
    font-weight: 900;
  }

  .stock-pill {
    border-radius: 999px;
    padding: 8px 14px;
    font-size: 13px;
    font-weight: 800;
    white-space: nowrap;

    &.full {
      color: #087443;
      background: #dcfae6;
    }

    &.low {
      color: #b54708;
      background: #fff3cd;
    }

    &.none {
      color: #b42318;
      background: #fee4e2;
    }
  }

  .meta-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 16px;

    div {
      min-width: 0;
      background: #f7f8fa;
      border: 1px solid #edf0f4;
      border-radius: 14px;
      padding: 14px;
    }

    span {
      display: block;
      color: #8a93a3;
      font-size: 12px;
      margin-bottom: 7px;
    }

    strong {
      display: block;
      color: #1d2939;
      font-size: 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .service-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin: auto 0 20px;

    span {
      color: #475467;
      background: #fff;
      border: 1px solid #edf0f4;
      border-radius: 999px;
      padding: 7px 12px;
      font-size: 13px;
    }
  }

  .action-row {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .buy-btn,
  .cart-btn {
    min-width: 150px;
    height: 48px;
    border-radius: 999px;
    border: 0;
    cursor: pointer;
    font-size: 16px;
    font-weight: 800;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .buy-btn {
    margin-top: 0;
    width: auto;
    background: #222831;
    color: #fff;
    box-shadow: 0 12px 28px rgba(34, 40, 49, 0.18);

    img {
      width: 16px;
      filter: brightness(0) invert(1);
    }
  }

  .cart-btn {
    background: #ffd100;
    color: #222831;
  }

  .buy-btn:hover,
  .cart-btn:hover {
    transform: translateY(-2px);
  }

  .hero-actions {
    border-left: 1px solid #edf0f4;
    padding-left: 22px;
    display: grid;
    align-content: center;
    gap: 12px;
  }

  .action-item {
    width: 100%;
    min-height: 76px;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    background: #fff;
    display: grid;
    grid-template-columns: 34px minmax(0, 1fr) auto;
    gap: 12px;
    align-items: center;
    padding: 14px;
    cursor: pointer;
    color: #1d2939;
    text-align: left;

    img {
      width: 30px;
    }

    span {
      font-weight: 800;
    }

    strong {
      color: #222831;
      font-size: 22px;
    }

    .share-mark {
      width: 28px;
      justify-self: end;
    }

    &:hover {
      border-color: #ffd100;
      background: #fffdf1;
    }
  }

  .content-layout {
    margin-top: 28px;
    display: grid;
    grid-template-columns: minmax(0, 1fr) 300px;
    gap: 28px;
    align-items: start;
  }

  .main-content,
  .recommend {
    border: 1px solid #edf0f4;
    border-radius: 22px;
    box-shadow: 0 18px 48px rgba(16, 24, 40, 0.06);
  }

  .main-content {
    padding: 28px 28px 34px;
  }

  .section-head {
    margin-bottom: 20px;

    span {
      color: #98a2b3;
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.08em;
    }

    h2 {
      margin: 4px 0 0;
      color: #101828;
      font-size: 24px;
      line-height: 1.3;
    }
  }

  .highlight-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 14px;
    margin-bottom: 18px;
  }

  .highlight-card {
    min-height: 140px;
    border-radius: 18px;
    background: #f7f8fa;
    border: 1px solid #edf0f4;
    padding: 18px;

    span {
      color: #98a2b3;
      font-size: 12px;
      font-weight: 900;
    }

    strong {
      display: block;
      color: #101828;
      font-size: 18px;
      margin: 8px 0;
    }

    p {
      margin: 0;
      color: #667085;
      line-height: 1.7;
    }
  }

  .description-card,
  .service-panel {
    border-radius: 18px;
    border: 1px solid #edf0f4;
    background: #fff;
    padding: 22px;

    h3 {
      margin: 0 0 14px;
      color: #101828;
      font-size: 18px;
    }
  }

  .main-content .description-card .text {
    background: #fafafa;
    border: 1px solid #f0f2f5;
    border-radius: 14px;
    color: #344054;
    line-height: 1.9;
    padding: 18px;
  }

  .notice-strip {
    margin-top: 16px;
    border-radius: 16px;
    background: #fff8d7;
    border: 1px solid #ffeaa3;
    padding: 14px 16px;
    color: #665500;
    display: flex;
    gap: 12px;
    align-items: center;
    line-height: 1.7;
  }

  .spec-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    border: 1px solid #edf0f4;
    border-radius: 18px;
    overflow: hidden;
    margin-bottom: 20px;
  }

  .spec-item {
    display: grid;
    grid-template-columns: 110px minmax(0, 1fr);
    gap: 14px;
    padding: 16px 18px;
    border-bottom: 1px solid #edf0f4;
    background: #fff;

    &:nth-child(odd) {
      border-right: 1px solid #edf0f4;
    }

    &:nth-last-child(-n + 2) {
      border-bottom: 0;
    }

    span {
      color: #98a2b3;
    }

    strong {
      color: #1d2939;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .service-list {
    display: grid;
    gap: 12px;

    div {
      background: #f7f8fa;
      border-radius: 14px;
      padding: 14px 16px;
    }

    strong {
      color: #101828;
    }

    p {
      margin: 6px 0 0;
      color: #667085;
      line-height: 1.7;
    }
  }

  .recommend {
    padding: 22px;
    position: sticky;
    top: 96px;
  }

  .recommend .things {
    max-height: none;
    overflow: visible;
  }

  .recommend .things .thing-item {
    display: grid;
    grid-template-columns: 92px minmax(0, 1fr);
    gap: 14px;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px;
    border: 1px solid #edf0f4;
    border-radius: 16px;
  }

  .recommend .things .thing-item .img-view {
    height: 92px;
    border-radius: 12px;
  }

  .recommend .things .thing-item .img-view img {
    max-height: 82px;
  }

  .recommend .things .thing-item .info-view {
    min-width: 0;
    padding: 0;
  }

  .recommend .things .thing-item .info-view .thing-name {
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  @media (max-width: 768px) {
    .detail-content {
      width: 100%;
      padding: 0 15px;
    }

    .thing-infos-view {
      flex-direction: column;
      padding: 20px;

      .thing-infos {
        flex-direction: column;
      }

      .thing-img-box {
        margin: 0 0 20px 0;
        width: 100%;
        flex: none;
      }

      .thing-counts {
        margin-left: 0;
        margin-top: 20px;
        border-left: none;
        border-top: 1px solid #f0f0f0;
        padding-left: 0;
        padding-top: 20px;
      }
    }

    .thing-content-view {
      flex-direction: column;

      .recommend {
        margin-top: 20px;
        width: 100%;
      }
    }
  }

  @media (max-width: 1180px) {
    .detail-hero {
      grid-template-columns: 360px minmax(0, 1fr);
    }

    .hero-actions {
      grid-column: 1 / -1;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      border-left: 0;
      border-top: 1px solid #edf0f4;
      padding-left: 0;
      padding-top: 20px;
    }

    .content-layout {
      grid-template-columns: 1fr;
    }

    .recommend {
      position: static;
    }

    .recommend .things {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 14px;
    }
  }

  @media (max-width: 820px) {
    .detail-content {
      width: min(100% - 28px, 720px);
      margin-top: 96px;
      padding: 0;
    }

    .detail-hero {
      grid-template-columns: 1fr;
      padding: 18px;
      border-radius: 18px;
      gap: 20px;
    }

    .thing-name {
      font-size: 25px !important;
    }

    .price-panel,
    .meta-grid,
    .action-row,
    .notice-strip {
      grid-template-columns: 1fr;
      flex-direction: column;
      align-items: stretch;
    }

    .meta-grid,
    .highlight-grid,
    .spec-grid,
    .hero-actions,
    .recommend .things {
      grid-template-columns: 1fr;
    }

    .spec-item {
      grid-template-columns: 92px minmax(0, 1fr);
      border-right: 0 !important;

      &:nth-last-child(-n + 2) {
        border-bottom: 1px solid #edf0f4;
      }

      &:last-child {
        border-bottom: 0;
      }
    }

    .main-content {
      padding: 20px;
    }

    .tab-container {
      overflow-x: auto;
    }

    .buy-btn,
    .cart-btn {
      width: 100%;
    }
  }
</style>
