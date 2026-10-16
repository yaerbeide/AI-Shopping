<template>
  <section class="explore-page">
    <div class="explore-shell">
      <aside class="explore-panel">
        <button class="back-button" type="button" @click="backToHome">
          <arrow-left-outlined />
          <span>返回首页</span>
        </button>

        <div class="panel-section">
          <div class="section-title">分类</div>
          <button
            v-for="item in tData.categoryList"
            :key="item.id"
            class="side-filter"
            :class="{ active: String(route.query.c || '') === String(item.id) }"
            type="button"
            @click="selectCategory(item)"
          >
            <appstore-outlined />
            <span>{{ item.title }}</span>
          </button>
        </div>
      </aside>

      <main class="explore-main">
        <div class="result-hero">
          <div class="hero-copy">
            <div class="eyebrow">SEARCH RESULTS</div>
            <h1>{{ getSearchTitle() }}</h1>
            <p>{{ summaryText }}</p>
            <div class="hero-slogan">{{ heroSlogan }}</div>
            <div class="hero-badges">
              <span>{{ heroBadgePrimary }}</span>
              <span>{{ heroBadgeSecondary }}</span>
            </div>
          </div>
          <div class="result-count">
            <strong>{{ tData.total }}</strong>
            <span>件商品</span>
          </div>
        </div>

        <div class="tools-card">
          <div class="query-pill" v-if="activeQueryLabel">
            <search-outlined />
            <span>{{ activeQueryLabel }}</span>
            <button type="button" @click="clearFilters">
              <close-outlined />
            </button>
          </div>
          <div class="query-pill muted" v-else>
            <compass-outlined />
            <span>全部探索结果</span>
          </div>

          <div class="sort-group">
            <button
              v-for="item in sortOptions"
              :key="item.value"
              type="button"
              :class="{ active: sortMode === item.value }"
              @click="changeSort(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <section class="ai-guide-card">
          <div class="ai-guide-head">
            <div>
              <span>AI GUIDE</span>
              <h2>&#x667A;&#x80FD;&#x5BFC;&#x8D2D;</h2>
            </div>
            <p>&#x7528;&#x81EA;&#x7136;&#x8BED;&#x8A00;&#x63CF;&#x8FF0;&#x9884;&#x7B97;&#x3001;&#x7528;&#x9014;&#x6216;&#x9001;&#x793C;&#x573A;&#x666F;&#xFF0C;AI &#x4F1A;&#x4ECE;&#x5DF2;&#x4E0A;&#x67B6;&#x5546;&#x54C1;&#x4E2D;&#x5E2E;&#x4F60;&#x6311;&#x9009;&#x3002;</p>
          </div>

          <div class="ai-guide-input">
            <a-input
              v-model:value="aiGuide.query"
              size="large"
              allow-clear
              placeholder="&#x4F8B;&#x5982;&#xFF1A;3000&#x5143;&#x4EE5;&#x5185;&#x9002;&#x5408;&#x7236;&#x6BCD;&#x7528;&#x7684;&#x624B;&#x673A;"
              @pressEnter="runAiGuide"
            />
            <a-button type="primary" size="large" :loading="aiGuide.loading" @click="runAiGuide">
              AI &#x63A8;&#x8350;
            </a-button>
          </div>

          <div v-if="aiGuide.response" class="ai-guide-result">
            <div class="ai-answer">
              <strong>&#x5BFC;&#x8D2D;&#x5EFA;&#x8BAE;</strong>
              <p>{{ aiGuide.response.answer }}</p>
            </div>

            <div v-if="aiGuide.response.recommendations && aiGuide.response.recommendations.length" class="ai-recommend-grid">
              <article
                v-for="item in aiGuide.response.recommendations"
                :key="item.thingId"
                class="ai-recommend-card"
                @click="handleDetail(item.thing)"
              >
                <div class="ai-product-img">
                  <img v-if="item.thing" :src="item.thing.cover" :alt="item.thing.title" />
                </div>
                <div class="ai-product-info">
                  <h3>{{ item.thing ? item.thing.title : item.thingId }}</h3>
                  <div class="ai-price" v-if="item.thing">&yen;{{ item.thing.price }}</div>
                  <p>{{ item.reason }}</p>
                  <small>{{ item.risk }}</small>
                </div>
              </article>
            </div>

            <div v-if="aiGuide.response.nextQuestions && aiGuide.response.nextQuestions.length" class="ai-next-questions">
              <button v-for="question in aiGuide.response.nextQuestions" :key="question" type="button" @click="askNext(question)">
                {{ question }}
              </button>
            </div>
          </div>
        </section>

        <div class="tag-strip" v-if="tData.tagList.length">
          <button
            v-for="item in tData.tagList.slice(0, 10)"
            :key="item.id"
            class="tag-chip"
            :class="{ active: String(route.query.tag || '') === String(item.id) }"
            type="button"
            @click="selectTag(item)"
          >
            {{ item.title }}
          </button>
        </div>

        <a-spin :spinning="tData.loading" style="min-height: 320px">
          <div v-if="tData.pageData.length" class="product-grid">
            <article v-for="item in tData.pageData" :key="item.id" class="product-card" @click="handleDetail(item)">
              <div class="image-frame">
                <img :src="item.cover" :alt="item.title" />
              </div>
              <div class="card-body">
                <div class="product-meta">
                  <span>精选</span>
                  <span v-if="item.score">{{ item.score }} 分</span>
                </div>
                <h3>{{ item.title }}</h3>
                <div class="card-footer">
                  <div class="price">
                    <span>￥</span>{{ item.price }}
                  </div>
                  <button type="button" class="detail-button" @click.stop="handleDetail(item)">
                    查看
                  </button>
                </div>
              </div>
            </article>
          </div>

          <div v-else-if="!tData.loading" class="empty-state">
            <div class="empty-icon">
              <search-outlined />
            </div>
            <h3>暂无相关商品</h3>
            <p>换个关键词、分类或标签试试。</p>
            <button type="button" @click="clearFilters">查看全部商品</button>
          </div>
        </a-spin>

        <div class="page-view" v-if="tData.total > tData.pageSize">
          <a-pagination
            v-model:value="tData.page"
            @change="changePage"
            :defaultPageSize="tData.pageSize"
            :total="tData.total"
            show-less-items
          />
        </div>
      </main>
    </div>
  </section>
</template>

<script setup>
  import {
    AppstoreOutlined,
    ArrowLeftOutlined,
    CloseOutlined,
    CompassOutlined,
    SearchOutlined,
  } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { listApi as listThingList } from '/@/api/thing';
  import { guideApi as aiGuideApi } from '/@/api/ai';
  import { listApi as listClassificationList } from '/@/api/classification';
  import { listApi as listTagList } from '/@/api/tag';
  import { BASE_URL } from '/@/store/constants';

  const router = useRouter();
  const route = useRoute();

  const sortOptions = [
    { label: '综合', value: 'default' },
    { label: '价格低', value: 'priceAsc' },
    { label: '价格高', value: 'priceDesc' },
  ];
  const sortMode = ref('default');

  const tData = reactive({
    loading: false,
    keyword: '',
    thingData: [],
    pageData: [],
    categoryList: [],
    tagList: [],
    page: 1,
    total: 0,
    pageSize: 12,
  });

  const aiGuide = reactive({
    query: '',
    loading: false,
    response: null,
  });
  const AI_GUIDE_CACHE_PREFIX = 'mall.aiGuide.';

  const summaryText = computed(() => {
    if (route.query.keyword) {
      return `围绕“${route.query.keyword}”为你筛选出的灵感清单`;
    }
    if (route.query.c) {
      return '把这个分类里的好物重新排了排';
    }
    if (route.query.tag) {
      return '该标签下值得多看一眼的精选商品';
    }
    return '从全站商品里整理出这份可逛清单';
  });

  const heroSlogan = computed(() => {
    const title = getSearchTitle();
    if (route.query.keyword) return `围绕 ${title}，把选择变得更轻松。`;
    if (route.query.tag) return `${title} 专区，今天就逛点有意思的。`;
    if (route.query.c) return `${title} 好物集合，适合慢慢挑。`;
    return '不急着下单，先发现一些刚刚好的选择。';
  });

  const heroBadgePrimary = computed(() => {
    if (route.query.tag) return '标签精选';
    if (route.query.c) return '分类优选';
    if (route.query.keyword) return '关键词匹配';
    return '全站发现';
  });

  const heroBadgeSecondary = computed(() => {
    if (tData.total <= 0) return '等待补货';
    if (tData.total <= 3) return '少而精';
    if (tData.total <= 10) return '刚好可选';
    return '选择丰富';
  });

  const activeQueryLabel = computed(() => {
    if (route.query.keyword) return `关键词：${route.query.keyword}`;
    if (route.query.c) return `分类：${getSearchTitle()}`;
    if (route.query.tag) return `标签：${getSearchTitle()}`;
    return '';
  });

  onMounted(() => {
    restoreAiGuideCache();
    if (!aiGuide.query && route.query.keyword) {
      aiGuide.query = String(route.query.keyword);
    }
    getCategories();
    getTags();
    search();
  });

  watch(
    () => route.query,
    () => {
      search();
      restoreAiGuideCache();
    },
    { immediate: false },
  );

  const getCategories = () => {
    listClassificationList()
      .then((res) => {
        tData.categoryList = res.data || [];
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const getTags = () => {
    listTagList()
      .then((res) => {
        tData.tagList = res.data || [];
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const getSearchTitle = () => {
    if (route.query.keyword) {
      return route.query.keyword;
    }
    if (route.query.c) {
      const category = tData.categoryList.find((item) => String(item.id) === String(route.query.c));
      return category ? category.title : '分类商品';
    }
    if (route.query.tag) {
      const tag = tData.tagList.find((item) => String(item.id) === String(route.query.tag));
      return tag ? tag.title : '标签商品';
    }
    return '搜索结果';
  };

  const search = () => {
    const searchParams = {};

    if (route.query.keyword) {
      tData.keyword = String(route.query.keyword).trim();
      searchParams.keyword = tData.keyword;
    }
    if (route.query.c) {
      searchParams.c = route.query.c;
    }
    if (route.query.tag) {
      searchParams.tag = route.query.tag;
    }

    getThingList(searchParams);
  };

  const backToHome = () => {
    router.push({ name: 'portal' });
  };

  const selectCategory = (category) => {
    router.push({ name: 'search', query: { c: category.id } });
  };

  const selectTag = (tag) => {
    router.push({ name: 'search', query: { tag: tag.id } });
  };

  const clearFilters = () => {
    router.push({ name: 'search' });
  };

  const changeSort = (value) => {
    sortMode.value = value;
    changePage(1);
  };

  const getSortedThings = () => {
    const list = [...tData.thingData];
    if (sortMode.value === 'priceAsc') {
      return list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0));
    }
    if (sortMode.value === 'priceDesc') {
      return list.sort((a, b) => Number(b.price || 0) - Number(a.price || 0));
    }
    return list;
  };

  const changePage = (page) => {
    tData.page = page;
    const sortedList = getSortedThings();
    const start = (tData.page - 1) * tData.pageSize;
    tData.pageData = sortedList.slice(start, start + tData.pageSize);
  };

  const handleDetail = (item) => {
    if (!item) return;
    saveAiGuideCache();
    router.push({ name: 'detail', query: { id: item.id } });
  };

  const getAiGuideCacheKey = () => `${AI_GUIDE_CACHE_PREFIX}${route.fullPath || route.path}`;

  const normalizeAiGuideResponse = (data) => {
    if (data && data.recommendations) {
      data.recommendations.forEach((item) => {
        if (item.thing && item.thing.cover && !String(item.thing.cover).startsWith('http')) {
          item.thing.cover = BASE_URL + '/api/staticfiles/image/' + item.thing.cover;
        }
      });
    }
    return data;
  };

  const saveAiGuideCache = () => {
    if (!aiGuide.query && !aiGuide.response) return;
    try {
      sessionStorage.setItem(
        getAiGuideCacheKey(),
        JSON.stringify({
          query: aiGuide.query,
          response: aiGuide.response,
        }),
      );
    } catch (err) {
      console.log(err);
    }
  };

  const restoreAiGuideCache = () => {
    try {
      const cached = sessionStorage.getItem(getAiGuideCacheKey());
      if (!cached) {
        aiGuide.response = null;
        aiGuide.query = route.query.keyword ? String(route.query.keyword) : '';
        return;
      }
      const data = JSON.parse(cached);
      aiGuide.query = data.query || (route.query.keyword ? String(route.query.keyword) : '');
      aiGuide.response = normalizeAiGuideResponse(data.response || null);
      aiGuide.loading = false;
    } catch (err) {
      console.log(err);
      aiGuide.response = null;
      aiGuide.query = route.query.keyword ? String(route.query.keyword) : '';
    }
  };

  const runAiGuide = () => {
    const query = String(aiGuide.query || '').trim();
    if (!query) {
      message.warning('请输入你的导购需求');
      return;
    }

    aiGuide.loading = true;
    aiGuideApi({ query })
      .then((res) => {
        aiGuide.response = normalizeAiGuideResponse(res.data || null);
        saveAiGuideCache();
      })
      .catch((err) => {
        console.log(err);
        message.error('AI 导购暂时不可用，请稍后再试');
      })
      .finally(() => {
        aiGuide.loading = false;
      });
  };

  const askNext = (question) => {
    aiGuide.query = question;
    runAiGuide();
  };

  const getThingList = (data) => {
    tData.loading = true;
    listThingList(data)
      .then((res) => {
        const list = res.data || [];
        list.forEach((item) => {
          if (item.cover && !String(item.cover).startsWith('http')) {
            item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover;
          }
        });
        tData.thingData = list;
        tData.total = list.length;
        changePage(1);
        tData.loading = false;
      })
      .catch((err) => {
        console.log(err);
        tData.loading = false;
      });
  };
</script>

<style scoped lang="less">
  .explore-page {
    min-height: calc(100vh - 80px);
    padding: 112px 0 80px;
    background:
      radial-gradient(circle at 8% 10%, rgba(255, 209, 0, 0.16), transparent 24%),
      linear-gradient(180deg, #f6f8fa 0%, #ffffff 42%);
  }

  .explore-shell {
    width: min(1400px, calc(100% - 80px));
    max-width: 1400px;
    margin-left: auto;
    margin-right: auto;
    display: grid;
    grid-template-columns: 240px minmax(0, 1fr);
    gap: 28px;
    justify-content: center;
  }

  .explore-main {
    min-width: 0;
    max-width: 100%;
  }

  .explore-panel {
    position: sticky;
    top: 104px;
    align-self: start;
    max-height: calc(100vh - 128px);
    overflow: auto;
    background: #fff;
    border-radius: 22px;
    padding: 20px;
    box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  }

  .explore-panel::-webkit-scrollbar {
    width: 0;
    height: 0;
  }

  .back-button {
    width: 100%;
    height: 40px;
    border: 0;
    border-radius: 12px;
    background: #f5f6f8;
    color: #24272d;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-weight: 700;
    cursor: pointer;
    transition: background-color 0.2s, transform 0.2s;

    &:hover {
      background: #ffd100;
      transform: translateY(-1px);
    }
  }

  .panel-section {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 18px;

    .section-title {
      color: #9aa0aa;
      font-size: 12px;
      font-weight: 900;
      padding: 0 4px 6px;
    }
  }

  .side-filter {
    height: 39px;
    border: 0;
    border-radius: 12px;
    background: transparent;
    color: #5d636d;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 12px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: background-color 0.2s, color 0.2s, transform 0.2s;

    &:hover,
    &.active {
      background: #ffd100;
      color: #24272d;
      transform: translateX(2px);
    }
  }

  .explore-main {
    min-width: 0;
  }

  .result-hero {
    position: relative;
    max-width: 100%;
    min-height: 176px;
    border-radius: 28px;
    padding: 30px 34px;
    color: #fff;
    background:
      linear-gradient(90deg, rgba(20, 23, 31, 0.94), rgba(20, 23, 31, 0.58)),
      url('/src/assets/images/banner-02.webp') center/cover;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    box-shadow: 0 22px 54px rgba(15, 23, 42, 0.16);
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      inset: auto 190px -58px auto;
      width: 260px;
      height: 260px;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 50%;
    }

    &::after {
      content: '';
      position: absolute;
      left: 34px;
      right: 34px;
      bottom: 24px;
      height: 1px;
      background: linear-gradient(90deg, rgba(255, 209, 0, 0.6), transparent);
      opacity: 0.65;
    }

    .hero-copy {
      position: relative;
      z-index: 1;
      max-width: 720px;
    }

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
      line-height: 1.15;
      text-shadow: 0 8px 24px rgba(0, 0, 0, 0.26);
    }

    p {
      color: rgba(255, 255, 255, 0.78);
      margin: 0;
      font-weight: 600;
    }
  }

  .hero-slogan {
    margin-top: 12px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 15px;
    font-weight: 800;
  }

  .hero-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 14px;

    span {
      height: 30px;
      display: inline-flex;
      align-items: center;
      border-radius: 999px;
      padding: 0 12px;
      background: rgba(255, 255, 255, 0.12);
      color: rgba(255, 255, 255, 0.86);
      border: 1px solid rgba(255, 255, 255, 0.12);
      font-size: 12px;
      font-weight: 800;
      backdrop-filter: blur(10px);
    }
  }

  .result-count {
    position: relative;
    z-index: 1;
    min-width: 156px;
    min-height: 118px;
    border-radius: 30px;
    background:
      linear-gradient(145deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.08));
    border: 1px solid rgba(255, 255, 255, 0.18);
    backdrop-filter: blur(14px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);

    strong {
      color: #ffd100;
      font-size: 64px;
      line-height: 0.86;
      font-weight: 1000;
      letter-spacing: 0;
      text-shadow: 0 10px 26px rgba(255, 209, 0, 0.28);
    }

    span {
      color: rgba(255, 255, 255, 0.78);
      font-size: 13px;
      margin-top: 12px;
      font-weight: 900;
    }
  }

  .tools-card {
    max-width: 100%;
    margin-top: 22px;
    background: #fff;
    border-radius: 22px;
    padding: 18px;
    box-shadow: 0 14px 42px rgba(15, 23, 42, 0.06);
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .query-pill {
    min-height: 42px;
    border-radius: 999px;
    background: #f5f6f8;
    color: #24272d;
    padding: 0 12px 0 16px;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-weight: 800;

    &.muted {
      color: #767d88;
    }

    button {
      width: 26px;
      height: 26px;
      border: 0;
      border-radius: 50%;
      background: #fff;
      color: #767d88;
      cursor: pointer;
    }
  }

  .sort-group {
    display: flex;
    gap: 8px;
    padding: 4px;
    border-radius: 999px;
    background: #f5f6f8;

    button {
      height: 34px;
      border: 0;
      border-radius: 999px;
      background: transparent;
      color: #767d88;
      padding: 0 16px;
      font-weight: 800;
      cursor: pointer;

      &.active {
        background: #24272d;
        color: #fff;
      }
    }
  }

  .ai-guide-card {
    max-width: 100%;
    margin-top: 18px;
    background: #fff;
    border: 1px solid #edf0f4;
    border-radius: 22px;
    padding: 20px;
    box-shadow: 0 14px 36px rgba(15, 23, 42, 0.05);
  }

  .ai-guide-head {
    display: flex;
    justify-content: space-between;
    gap: 24px;
    margin-bottom: 16px;

    span {
      color: #8d929c;
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.08em;
    }

    h2 {
      margin: 4px 0 0;
      color: #24272d;
      font-size: 22px;
      font-weight: 900;
    }

    p {
      max-width: 560px;
      margin: 0;
      color: #767d88;
      line-height: 1.7;
    }
  }

  .ai-guide-input {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 12px;

    :deep(.ant-input-affix-wrapper) {
      border-radius: 14px;
      border-color: #e5e8ef;
      box-shadow: none;
    }

    :deep(.ant-btn-primary) {
      border-radius: 14px;
      background: #24272d;
      border-color: #24272d;
      box-shadow: none;
      font-weight: 800;
    }
  }

  .ai-guide-result {
    margin-top: 18px;
    display: grid;
    gap: 16px;
  }

  .ai-answer {
    padding: 14px 16px;
    border-radius: 16px;
    background: #f7f8fa;
    color: #4f5662;

    strong {
      display: block;
      color: #24272d;
      margin-bottom: 6px;
    }

    p {
      margin: 0;
      line-height: 1.7;
    }

    span {
      display: block;
      margin-top: 8px;
      color: #8a6d1d;
      font-size: 12px;
    }
  }

  .ai-recommend-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .ai-recommend-card {
    display: grid;
    grid-template-columns: 104px minmax(0, 1fr);
    gap: 14px;
    padding: 12px;
    border: 1px solid #edf0f4;
    border-radius: 16px;
    background: #fff;
    cursor: pointer;
    transition: border-color 0.2s, transform 0.2s;

    &:hover {
      border-color: #d7e3f8;
      transform: translateY(-2px);
    }
  }

  .ai-product-img {
    height: 104px;
    border-radius: 14px;
    background: #f4f5f7;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      width: 86%;
      height: 86%;
      object-fit: contain;
    }
  }

  .ai-product-info {
    min-width: 0;

    h3 {
      margin: 0;
      color: #24272d;
      font-size: 15px;
      font-weight: 900;
      line-height: 1.45;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    p {
      margin: 8px 0 4px;
      color: #4f5662;
      line-height: 1.55;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    small {
      color: #8d929c;
      line-height: 1.45;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .ai-price {
    margin-top: 6px;
    color: #ff4d57;
    font-size: 18px;
    font-weight: 900;
  }

  .ai-next-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    button {
      min-height: 34px;
      border: 1px solid #e5e8ef;
      border-radius: 999px;
      background: #fff;
      color: #4f5662;
      padding: 0 14px;
      cursor: pointer;
      font-weight: 700;

      &:hover {
        border-color: #24272d;
        color: #24272d;
      }
    }
  }

  .tag-strip {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin: 24px 0 8px;
  }

  .tag-chip {
    height: 38px;
    border: 0;
    border-radius: 999px;
    background: #fff;
    color: #6f7682;
    padding: 0 18px;
    font-weight: 800;
    cursor: pointer;
    box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);

    &:hover,
    &.active {
      background: #ffd100;
      color: #24272d;
    }
  }

  .product-grid {
    max-width: 100%;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 24px;
    margin-top: 24px;
  }

  .product-card {
    background: #fff;
    border-radius: 24px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 16px 44px rgba(15, 23, 42, 0.06);
    transition: transform 0.22s, box-shadow 0.22s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 22px 54px rgba(15, 23, 42, 0.12);

      .image-frame img {
        transform: scale(1.04);
      }
    }
  }

  .image-frame {
    aspect-ratio: 1.2;
    background: #f4f5f7;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      width: 86%;
      height: 86%;
      object-fit: contain;
      transition: transform 0.24s;
    }
  }

  .card-body {
    padding: 18px 18px 20px;
  }

  .product-meta {
    display: flex;
    justify-content: space-between;
    color: #9aa0aa;
    font-size: 12px;
    font-weight: 800;
  }

  .product-card h3 {
    color: #24272d;
    font-size: 17px;
    font-weight: 900;
    line-height: 1.45;
    margin: 10px 0 16px;
    min-height: 48px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .price {
    color: #ff4d57;
    font-size: 24px;
    font-weight: 900;

    span {
      font-size: 13px;
      margin-right: 2px;
    }
  }

  .detail-button,
  .empty-state button {
    height: 38px;
    border: 0;
    border-radius: 999px;
    background: #24272d;
    color: #fff;
    padding: 0 18px;
    font-weight: 800;
    cursor: pointer;
  }

  .empty-state {
    margin-top: 28px;
    min-height: 360px;
    border-radius: 28px;
    background: #fff;
    box-shadow: 0 18px 48px rgba(15, 23, 42, 0.06);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;

    .empty-icon {
      width: 68px;
      height: 68px;
      border-radius: 24px;
      background: #ffd100;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: #24272d;
    }

    h3 {
      margin: 18px 0 8px;
      color: #24272d;
      font-size: 24px;
      font-weight: 900;
    }

    p {
      color: #8b929d;
      margin-bottom: 22px;
    }
  }

  .page-view {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-top: 42px;
  }

  @media (max-width: 1180px) {
    .explore-shell {
      grid-template-columns: 1fr;
    }

    .explore-panel {
      position: static;
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 18px;

      .panel-section {
        grid-column: 1 / -1;
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
      }

      .section-title {
        grid-column: 1 / -1;
      }
    }

    .product-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 760px) {
    .explore-page {
      padding-top: 96px;
    }

    .explore-shell {
      width: min(100% - 28px, 1320px);
      gap: 20px;
    }

    .explore-panel {
      display: block;
      padding: 18px;

      .panel-section {
        display: flex;
        overflow-x: auto;
        padding-bottom: 4px;
      }

      .side-filter {
        flex: 0 0 auto;
      }
    }

    .result-hero {
      min-height: 170px;
      padding: 24px;
      align-items: flex-start;
      flex-direction: column;

      h1 {
        font-size: 32px;
      }

      &::before {
        right: -80px;
        bottom: -120px;
      }

      &::after {
        left: 24px;
        right: 24px;
        bottom: 18px;
      }
    }

    .result-count {
      min-width: 120px;
      min-height: 88px;
      border-radius: 22px;

      strong {
        font-size: 46px;
      }
    }

    .tools-card {
      align-items: stretch;
      flex-direction: column;
    }

    .ai-guide-head,
    .ai-guide-input {
      grid-template-columns: 1fr;
      flex-direction: column;
    }

    .ai-recommend-grid {
      grid-template-columns: 1fr;
    }

    .sort-group {
      width: 100%;
      overflow-x: auto;

      button {
        flex: 1;
      }
    }

    .product-grid {
      grid-template-columns: 1fr;
      gap: 18px;
    }
  }
  .side-filter,
  .tag-chip,
  .sort-group button,
  .query-pill,
  .product-card,
  .detail-button,
  .ai-recommend-card,
  .ai-next-questions button {
    transition: background-color 180ms ease, color 180ms ease, border-color 180ms ease, box-shadow 180ms ease,
      transform 180ms ease, opacity 180ms ease;
  }

  .side-filter:hover,
  .tag-chip:hover,
  .sort-group button:hover,
  .product-card:hover,
  .ai-recommend-card:hover {
    transform: translateY(-2px);
  }

  @media (prefers-reduced-motion: reduce) {
    .side-filter,
    .tag-chip,
    .sort-group button,
    .query-pill,
    .product-card,
    .detail-button,
    .ai-recommend-card,
    .ai-next-questions button {
      transition: none !important;
    }
  }
</style>
