<template>
  <div class="content-list">
    <div class="list-title">我的购物车</div>
    <div role="tablist" class="list-tabs-view flex-view"> </div>
    <div class="list-content">
      <div class="collect-thing-view">
        <div class="thing-list flex-view">
          <div v-if="cartItems.length === 0" class="empty-cart">
            <a-empty description="购物车还是空的，快去选购商品吧~">
              <a-button type="primary" @click="goShopping">去购物</a-button>
            </a-empty>
          </div>
          <div class="thing-item item-column-3" v-for="(item, index) in cartItems" :key="index">
            <div class="remove" @click="handleRemove(item, index)">移出</div>
            <div class="img-view" @click="handleClickItem(item)">
              <img :src="item.cover" />
            </div>
            <div class="info-view">
              <h3 class="thing-name">{{ item.title }}</h3>
              <p class="price">¥{{ item.price }}</p>
              <div class="quantity">
                <span>数量: </span>
                <a-input-number
                  v-model:value="item.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  @change="() => updateItemQuantity(item, index)"
                />
              </div>
              <p class="subtotal">小计: ¥{{ (item.price * item.quantity).toFixed(2) }}</p>
            </div>
          </div>
        </div>
        <div class="cart-footer" v-if="cartItems.length > 0">
          <div class="total">总计: ¥{{ totalPrice.toFixed(2) }}</div>
          <a-button type="primary" @click="checkout">去结算</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { message } from 'ant-design-vue';
  import { BASE_URL } from '/@/store/constants';
  import { useUserStore } from '/@/store';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();

  let cartItems = ref([]);
  let totalPrice = ref(0);

  onMounted(() => {
    loadCartItems();
  });

  const handleClickItem = (record) => {
    let text = router.resolve({ name: 'detail', query: { id: record.id } });
    window.open(text.href, '_blank');
  };

  const handleRemove = (item, index) => {
    cartItems.value.splice(index, 1);
    updateTotalPrice();
    saveCartToStorage();
    message.success('商品已从购物车中移除');
  };

  const loadCartItems = () => {
    // 从本地存储加载购物车数据
    const storedCart = localStorage.getItem('cart');
    if (storedCart) {
      cartItems.value = JSON.parse(storedCart);
      updateTotalPrice();
    }
  };

  const updateItemQuantity = (item, index) => {
    cartItems.value[index].quantity = item.quantity;
    updateTotalPrice();
    saveCartToStorage();
  };

  const updateTotalPrice = () => {
    totalPrice.value = cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
  };

  const saveCartToStorage = () => {
    localStorage.setItem('cart', JSON.stringify(cartItems.value));
  };

  const goShopping = () => {
    router.push({ name: 'portal' });
  };

  const checkout = () => {
    if (cartItems.value.length === 0) {
      message.warning('购物车为空，请先添加商品');
      return;
    }

    // 将商品传递到确认订单页面
    localStorage.setItem('checkoutItems', JSON.stringify(cartItems.value));
    router.push({ name: 'confirm' });
  };
</script>
<style scoped lang="less">
  .flex-view {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
  }

  .content-list {
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;

    .list-title {
      color: #152844;
      font-weight: 600;
      font-size: 18px;
      line-height: 24px;
      height: 24px;
      margin-bottom: 4px;
    }

    .list-tabs-view {
      position: relative;
      border-bottom: 1px solid #cedce4;
      height: 12px;
      line-height: 42px;
    }
  }

  .empty-cart {
    width: 100%;
    padding: 40px 0;
  }

  .cart-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 20px;
    padding: 10px 0;
    border-top: 1px solid #eee;

    .total {
      font-size: 16px;
      font-weight: bold;
      color: #ff4d4f;
      margin-right: 20px;
    }
  }

  .thing-list {
    -ms-flex-wrap: wrap;
    flex-wrap: wrap;
    -webkit-box-pack: start;
    -ms-flex-pack: start;
    justify-content: flex-start;

    .thing-item {
      position: relative;
      -webkit-box-flex: 1;
      -ms-flex: 1;
      flex: 1;
      margin-right: 20px;
      min-width: 255px;
      max-width: 255px;
      height: fit-content;
      border-radius: 4px;
      overflow: hidden;
      margin-top: 16px;
      cursor: pointer;

      .remove {
        position: absolute;
        right: 8px;
        top: 8px;
        width: 48px;
        height: 20px;
        text-align: center;
        line-height: 20px;
        color: #fff;
        background: #a1adc5;
        border-radius: 32px;
        cursor: pointer;
      }

      .img-view {
        background: #eaf1f5;
        font-size: 0;
        text-align: center;
        height: 156px;
        padding: 8px 0;

        img {
          max-width: 100%;
          height: 100%;
          display: block;
          margin: 0 auto;
          border-radius: 4px;
          -webkit-box-sizing: border-box;
          box-sizing: border-box;
        }
      }

      .info-view {
        background: #f6f9fb;
        text-align: center;
        height: auto;
        min-height: 140px;
        overflow: hidden;
        padding: 0 16px 16px;

        h3 {
          color: #1c355a;
          font-weight: 500;
          font-size: 16px;
          line-height: 20px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          margin: 12px 0 8px;
        }

        .price {
          color: #ff4d4f;
          font-size: 16px;
          font-weight: bold;
          margin: 8px 0;
        }

        .quantity {
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 8px 0;

          span {
            margin-right: 8px;
          }
        }

        .subtotal {
          color: #ff4d4f;
          font-size: 14px;
          margin-top: 8px;
        }
      }
    }
  }
</style>
