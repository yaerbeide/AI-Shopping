<template>
  <a-spin :spinning="showSpin">
    <div class="overview-container">
      <a-row :gutter="[24, 24]">
        <!-- 左侧主内容 -->
        <a-col :xs="24" :xl="16">
          <a-row :gutter="[24, 24]">
            <!-- 统计卡片 (基于项目原有数据) -->
            <a-col :xs="24" :sm="12" :lg="6">
              <div class="stat-card mini">
                <div class="stat-header">
                  <span class="stat-title">商品总数</span>
                  <shopping-outlined class="stat-icon-mini" />
                </div>
                <div class="stat-body">
                  <h2 class="stat-value">{{ tdata.data.spzs || 0 }}</h2>
                  <span class="stat-desc">所有商品种类</span>
                </div>
              </div>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <div class="stat-card mini">
                <div class="stat-header">
                  <span class="stat-title">未付订单</span>
                  <clock-circle-outlined class="stat-icon-mini warning" />
                </div>
                <div class="stat-body">
                  <h2 class="stat-value">{{ tdata.data.wfdd || 0 }}</h2>
                  <span class="stat-desc">共 {{ tdata.data.wfddrs || 0 }} 人</span>
                </div>
              </div>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <div class="stat-card mini">
                <div class="stat-header">
                  <span class="stat-title">已付订单</span>
                  <check-circle-outlined class="stat-icon-mini success" />
                </div>
                <div class="stat-body">
                  <h2 class="stat-value">{{ tdata.data.yfdd || 0 }}</h2>
                  <span class="stat-desc">共 {{ tdata.data.yfddrs || 0 }} 人</span>
                </div>
              </div>
            </a-col>
            <a-col :xs="24" :sm="12" :lg="6">
              <div class="stat-card mini">
                <div class="stat-header">
                  <span class="stat-title">取消订单</span>
                  <close-circle-outlined class="stat-icon-mini danger" />
                </div>
                <div class="stat-body">
                  <h2 class="stat-value">{{ tdata.data.qxdd || 0 }}</h2>
                  <span class="stat-desc">共 {{ tdata.data.qxddrs || 0 }} 人</span>
                </div>
              </div>
            </a-col>

            <!-- 访问量折线图 -->
            <a-col :span="24">
              <div class="main-chart-card">
                <div class="chart-header">
                  <h3>最近一周访问量</h3>
                  <div class="chart-legend">
                    <span class="legend-item"><i class="dot blue"></i> IP (UV)</span>
                    <span class="legend-item"><i class="dot green"></i> Visit (PV)</span>
                  </div>
                </div>
                <div class="chart-content" ref="visitChartDiv"></div>
              </div>
            </a-col>

            <!-- 热门商品排名 -->
            <a-col :span="24">
              <div class="main-chart-card">
                <div class="chart-header">
                  <h3>热门商品排名</h3>
                </div>
                <div class="chart-content" ref="barChartDiv"></div>
              </div>
            </a-col>
          </a-row>
        </a-col>

        <!-- 右侧侧边内容 -->
        <a-col :xs="24" :xl="8">
          <a-row :gutter="[24, 24]">
            <!-- 热门分类比例 -->
            <a-col :span="24">
              <div class="side-chart-card">
                <div class="card-header">
                  <h3>热门分类比例</h3>
                </div>
                <div class="donut-chart-wrapper">
                  <div class="donut-chart" ref="pieChartDiv"></div>
                </div>
              </div>
            </a-col>

            <!-- 原有的热门产品列表样式优化 -->
            <a-col :span="24">
              <div class="product-list-card">
                <div class="card-header">
                  <h3>热门商品详情</h3>
                </div>
                <div class="product-list">
                  <div v-for="product in popularProducts" :key="product.id" class="product-item">
                    <img :src="product.cover" class="product-img" />
                    <div class="product-info">
                      <h4>{{ product.title }}</h4>
                      <p>{{ product.category }}</p>
                    </div>
                    <div class="product-count">
                      <span class="count-tag">{{ product.count }} 销量</span>
                    </div>
                  </div>
                </div>
              </div>
            </a-col>
          </a-row>
        </a-col>
      </a-row>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted, onUnmounted } from 'vue';
  import {
    RiseOutlined,
    FallOutlined,
    ShoppingOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    TeamOutlined,
  } from '@ant-design/icons-vue';
  import { listApi } from '/@/api/overview';
  import { BASE_URL } from '/@/store/constants';

  declare const echarts: any;

  let showSpin = ref(true);
  const visitChartDiv = ref();
  const barChartDiv = ref();
  const pieChartDiv = ref();

  let charts: any[] = [];

  let tdata = reactive({
    data: {} as any,
  });

  const popularProducts = ref<any[]>([]);

  onMounted(() => {
    fetchData();
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    charts.forEach((chart) => chart.dispose());
  });

  const handleResize = () => {
    charts.forEach((chart) => chart.resize());
  };

  const fetchData = () => {
    listApi({})
      .then((res) => {
        tdata.data = res.data;

        // 处理热门产品数据
        popularProducts.value = res.data.popularThings
          .map((item: any) => ({
            id: item.id,
            title: item.title,
            category: item.classificationTitle || '通用',
            cover: BASE_URL + '/api/staticfiles/image/' + item.cover,
            count: item.count,
          }))
          .slice(0, 6);

        initAllCharts();
        showSpin.value = false;
      })
      .catch(() => {
        showSpin.value = false;
      });
  };

  const initAllCharts = () => {
    initVisitChart();
    initBarChart();
    initPieChart();
  };

  const initVisitChart = () => {
    const chart = echarts.init(visitChartDiv.value);
    charts.push(chart);

    const xData = tdata.data.visitList.map((i: any) => i.day);
    const uvData = tdata.data.visitList.map((i: any) => i.uv);
    const pvData = tdata.data.visitList.map((i: any) => i.pv);

    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xData,
        axisLine: { show: false },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
      },
      series: [
        {
          name: 'IP (UV)',
          type: 'line',
          smooth: true,
          data: uvData,
          lineStyle: { width: 4, color: '#4318ff' },
          showSymbol: false,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(67, 24, 255, 0.2)' },
              { offset: 1, color: 'rgba(67, 24, 255, 0)' },
            ]),
          },
        },
        {
          name: 'Visit (PV)',
          type: 'line',
          smooth: true,
          data: pvData,
          lineStyle: { width: 4, color: '#05cd99' },
          showSymbol: false,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(5, 205, 153, 0.2)' },
              { offset: 1, color: 'rgba(5, 205, 153, 0)' },
            ]),
          },
        },
      ],
    });
  };

  const initBarChart = () => {
    const chart = echarts.init(barChartDiv.value);
    charts.push(chart);

    const xData = tdata.data.popularThings.map((item: any) => item.title);
    const yData = tdata.data.popularThings.map((item: any) => item.count);

    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: xData,
        axisLabel: { rotate: 30, color: '#a3aed0', fontWeight: 600 },
        axisLine: { show: false },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
      },
      series: [
        {
          type: 'bar',
          data: yData,
          barWidth: '40%',
          itemStyle: {
            borderRadius: [8, 8, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#4318ff' },
              { offset: 1, color: '#6ad2ff' },
            ]),
          },
        },
      ],
    });
  };

  const initPieChart = () => {
    const chart = echarts.init(pieChartDiv.value);
    charts.push(chart);

    const pieData = tdata.data.popularClassification.map((item: any) => ({
      name: item.title,
      value: item.count,
    }));

    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { color: '#a3aed0', fontWeight: 600 } },
      series: [
        {
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 5 },
          label: { show: false },
          data: pieData,
          color: ['#4318ff', '#6ad2ff', '#05cd99', '#ffb547', '#ee5d50'],
        },
      ],
    });
  };
</script>

<style lang="less" scoped>
  .overview-container {
    padding: 0;
  }

  .stat-card.mini {
    background: #fff;
    border-radius: 20px;
    padding: 20px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.12);

    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .stat-title {
        font-size: 13px;
        color: #a3aed0;
        font-weight: 700;
      }

      .stat-icon-mini {
        font-size: 18px;
        color: #4318ff;
        &.warning {
          color: #ffb547;
        }
        &.success {
          color: #05cd99;
        }
        &.danger {
          color: #ee5d50;
        }
      }
    }

    .stat-body {
      .stat-value {
        font-size: 24px;
        font-weight: 800;
        color: #1b2559;
        margin: 0;
      }
      .stat-desc {
        font-size: 12px;
        color: #a3aed0;
        font-weight: 600;
      }
    }
  }

  .main-chart-card {
    background: #fff;
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.12);

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      h3 {
        font-size: 18px;
        font-weight: 800;
        color: #1b2559;
        margin: 0;
      }

      .chart-legend {
        display: flex;
        gap: 20px;
        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 12px;
          color: #a3aed0;
          font-weight: 700;
          .dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            &.blue {
              background: #4318ff;
            }
            &.green {
              background: #05cd99;
            }
          }
        }
      }
    }

    .chart-content {
      height: 280px;
    }
  }

  .side-chart-card {
    background: #fff;
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.12);

    h3 {
      font-size: 18px;
      font-weight: 800;
      color: #1b2559;
      margin-bottom: 24px;
    }

    .donut-chart-wrapper {
      height: 260px;
      .donut-chart {
        height: 100%;
      }
    }
  }

  .product-list-card {
    background: #fff;
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0px 18px 40px rgba(112, 144, 176, 0.12);

    .card-header {
      margin-bottom: 20px;
      h3 {
        font-size: 18px;
        font-weight: 800;
        color: #1b2559;
        margin: 0;
      }
    }

    .product-list {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .product-item {
        display: flex;
        align-items: center;
        gap: 12px;

        .product-img {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          object-fit: cover;
        }

        .product-info {
          flex: 1;
          h4 {
            font-size: 14px;
            font-weight: 700;
            color: #1b2559;
            margin: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            max-width: 150px;
          }
          p {
            font-size: 12px;
            color: #a3aed0;
            margin: 0;
          }
        }

        .product-count {
          .count-tag {
            background: #f4f7fe;
            color: #4318ff;
            padding: 4px 8px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
          }
        }
      }
    }
  }

  @media screen and (max-width: 1200px) {
    .main-chart-card,
    .side-chart-card,
    .product-list-card,
    .stat-card.mini {
      border-radius: 16px;
    }

    .product-list-card .product-list .product-item .product-info h4 {
      max-width: 260px;
    }
  }

  @media screen and (max-width: 768px) {
    .stat-card.mini {
      padding: 16px;
    }

    .main-chart-card,
    .side-chart-card,
    .product-list-card {
      padding: 18px;
      border-radius: 14px;
    }

    .main-chart-card {
      .chart-header {
        align-items: flex-start;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 16px;

        .chart-legend {
          flex-wrap: wrap;
          gap: 10px 16px;
        }
      }

      .chart-content {
        height: 240px;
      }
    }

    .side-chart-card .donut-chart-wrapper {
      height: 220px;
    }

    .product-list-card .product-list .product-item {
      align-items: flex-start;

      .product-info {
        min-width: 0;

        h4 {
          max-width: none;
        }
      }

      .product-count {
        flex-shrink: 0;
      }
    }
  }

  @media screen and (max-width: 480px) {
    .product-list-card .product-list .product-item {
      flex-wrap: wrap;

      .product-count {
        width: 100%;
        padding-left: 60px;
      }
    }
  }
</style>
