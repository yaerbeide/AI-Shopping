<template>
  <div class="cart-container">
    <Header />

    <main class="cart-page">
      <section class="cart-hero">
        <div>
          <div class="eyebrow">&#x8D2D;&#x7269;&#x8F66;</div>
          <h1>&#x6211;&#x7684;&#x8D2D;&#x7269;&#x8F66;</h1>
          <p>&#x786E;&#x8BA4;&#x60F3;&#x8981;&#x5E26;&#x8D70;&#x7684;&#x5546;&#x54C1;&#xFF0C;&#x6570;&#x91CF;&#x548C;&#x91D1;&#x989D;&#x4F1A;&#x5B9E;&#x65F6;&#x540C;&#x6B65;&#x3002;</p>
        </div>
        <div class="hero-stat">
          <strong>{{ cartItems.length }}</strong>
          <span>&#x4EF6;&#x5546;&#x54C1;</span>
        </div>
      </section>

      <section v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-icon">
          <shopping-cart-outlined />
        </div>
        <h2>&#x8D2D;&#x7269;&#x8F66;&#x8FD8;&#x662F;&#x7A7A;&#x7684;</h2>
        <p>&#x5148;&#x53BB;&#x6311;&#x9009;&#x559C;&#x6B22;&#x7684;&#x5546;&#x54C1;&#xFF0C;&#x518D;&#x56DE;&#x6765;&#x7EDF;&#x4E00;&#x7ED3;&#x7B97;&#x3002;</p>
        <a-button type="primary" @click="goShopping">&#x53BB;&#x9009;&#x8D2D;</a-button>
      </section>

      <section v-else class="cart-layout">
        <div class="cart-list-card">
          <div class="list-toolbar">
            <a-checkbox v-model:checked="allChecked" @change="handleCheckAllChange">&#x5168;&#x9009;</a-checkbox>
            <button type="button" class="ghost-action" @click="clearCart">
              <delete-outlined />
              &#x6E05;&#x7A7A;&#x8D2D;&#x7269;&#x8F66;
            </button>
          </div>

          <div class="cart-items">
            <article v-for="(item, index) in cartItems" :key="item.id || index" class="cart-item" :class="{ checked: item.checked }">
              <a-checkbox v-model:checked="item.checked" @change="updateTotalPrice" class="item-check" />

              <div class="image-frame" @click="goDetail(item)">
                <img :src="item.cover" :alt="item.title" />
              </div>

              <div class="item-info">
                <div class="item-topline">
                  <span>&#x5DF2;&#x52A0;&#x5165;</span>
                  <button type="button" @click="removeItem(index)">
                    <close-outlined />
                  </button>
                </div>
                <h3 @click="goDetail(item)">{{ item.title }}</h3>
                <p>{{ item.spec || defaultSpecText }}</p>
                <div class="mobile-price">&yen;{{ formatPrice(item.price) }}</div>
              </div>

              <div class="price-col">
                <span>&#x5355;&#x4EF7;</span>
                <strong>&yen;{{ formatPrice(item.price) }}</strong>
              </div>

              <div class="quantity-col">
                <span>&#x6570;&#x91CF;</span>
                <a-input-number v-model:value="item.quantity" :min="1" :max="99" @change="() => updateItemTotal(item)" />
              </div>

              <div class="total-col">
                <span>&#x5C0F;&#x8BA1;</span>
                <strong>&yen;{{ formatPrice(item.price * item.quantity) }}</strong>
              </div>
            </article>
          </div>
        </div>

        <aside class="summary-card">
          <div class="summary-head">
            <span>&#x8BA2;&#x5355;&#x6458;&#x8981;</span>
            <h2>&#x7ED3;&#x7B97;&#x4FE1;&#x606F;</h2>
          </div>

          <div class="summary-row">
            <span>&#x5DF2;&#x9009;</span>
            <strong>{{ selectedCount }} &#x4EF6;</strong>
          </div>
          <div class="summary-row">
            <span>&#x5546;&#x54C1;&#x6570;</span>
            <strong>{{ cartItems.length }} &#x4EF6;</strong>
          </div>
          <div class="summary-row muted">
            <span>&#x914D;&#x9001;</span>
            <strong>&#x7A0D;&#x540E;&#x786E;&#x8BA4;</strong>
          </div>

          <div class="summary-total">
            <span>&#x5408;&#x8BA1;</span>
            <strong>&yen;{{ totalPrice.toFixed(2) }}</strong>
          </div>

          <a-button type="primary" size="large" block @click="checkout" :disabled="selectedCount === 0">
            &#x53BB;&#x7ED3;&#x7B97;
          </a-button>
          <button type="button" class="continue-button" @click="goShopping">&#x7EE7;&#x7EED;&#x9009;&#x8D2D;</button>
        </aside>
      </section>
    </main>
  </div>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { CloseOutlined, DeleteOutlined, ShoppingCartOutlined } from '@ant-design/icons-vue';
  import Header from './components/header.vue';

  const router = useRouter();
  const cartItems = ref([]);
  const allChecked = ref(false);
  const totalPrice = ref(0);
  const defaultSpecText = '\u9ed8\u8ba4\u89c4\u683c';

  const selectedCount = computed(() => cartItems.value.filter((item) => item.checked).length);

  onMounted(() => {
    loadCartItems();
  });

  const formatPrice = (value) => Number(value || 0).toFixed(2);

  const loadCartItems = () => {
    const storedCart = localStorage.getItem('cart');
    if (storedCart) {
      try {
        const parsed = JSON.parse(storedCart);
        cartItems.value = parsed.map((item) => ({
          ...item,
          id: item.id,
          quantity: item.quantity || 1,
          checked: typeof item.checked === 'boolean' ? item.checked : true,
        }));
      } catch (error) {
        console.error('Failed to parse cart data', error);
        cartItems.value = [];
        saveCartToStorage();
      }
    } else {
      cartItems.value = [];
      saveCartToStorage();
    }
    updateTotalPrice();
  };

  const updateItemTotal = (item) => {
    item.total = Number(item.price || 0) * Number(item.quantity || 1);
    updateTotalPrice();
  };

  const updateTotalPrice = () => {
    totalPrice.value = cartItems.value
      .filter((item) => item.checked)
      .reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 1), 0);
    checkAllChecked();
    saveCartToStorage();
  };

  const checkAllChecked = () => {
    allChecked.value = cartItems.value.length > 0 && cartItems.value.every((item) => item.checked);
  };

  const handleCheckAllChange = (e) => {
    const checked = e.target.checked;
    cartItems.value.forEach((item) => {
      item.checked = checked;
    });
    updateTotalPrice();
  };

  const removeItem = (index) => {
    cartItems.value.splice(index, 1);
    updateTotalPrice();
    saveCartToStorage();
    message.success('\u5df2\u79fb\u51fa\u8d2d\u7269\u8f66');
  };

  const clearCart = () => {
    cartItems.value = [];
    totalPrice.value = 0;
    allChecked.value = false;
    saveCartToStorage();
    message.success('\u8d2d\u7269\u8f66\u5df2\u6e05\u7a7a');
  };

  const saveCartToStorage = () => {
    const validCartItems = cartItems.value.map((item) => ({
      id: item.id,
      title: item.title,
      price: item.price,
      quantity: item.quantity || 1,
      spec: item.spec || defaultSpecText,
      cover: item.cover,
      checked: item.checked,
    }));
    localStorage.setItem('cart', JSON.stringify(validCartItems));
  };

  const goShopping = () => {
    router.push({ name: 'portal' });
  };

  const goDetail = (item) => {
    if (item.id) {
      router.push({ name: 'detail', query: { id: item.id } });
    }
  };

  const checkout = () => {
    const selectedItems = cartItems.value.filter((item) => item.checked);
    if (selectedItems.length === 0) {
      message.warning('\u8bf7\u5148\u9009\u62e9\u5546\u54c1');
      return;
    }

    const checkoutData = selectedItems.map((item) => ({
      id: item.id,
      title: item.title,
      price: item.price,
      quantity: item.quantity || 1,
      spec: item.spec || defaultSpecText,
      cover: item.cover,
      checked: true,
    }));

    localStorage.setItem('checkoutItems', JSON.stringify(checkoutData));
    router.push({ name: 'confirm' });
  };
</script>

<style scoped lang="less">
  .cart-container {
    min-height: 100vh;
    background:
      radial-gradient(circle at 10% 10%, rgba(255, 209, 0, 0.18), transparent 24%),
      linear-gradient(180deg, #f6f8fa 0%, #fff 52%);
  }

  .cart-page {
    width: min(1320px, calc(100% - 48px));
    margin: 0 auto;
    padding: 112px 0 80px;
  }

  .cart-hero {
    min-height: 190px;
    border-radius: 30px;
    padding: 38px;
    background:
      linear-gradient(90deg, rgba(24, 27, 34, 0.94), rgba(24, 27, 34, 0.66)),
      url('/src/assets/images/banner-02.webp') center/cover;
    color: #fff;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 24px;
    box-shadow: 0 24px 56px rgba(15, 23, 42, 0.16);
  }

  .cart-hero .eyebrow {
    color: #ffd100;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.1em;
  }

  .cart-hero h1 {
    margin: 8px 0;
    color: #fff;
    font-size: 42px;
    font-weight: 900;
  }

  .cart-hero p {
    margin: 0;
    color: rgba(255, 255, 255, 0.76);
    font-weight: 600;
  }

  .hero-stat {
    width: 118px;
    height: 96px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.14);
    backdrop-filter: blur(12px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .hero-stat strong {
    color: #ffd100;
    font-size: 34px;
    line-height: 1;
  }

  .hero-stat span {
    margin-top: 8px;
    color: rgba(255, 255, 255, 0.78);
    font-size: 12px;
  }

  .cart-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 340px;
    gap: 28px;
    margin-top: 28px;
    align-items: start;
  }

  .cart-list-card,
  .summary-card,
  .empty-cart {
    background: #fff;
    border-radius: 26px;
    box-shadow: 0 18px 48px rgba(15, 23, 42, 0.07);
  }

  .list-toolbar {
    min-height: 70px;
    border-bottom: 1px solid #edf0f5;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .ghost-action,
  .continue-button {
    border: 0;
    border-radius: 999px;
    background: #f5f6f8;
    color: #24272d;
    height: 38px;
    padding: 0 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-weight: 800;
    cursor: pointer;
  }

  .ghost-action:hover,
  .continue-button:hover {
    background: #ffd100;
  }

  .cart-items {
    padding: 6px 24px 24px;
  }

  .cart-item {
    display: grid;
    grid-template-columns: 34px 108px minmax(0, 1fr) 104px 116px 120px;
    gap: 18px;
    align-items: center;
    padding: 22px 0;
    border-bottom: 1px solid #edf0f5;
  }

  .cart-item:last-child {
    border-bottom: 0;
  }

  .cart-item.checked .image-frame {
    box-shadow: inset 0 0 0 2px #ffd100;
  }

  .item-check {
    justify-self: center;
  }

  .image-frame {
    width: 108px;
    height: 108px;
    max-width: 108px;
    max-height: 108px;
    border-radius: 22px;
    background: #f4f5f7;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
  }

  .image-frame img {
    display: block;
    width: 86%;
    height: 86%;
    max-width: 92px;
    max-height: 92px;
    object-fit: contain;
  }

  .item-info {
    min-width: 0;
  }

  .item-topline {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    color: #9aa0aa;
    font-size: 12px;
    font-weight: 800;
  }

  .item-topline button {
    width: 28px;
    height: 28px;
    border: 0;
    border-radius: 50%;
    background: #f5f6f8;
    color: #777e89;
    display: none;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }

  .item-info h3 {
    margin: 8px 0 6px;
    color: #24272d;
    font-size: 17px;
    font-weight: 900;
    line-height: 1.45;
    cursor: pointer;
  }

  .item-info p {
    margin: 0;
    color: #8b929d;
    font-weight: 600;
  }

  .mobile-price {
    display: none;
  }

  .price-col,
  .quantity-col,
  .total-col {
    display: flex;
    flex-direction: column;
    gap: 8px;
    align-items: center;
  }

  .price-col span,
  .quantity-col span,
  .total-col span {
    color: #9aa0aa;
    font-size: 12px;
    font-weight: 800;
  }

  .price-col strong,
  .quantity-col strong,
  .total-col strong {
    color: #24272d;
    font-size: 17px;
    font-weight: 900;
  }

  .total-col strong {
    color: #ff4d57;
  }

  .summary-card {
    position: sticky;
    top: 104px;
    padding: 26px;
  }

  .summary-head span {
    color: #9aa0aa;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.08em;
  }

  .summary-head h2 {
    margin: 6px 0 22px;
    color: #24272d;
    font-size: 26px;
    font-weight: 900;
  }

  .summary-row,
  .summary-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #edf0f5;
    color: #7d8490;
    font-weight: 700;
    gap: 16px;
  }

  .summary-row strong {
    color: #24272d;
  }

  .summary-row.muted strong {
    color: #9aa0aa;
  }

  .summary-total {
    border-bottom: 0;
    margin: 8px 0 18px;
  }

  .summary-total strong {
    color: #ff4d57;
    font-size: 28px;
    font-weight: 900;
    text-align: right;
  }

  .continue-button {
    width: 100%;
    margin-top: 12px;
  }

  .empty-cart {
    margin-top: 28px;
    min-height: 420px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
  }

  .empty-icon {
    width: 76px;
    height: 76px;
    border-radius: 26px;
    background: #ffd100;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #24272d;
    font-size: 32px;
  }

  .empty-cart h2 {
    margin: 20px 0 8px;
    color: #24272d;
    font-size: 28px;
    font-weight: 900;
  }

  .empty-cart p {
    color: #8b929d;
    margin-bottom: 22px;
  }

  :deep(.ant-btn-primary) {
    height: 46px;
    border-radius: 999px;
    background: #24272d;
    border-color: #24272d;
    font-weight: 900;
    box-shadow: 0 12px 24px rgba(36, 39, 45, 0.18);
  }

  :deep(.ant-input-number) {
    width: 88px;
    border-radius: 999px;
    background: #f5f6f8;
    border-color: transparent;
  }

  @media (max-width: 1100px) {
    .cart-layout {
      grid-template-columns: 1fr;
    }

    .summary-card {
      position: static;
    }
  }

  @media (max-width: 760px) {
    .cart-page {
      width: min(100% - 28px, 1320px);
      padding-top: 96px;
    }

    .cart-hero {
      padding: 26px;
      align-items: flex-start;
      flex-direction: column;
    }

    .cart-hero h1 {
      font-size: 34px;
    }

    .cart-item {
      grid-template-columns: 28px 90px minmax(0, 1fr);
      gap: 14px;
      align-items: start;
    }

    .price-col,
    .total-col {
      display: none;
    }

    .quantity-col {
      grid-column: 3;
      align-items: flex-start;
    }

    .image-frame {
      width: 90px;
      height: 90px;
      max-width: 90px;
      max-height: 90px;
      border-radius: 18px;
    }

    .image-frame img {
      max-width: 78px;
      max-height: 78px;
    }

    .item-topline button,
    .mobile-price {
      display: inline-flex;
    }

    .mobile-price {
      margin-top: 8px;
      color: #ff4d57;
      font-size: 18px;
      font-weight: 900;
    }

    .list-toolbar {
      padding: 0 16px;
    }

    .cart-items {
      padding: 4px 16px 18px;
    }
  }
</style>
