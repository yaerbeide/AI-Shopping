<template>
  <div class="content-list">
    <div class="list-title">我的购物车</div>
    <div role="tablist" class="list-tabs-view flex-view"> </div>
    <div class="list-content">
      <div class="collect-thing-view">
        <div v-if="cartItems.length === 0" class="empty-cart">
          <a-empty description="购物车还是空的，快去选购商品吧~">
            <a-button type="primary" @click="goShopping">去购物</a-button>
          </a-empty>
        </div>
        <div v-else>
          <!-- 添加全选和操作栏 -->
          <div class="cart-header">
            <div class="select-all">
              <a-checkbox v-model:checked="selectAll" @change="handleSelectAllChange">全选</a-checkbox>
            </div>
            <div class="batch-actions" v-if="getSelectedItems().length > 0">
              <a-button danger type="primary" size="small" @click="removeSelectedItems"> 删除选中商品 </a-button>
            </div>
          </div>

          <div class="thing-list flex-view">
            <div class="thing-item item-column-3" v-for="(item, index) in cartItems" :key="index">
              <!-- 添加选择框 -->
              <div class="item-select">
                <a-checkbox v-model:checked="item.selected" @change="updateSelectAll" />
              </div>
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
            <div class="cart-summary">
              <div class="selected-count"
                >已选择 <span class="count-num">{{ getSelectedItems().length }}</span> 件商品</div
              >
              <div class="total">总计: ¥{{ getSelectedTotal().toFixed(2) }}</div>
            </div>
            <a-button type="primary" @click="checkout" :disabled="getSelectedItems().length === 0"> 去结算 </a-button>
          </div>
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
  let selectAll = ref(false);

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
    updateSelectAll();
  };

  const loadCartItems = () => {
    // 从本地存储加载购物车数据
    const storedCart = localStorage.getItem('cart');
    if (storedCart) {
      const items = JSON.parse(storedCart);
      // 为每个商品添加selected属性
      cartItems.value = items.map((item) => ({
        ...item,
        selected: true, // 默认全选
        quantity: item.quantity || 1,
      }));
      updateTotalPrice();
      updateSelectAll(); // 更新全选状态
    }
  };

  const updateItemQuantity = (item, index) => {
    cartItems.value[index].quantity = item.quantity;
    updateTotalPrice();
    saveCartToStorage();
  };

  const updateTotalPrice = () => {
    totalPrice.value = getSelectedTotal();
  };

  // 获取选中商品的总价
  const getSelectedTotal = () => {
    return cartItems.value.reduce((sum, item) => {
      return sum + (item.selected ? item.price * item.quantity : 0);
    }, 0);
  };

  // 获取选中的商品
  const getSelectedItems = () => {
    return cartItems.value.filter((item) => item.selected);
  };

  // 全选/取消全选
  const handleSelectAllChange = (e) => {
    const checked = e.target.checked;
    cartItems.value.forEach((item) => {
      item.selected = checked;
    });
    updateTotalPrice();
    saveCartToStorage();
  };

  // 更新全选状态
  const updateSelectAll = () => {
    selectAll.value = cartItems.value.length > 0 && cartItems.value.every((item) => item.selected);
    updateTotalPrice();
  };

  // 删除选中的商品
  const removeSelectedItems = () => {
    if (getSelectedItems().length === 0) {
      message.warning('请先选择要删除的商品');
      return;
    }

    cartItems.value = cartItems.value.filter((item) => !item.selected);
    updateTotalPrice();
    saveCartToStorage();
    message.success('已删除选中的商品');
  };

  const saveCartToStorage = () => {
    localStorage.setItem('cart', JSON.stringify(cartItems.value));
  };

  const goShopping = () => {
    router.push({ name: 'portal' });
  };

  const checkout = () => {
    const selectedItems = getSelectedItems();

    if (selectedItems.length === 0) {
      message.warning('请先选择要结算的商品');
      return;
    }

    // 将选中的商品传递到确认订单页面
    localStorage.setItem('checkoutItems', JSON.stringify(selectedItems));
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

  .cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
    margin-bottom: 10px;
  }

  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding: 10px 0;
    border-top: 1px solid #eee;

    .cart-summary {
      display: flex;
      align-items: center;

      .selected-count {
        margin-right: 20px;

        .count-num {
          color: #ff4d4f;
          font-weight: bold;
        }
      }

      .total {
        font-size: 16px;
        font-weight: bold;
        color: #ff4d4f;
      }
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

      .item-select {
        position: absolute;
        left: 8px;
        top: 8px;
        z-index: 1;
      }

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
