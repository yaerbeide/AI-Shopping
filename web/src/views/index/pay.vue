<template>
  <div>
    <Header />
    <div class="pay-content">
      <div v-if="payStatus === 'pending'" class="pay-pending">
        <div class="title">订单提交并等待支付</div>
        <div class="order-details" v-if="orderDetails.length > 0">
          <h3>订单商品</h3>
          <div class="order-item" v-for="(item, index) in orderDetails" :key="index">
            <img :src="item.cover" class="item-cover" alt="商品图片" />
            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-price">单价: ¥{{ item.price }}</div>
              <div class="item-quantity">数量: {{ item.quantity }}</div>
            </div>
          </div>
        </div>
        <div class="text">支付金额</div>
        <div class="price">
          <span class="num">{{ amount }}</span>
          <span>元</span>
        </div>
        <div class="pay-choose-view" style="">
          <div class="tips">请选择任意一种支付方式</div>
          <button class="pay-btn pay-btn-active" @click="handlePay()">确认支付</button>
        </div>
      </div>

      <div v-else-if="payStatus === 'success'" class="pay-success">
        <div class="success-icon">
          <CheckCircleFilled style="font-size: 60px; color: #52c41a" />
        </div>
        <div class="title">支付成功</div>
        <div class="text">订单已支付金额</div>
        <div class="price">
          <span class="num">{{ amount }}</span>
          <span>元</span>
        </div>
        <div class="order-info">
          <p
            >订单编号: <span class="order-number">{{ orderNumber }}</span></p
          >
          <p
            >支付时间: <span class="pay-time">{{ formatDate(new Date().getTime()) }}</span></p
          >
        </div>
        <div class="action-buttons">
          <a-button type="primary" @click="goToOrderList">查看订单</a-button>
          <a-button @click="goToShopping" style="margin-left: 16px">继续购物</a-button>
        </div>
      </div>

      <div v-else-if="payStatus === 'failed'" class="pay-failed">
        <div class="failed-icon">
          <CloseCircleFilled style="font-size: 60px; color: #f5222d" />
        </div>
        <div class="title">支付失败</div>
        <div class="text">支付过程中出现问题</div>
        <div class="error-message">{{ errorMessage }}</div>
        <div class="action-buttons">
          <a-button type="primary" @click="retryPayment">重新支付</a-button>
          <a-button @click="goToOrderList" style="margin-left: 16px">查看订单</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import Header from '/@/views/index/components/header.vue';
  import { message } from 'ant-design-vue';
  import WxPayIcon from '/@/assets/images/wx-pay-icon.svg';
  import AliPayIcon from '/@/assets/images/ali-pay-icon.svg';
  import { payOrderApi } from '/@/api/order';
  import { useRoute, useRouter } from 'vue-router';
  import { ref, onMounted } from 'vue';
  import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons-vue';

  const route = useRoute();
  const router = useRouter();

  let ddlTime = ref();
  let amount = ref();
  let orderNumber = ref('');
  let payStatus = ref('pending');
  let errorMessage = ref('');
  let orderDetails = ref([]);

  onMounted(() => {
    amount.value = route.query.amount;
    orderNumber.value = route.query.orderNum;
    ddlTime.value = formatDate(new Date().getTime());

    // 获取订单商品详情
    try {
      const checkoutItems = localStorage.getItem('checkoutItems');
      if (checkoutItems) {
        orderDetails.value = JSON.parse(checkoutItems);
        console.log('支付页面获取到的商品信息:', orderDetails.value);
      }
    } catch (err) {
      console.error('获取订单商品信息失败:', err);
    }
  });

  const handlePay = () => {
    // 获取订单信息
    const orderNum = route.query.orderNum;

    // 检查订单号是否包含多个订单（格式：timestamp_id1_id2）
    const orderParts = orderNum.toString().split('_');
    if (orderParts.length > 1) {
      // 这是一个批量订单，包含多个子订单
      console.log('检测到批量订单，订单号格式:', orderNum);

      // 创建一个数组来保存所有支付Promise
      const payPromises = [];

      // 处理主订单号和所有子订单号
      let mainOrderNum = orderParts[0];

      // 为每个子订单创建支付请求
      for (let i = 0; i < orderParts.length; i++) {
        // 构建完整的子订单号
        let subOrderNum;
        if (i === 0) {
          // 主订单号部分
          subOrderNum = mainOrderNum;
        } else {
          // 子订单部分 (timestamp_id1, timestamp_id1_id2, 等)
          subOrderNum = orderParts.slice(0, i + 1).join('_');
        }

        // 跳过空订单号
        if (!subOrderNum) continue;

        console.log(`准备支付子订单: ${subOrderNum}`);

        // 为每个子订单创建FormData
        const formData = new FormData();
        formData.append('orderNumber', subOrderNum);
        formData.append('status', '2');
        formData.append('payTime', new Date().getTime().toString());

        // 添加支付请求到数组
        payPromises.push(
          payOrderApi(formData)
            .then((res) => {
              console.log(`子订单 ${subOrderNum} 支付响应:`, res);
              return { success: true, orderNum: subOrderNum, response: res };
            })
            .catch((err) => {
              console.error(`子订单 ${subOrderNum} 支付失败:`, err);
              return { success: false, orderNum: subOrderNum, error: err };
            }),
        );
      }

      // 显示加载状态
      message.loading({ content: '正在处理支付...', key: 'payOrder' });

      // 等待所有支付请求完成
      Promise.all(payPromises).then((results) => {
        // 检查所有支付结果
        const successResults = results.filter((r) => r.success);
        const failedResults = results.filter((r) => !r.success);

        console.log('所有支付结果:', results);
        console.log('成功支付数:', successResults.length);
        console.log('失败支付数:', failedResults.length);

        if (successResults.length > 0) {
          // 至少有一个支付成功
          message.success({ content: `成功支付 ${successResults.length} 个订单`, key: 'payOrder' });

          // 支付成功后清空购物车中已购买的商品
          clearPurchasedItems();

          // 更新UI状态为支付成功
          payStatus.value = 'success';
        } else {
          // 所有支付都失败了
          message.error({ content: '所有订单支付失败', key: 'payOrder' });

          // 更新UI状态为支付失败
          payStatus.value = 'failed';
          errorMessage.value = '所有订单支付处理失败，请稍后重试';
        }
      });
    } else {
      // 单个订单的处理逻辑
      const formData = new FormData();
      formData.append('orderNumber', orderNum);
      formData.append('status', '2');
      formData.append('payTime', new Date().getTime().toString());

      // 添加日志
      console.log('提交单个订单支付数据:', {
        orderNumber: orderNum,
        status: '2',
        payTime: formData.get('payTime'),
      });

      message.loading({ content: '正在处理支付...', key: 'payOrder' });

      payOrderApi(formData)
        .then((res) => {
          console.log('支付响应:', res);
          message.success({ content: '订单支付成功', key: 'payOrder' });

          // 支付成功后清空购物车中已购买的商品
          clearPurchasedItems();

          // 更新UI状态为支付成功
          payStatus.value = 'success';
        })
        .catch((err) => {
          console.error('支付失败:', err);
          message.error({ content: err?.message || err?.msg || '支付失败，请稍后重试', key: 'payOrder' });

          // 更新UI状态为支付失败
          payStatus.value = 'failed';
          errorMessage.value = err?.message || err?.msg || '支付处理出现问题，请稍后重试';
        });
    }
  };

  // 清空购物车中已购买的商品
  const clearPurchasedItems = () => {
    try {
      console.log('开始清除已购买商品...');
      // 从本地存储获取购物车数据
      const cartData = localStorage.getItem('cart');
      if (cartData) {
        const cart = JSON.parse(cartData);
        console.log('当前购物车商品:', cart);

        // 获取结算的商品ID列表
        const checkoutItems = localStorage.getItem('checkoutItems');
        if (checkoutItems) {
          const purchasedItems = JSON.parse(checkoutItems);
          console.log('已购买商品:', purchasedItems);

          // 获取已购买的商品ID (注意：这里的ID可能是字符串或数字类型)
          const purchasedIds = purchasedItems.map((item) => item.id);
          console.log('已购买商品ID:', purchasedIds);

          // 过滤掉已购买的商品 (使用双等号进行比较，避免类型问题)
          const newCart = cart.filter((item) => {
            // 比较两个ID是否相等，转换为字符串进行比较，避免类型不匹配
            const itemId = String(item.id);
            return !purchasedIds.some((id) => String(id) === itemId);
          });

          console.log('过滤后的购物车:', newCart);

          // 更新购物车
          localStorage.setItem('cart', JSON.stringify(newCart));
          console.log('购物车已更新');
        }
      }

      // 清除结算商品缓存，支付成功后再清除
      localStorage.removeItem('checkoutItems');
      console.log('结算商品缓存已清除');

      console.log('已清除购物车中的已购买商品');
    } catch (error) {
      console.error('清除购物车商品失败:', error);
    }
  };

  // 导航到订单列表
  const goToOrderList = () => {
    router.push('/index/usercenter/orderView');
  };

  // 继续购物
  const goToShopping = () => {
    router.push('/');
  };

  // 重试支付
  const retryPayment = () => {
    payStatus.value = 'pending';
  };

  const formatDate = (time, format = 'YY-MM-DD hh:mm:ss') => {
    const date = new Date(time);

    const year = date.getFullYear(),
      month = date.getMonth() + 1,
      day = date.getDate(),
      hour = date.getHours(),
      min = date.getMinutes(),
      sec = date.getSeconds();
    const preArr = Array.apply(null, Array(10)).map(function (elem, index) {
      return '0' + index;
    });

    const newTime = format
      .replace(/YY/g, year)
      .replace(/MM/g, preArr[month] || month)
      .replace(/DD/g, preArr[day] || day)
      .replace(/hh/g, preArr[hour] || hour)
      .replace(/mm/g, preArr[min] || min)
      .replace(/ss/g, preArr[sec] || sec);

    return newTime;
  };
</script>

<style scoped lang="less">
  .flex-view {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
  }

  .pay-content {
    position: relative;
    margin: 120px auto 0;
    width: 600px;
    background: #fff;
    overflow: hidden;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    padding: 30px;

    // 订单商品详情样式
    .order-details {
      background-color: #f8f9fa;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 20px;

      h3 {
        font-size: 16px;
        color: #333;
        margin-bottom: 15px;
        padding-bottom: 8px;
        border-bottom: 1px solid #eaeaea;
      }

      .order-item {
        display: flex;
        margin-bottom: 15px;
        padding-bottom: 15px;
        border-bottom: 1px dashed #eaeaea;

        &:last-child {
          margin-bottom: 0;
          padding-bottom: 0;
          border-bottom: none;
        }

        .item-cover {
          width: 60px;
          height: 60px;
          object-fit: cover;
          border-radius: 4px;
          margin-right: 15px;
        }

        .item-info {
          flex: 1;

          .item-title {
            font-weight: 500;
            color: #333;
            margin-bottom: 5px;
            font-size: 14px;
          }

          .item-price,
          .item-quantity {
            color: #666;
            font-size: 12px;
          }
        }
      }
    }

    .title {
      color: #152844;
      font-weight: 500;
      font-size: 24px;
      line-height: 1.4;
      text-align: center;
      margin-bottom: 20px;
    }

    .text {
      height: 22px;
      line-height: 22px;
      font-size: 14px;
      text-align: center;
      color: #152844;
      margin-bottom: 10px;
    }

    .price {
      color: #ff8a00;
      font-weight: 500;
      font-size: 16px;
      height: 36px;
      line-height: 36px;
      text-align: center;
      margin-bottom: 20px;

      .num {
        font-size: 28px;
      }
    }

    // 支付成功样式
    .pay-success {
      text-align: center;

      .success-icon {
        margin-bottom: 20px;
      }

      .order-info {
        background-color: #f9f9f9;
        border-radius: 6px;
        padding: 16px;
        text-align: left;
        margin: 20px 0;

        p {
          margin: 5px 0;
          color: #666;

          .order-number,
          .pay-time {
            color: #333;
            font-weight: 500;
          }
        }
      }

      .action-buttons {
        margin-top: 30px;
      }
    }

    // 支付失败样式
    .pay-failed {
      text-align: center;

      .failed-icon {
        margin-bottom: 20px;
      }

      .error-message {
        color: #f5222d;
        background-color: rgba(245, 34, 45, 0.1);
        padding: 10px;
        border-radius: 4px;
        margin: 20px 0;
      }

      .action-buttons {
        margin-top: 30px;
      }
    }

    .pay-choose-view {
      margin-top: 24px;

      .tips {
        color: #6f6f6f;
        font-size: 14px;
        line-height: 22px;
        height: 22px;
        text-align: center;
        margin: 16px 0 24px;
      }

      .pay-btn {
        cursor: pointer;
        background: #c3c9d5;
        border-radius: 32px;
        width: 104px;
        height: 32px;
        line-height: 32px;
        border: none;
        outline: none;
        font-size: 14px;
        color: #fff;
        text-align: center;
        display: block;
        margin: 0 auto;
      }

      .pay-btn-active {
        background: #4684e2;
      }
    }
  }
</style>
