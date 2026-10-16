<template>
  <div class="confirm-page">
    <Header />
    <section class="confirm-container">
      <div class="page-title">
        <h2>确认订单</h2>
      </div>

      <div class="confirm-content">
        <div class="left-content">
          <!-- 商品信息卡片 -->
          <div class="content-card">
            <div class="card-header">
              <h3>商品信息</h3>
            </div>
            <div class="card-body">
              <!-- 使用循环显示所有商品 -->
              <div v-for="(item, index) in pageData.items" :key="index" class="product-item">
                <div class="product-image">
                  <img :src="item.cover" alt="商品图片" />
                </div>
                <div class="product-details">
                  <div class="product-title">{{ item.title }}</div>
                  <div class="product-price">¥{{ item.price }}</div>
                </div>
                <div class="product-quantity">
                  <span class="quantity-label">数量</span>
                  <a-input-number
                    v-model:value="item.quantity"
                    :min="1"
                    :max="10"
                    @change="() => updateItemQuantity(index)"
                    size="small"
                    class="custom-input-number"
                  />
                </div>
                <div class="product-subtotal">
                  <span class="subtotal-label">小计</span>
                  <span class="subtotal-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 优惠券卡片 -->
          <div class="content-card">
            <div class="card-header">
              <h3>优惠券</h3>
              <a-button type="link" @click="showCouponModal" class="select-coupon-btn"> 选择优惠券 <i class="select-icon">›</i> </a-button>
            </div>
            <div class="card-body">
              <div v-if="pageData.selectedCoupon" class="selected-coupon">
                <div class="coupon-card" :class="{ discount: pageData.selectedCoupon.type === 2 }">
                  <div class="coupon-left">
                    <div class="coupon-amount">
                      <template v-if="pageData.selectedCoupon.type === 1">
                        <span class="amount-value">{{ pageData.selectedCoupon.amount }}</span>
                        <span class="amount-unit">元</span>
                      </template>
                      <template v-else-if="pageData.selectedCoupon.type === 2">
                        <span class="amount-value">{{ pageData.selectedCoupon.discount * 10 }}</span>
                        <span class="amount-unit">折</span>
                      </template>
                    </div>
                    <div class="coupon-threshold" v-if="pageData.selectedCoupon.threshold > 0">
                      满{{ pageData.selectedCoupon.threshold }}元可用
                    </div>
                    <div class="coupon-threshold" v-else>无门槛</div>
                  </div>
                  <div class="coupon-right">
                    <div class="coupon-info">
                      <div class="coupon-name">{{ pageData.selectedCoupon.name }}</div>
                      <div class="coupon-time" v-if="pageData.selectedCoupon.startTime && pageData.selectedCoupon.endTime">
                        有效期: {{ formatDate(pageData.selectedCoupon.startTime) }} ~ {{ formatDate(pageData.selectedCoupon.endTime) }}
                      </div>
                    </div>
                    <div class="coupon-action">
                      <a-button type="link" danger @click="removeCoupon">取消使用</a-button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="no-coupon">
                <i class="coupon-icon">🎫</i>
                <span>暂未使用优惠券，点击右上角按钮选择</span>
              </div>
            </div>
          </div>

          <!-- 备注卡片 -->
          <div class="content-card">
            <div class="card-header">
              <h3>订单备注</h3>
            </div>
            <div class="card-body">
              <a-textarea
                v-model:value="pageData.remark"
                placeholder="请输入订单备注信息，最多100字"
                :rows="3"
                :maxlength="100"
                show-count
                class="remark-textarea"
              />
            </div>
          </div>
        </div>

        <div class="right-content">
          <!-- 收货地址卡片 -->
          <div class="content-card">
            <div class="card-header">
              <h3>收货地址</h3>
              <a-button type="link" @click="handleAdd" class="add-address-btn">
                {{ pageData.receiverAddress ? '修改地址' : '新增地址' }}
              </a-button>
            </div>
            <div class="card-body">
              <div v-if="pageData.receiverAddress" class="address-info">
                <div class="address-header">
                  <div class="receiver-info">
                    <span class="receiver-name">{{ pageData.receiverName }}</span>
                    <span class="receiver-phone">{{ pageData.receiverPhone }}</span>
                  </div>
                  <div class="default-tag" v-if="pageData.isDefaultAddress">默认</div>
                </div>
                <div class="address-detail">
                  <i class="location-icon">📍</i>
                  <span>{{ pageData.receiverAddress }}</span>
                </div>
              </div>
              <div v-else class="no-address">
                <i class="address-icon">📍</i>
                <span>暂无收货地址，请点击右上角按钮添加</span>
              </div>
            </div>
          </div>

          <!-- 结算卡片 -->
          <div class="content-card checkout-card">
            <div class="card-header">
              <h3>订单结算</h3>
            </div>
            <div class="card-body">
              <div class="price-summary">
                <div class="price-item">
                  <span class="price-label">商品总价</span>
                  <span class="price-value">¥{{ pageData.amount }}</span>
                </div>
                <div class="price-item">
                  <span class="price-label">优惠券</span>
                  <span class="price-discount">-¥{{ pageData.couponDiscount.toFixed(2) }}</span>
                </div>
                <div class="price-item">
                  <span class="price-label">商品折扣</span>
                  <span class="price-value">¥0</span>
                </div>
                <div class="price-divider"></div>
                <div class="price-total">
                  <span class="total-label">应付金额</span>
                  <span class="total-value"
                    >¥{{ typeof pageData.finalAmount === 'number' ? pageData.finalAmount.toFixed(2) : pageData.finalAmount }}</span
                  >
                </div>
              </div>
              <div class="checkout-actions">
                <a-button class="back-btn" @click="handleBack">返回购物车</a-button>
                <a-button type="primary" class="checkout-btn" @click="handleJiesuan">提交订单</a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 优惠券选择弹窗 -->
    <a-modal
      v-model:visible="couponModal.visible"
      title="选择优惠券"
      @ok="handleSelectCoupon"
      @cancel="cancelSelectCoupon"
      :width="700"
      class="coupon-modal"
    >
      <a-spin :spinning="couponModal.loading">
        <div v-if="couponModal.userCoupons.length === 0" class="empty-tips">
          <i class="empty-icon">🎫</i>
          <p>暂无可用优惠券</p>
        </div>
        <div v-else class="coupon-select-list">
          <a-radio-group v-model:value="couponModal.selectedId">
            <div v-for="coupon in couponModal.userCoupons" :key="coupon.userCouponId" class="coupon-item">
              <div class="coupon-left" :class="{ discount: coupon.type === 2 }">
                <div class="coupon-amount">
                  <template v-if="coupon.type === 1">
                    <span class="amount-value">{{ coupon.amount }}</span>
                    <span class="amount-unit">元</span>
                  </template>
                  <template v-else-if="coupon.type === 2">
                    <span class="amount-value">{{ coupon.discount * 10 }}</span>
                    <span class="amount-unit">折</span>
                  </template>
                </div>
                <div class="coupon-threshold" v-if="coupon.threshold > 0">满{{ coupon.threshold }}元可用</div>
                <div class="coupon-threshold" v-else>无门槛</div>
              </div>
              <div class="coupon-right">
                <div class="coupon-info">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-time"> 有效期: {{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }} </div>
                </div>
                <div class="coupon-action">
                  <a-radio :value="coupon.userCouponId" :disabled="!isCouponAvailable(coupon)" />
                  <div v-if="!isCouponAvailable(coupon)" class="coupon-disabled-reason">
                    {{ getCouponDisabledReason(coupon) }}
                  </div>
                </div>
              </div>
              <div v-if="!isCouponAvailable(coupon)" class="coupon-mask">
                <span>不可用</span>
              </div>
            </div>
          </a-radio-group>
        </div>
      </a-spin>
    </a-modal>

    <!-- 地址弹窗 -->
    <a-modal
      v-model:visible="modal.visible"
      :title="modal.title + '收货地址'"
      @ok="handleOk"
      @cancel="handleCancel"
      class="address-modal"
      :width="600"
    >
      <!-- 地址选择模式 -->
      <div v-if="modal.editFlag && modal.addressList.length > 0">
        <div class="address-list">
          <a-radio-group v-model:value="modal.selectedAddressId" class="address-radio-group">
            <div v-for="address in modal.addressList" :key="address.id" class="address-item">
              <a-radio :value="address.id" class="address-radio">
                <div class="address-content">
                  <div class="address-header">
                    <span class="address-name">{{ address.name }}</span>
                    <span class="address-phone">{{ address.mobile }}</span>
                    <span v-if="address.default" class="address-default-tag">默认</span>
                  </div>
                  <div class="address-text">{{ address.description }}</div>
                </div>
              </a-radio>
            </div>
          </a-radio-group>
        </div>
        <div class="address-actions">
          <a-button type="primary" ghost @click="handleShowAddNew">添加新地址</a-button>
        </div>
      </div>

      <!-- 新增地址模式 -->
      <a-form v-else ref="myform" :model="modal.form" :rules="modal.rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="收货人" name="name">
          <a-input v-model:value="modal.form.name" placeholder="请输入收货人姓名" />
        </a-form-item>
        <a-form-item label="联系方式" name="mobile">
          <a-input v-model:value="modal.form.mobile" placeholder="请输入联系方式" />
        </a-form-item>
        <a-form-item label="收货地址" name="desc">
          <a-textarea v-model:value="modal.form.desc" placeholder="请输入收货地址" :rows="3" />
        </a-form-item>
        <a-form-item label="默认地址" name="default">
          <a-switch v-model:checked="modal.form.default" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { reactive, onMounted, ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import Header from '/@/views/index/components/header.vue';
  import DeleteIcon from '/@/assets/images/delete-icon.svg';
  import { createApi } from '/@/api/order';
  import { listApi as listAddressListApi, createApi as createAddressApi } from '/@/api/address';
  import { useUserStore } from '/@/store';
  import { getUserCoupons, useCoupon } from '/@/api/userCoupon';
  import { checkCouponAvailable } from '/@/api/userCoupon';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();

  const pageData = reactive({
    items: [], // 订单中的所有商品
    remark: undefined,
    amount: 0, // 商品总价
    receiverName: undefined,
    receiverPhone: undefined,
    receiverAddress: undefined,
    selectedCoupon: null, // 选中的优惠券
    couponDiscount: 0, // 优惠券折扣金额
    finalAmount: 0, // 最终支付金额
    userCouponId: null, // 用户优惠券ID
  });

  // 优惠券弹窗
  const couponModal = reactive({
    visible: false,
    loading: false,
    userCoupons: [],
    selectedId: null,
  });

  // 弹窗数据
  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    form: {
      name: undefined,
      mobile: undefined,
      desc: undefined,
      default: undefined,
    },
    rules: {
      name: [{ required: true, message: '请输入', trigger: 'change' }],
    },
    addressList: [], // 用户的地址列表
    selectedAddressId: null, // 选中的地址ID
  });

  const myform = ref();

  onMounted(() => {
    // 从localStorage获取结算商品数据
    const checkoutItemsStr = localStorage.getItem('checkoutItems');
    if (checkoutItemsStr) {
      try {
        const checkoutItems = JSON.parse(checkoutItemsStr);
        if (checkoutItems && checkoutItems.length > 0) {
          console.log('从localStorage获取到结算商品数据:', checkoutItems);
          // 将所有商品添加到页面数据中
          pageData.items = checkoutItems.map((item) => ({
            id: item.id || item.thing_id,
            title: item.title,
            cover: item.cover,
            price: parseFloat(item.price),
            quantity: item.quantity || 1,
          }));

          // 计算商品总价
          updateTotalAmount();
        } else {
          message.warning('没有找到结算商品数据');
        }
      } catch (err) {
        console.error('解析结算商品数据失败', err);
        message.error('解析结算商品数据失败');
      }
    } else {
      // 如果没有从localStorage获取到数据，尝试从URL参数获取单个商品
      if (route.query.id) {
        console.log('从URL参数获取商品数据:', route.query);
        pageData.items = [
          {
            id: route.query.id,
            title: route.query.title,
            cover: route.query.cover,
            price: parseFloat(route.query.price),
            quantity: 1,
          },
        ];
        updateTotalAmount();
      }
    }

    listAddressData();
  });

  // 更新商品数量
  const updateItemQuantity = (index) => {
    // 确保数量为有效值
    if (pageData.items[index].quantity < 1) {
      pageData.items[index].quantity = 1;
    }
    updateTotalAmount();
  };

  // 更新商品总价和最终金额
  const updateTotalAmount = () => {
    pageData.amount = pageData.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    calculateDiscount(); // 重新计算优惠
  };

  // 显示优惠券选择弹窗
  const showCouponModal = () => {
    if (!userStore.user_id) {
      message.warning('请先登录');
      return;
    }

    couponModal.loading = true;
    couponModal.visible = true;

    // 加载用户可用优惠券
    getUserCoupons(userStore.user_id, 1)
      .then((res) => {
        if (res && res.code === 200) {
          if (res.data && res.data.length > 0 && res.data[0].coupon) {
            // 如果后端直接返回了关联的优惠券信息
            couponModal.userCoupons = res.data.map((item) => ({
              userCouponId: item.id, // 用户优惠券ID (b_user_coupon表)
              couponId: item.couponId, // 优惠券模板ID (b_coupon表)
              ...item.coupon, // 优惠券详细信息
              // 使用特殊名称存储原始数据，避免和其他属性冲突
              _rawData: item,
            }));
          } else if (res.data && res.data.length > 0) {
            // 后端只返回了关联数据，没有优惠券详情，使用临时测试数据
            message.warning('优惠券数据不完整，使用测试数据');
            couponModal.userCoupons = [
              {
                userCouponId: res.data[0].id, // 使用真实的用户优惠券ID
                couponId: res.data[0].couponId || 1,
                name: '满100减10元券',
                type: 1,
                amount: 10,
                threshold: 100,
                startTime: '2025-05-24T00:00:00',
                endTime: '2025-06-24T23:59:59',
              },
              {
                userCouponId: res.data.length > 1 ? res.data[1].id : res.data[0].id + 1,
                couponId: res.data.length > 1 ? res.data[1].couponId : res.data[0].couponId || 2,
                name: '8折优惠券',
                type: 2,
                discount: 0.8,
                threshold: 0,
                startTime: '2025-05-24T12:34:24',
                endTime: '2025-05-31T12:34:28',
              },
            ];
          } else {
            couponModal.userCoupons = [];
          }
        } else {
          message.error(res?.message || '获取优惠券失败');
        }
      })
      .catch((err) => {
        console.error('获取优惠券失败', err);
        message.error('获取优惠券失败');

        // 使用临时测试数据
        couponModal.userCoupons = [
          {
            userCouponId: 101,
            couponId: 1,
            name: '满100减10元券',
            type: 1,
            amount: 10,
            threshold: 100,
            startTime: '2025-05-24T00:00:00',
            endTime: '2025-06-24T23:59:59',
          },
          {
            userCouponId: 102,
            couponId: 2,
            name: '8折优惠券',
            type: 2,
            discount: 0.8,
            threshold: 0,
            startTime: '2025-05-24T12:34:24',
            endTime: '2025-05-31T12:34:28',
          },
        ];
      })
      .finally(() => {
        couponModal.loading = false;
      });
  };

  // 确认选择优惠券
  const handleSelectCoupon = () => {
    if (!couponModal.selectedId) {
      message.warning('请选择优惠券');
      return;
    }

    // 根据radio选择的ID找到对应的优惠券
    const selectedCoupon = couponModal.userCoupons.find((item) => item.userCouponId === couponModal.selectedId);
    if (!selectedCoupon) {
      message.warning('优惠券不存在');
      return;
    }

    console.log('选择的优惠券:', selectedCoupon);

    // 设置选中的优惠券，优先使用coupon对象中的信息
    if (selectedCoupon.coupon) {
      pageData.selectedCoupon = {
        ...selectedCoupon,
        ...selectedCoupon.coupon,
        id: selectedCoupon.id, // 保持使用用户优惠券ID
        userCouponId: selectedCoupon.userCouponId, // 明确保存用户优惠券ID
      };
    } else {
      pageData.selectedCoupon = {
        ...selectedCoupon,
        userCouponId: selectedCoupon.userCouponId, // 明确保存用户优惠券ID
      };
    }

    // 设置用户优惠券ID (b_user_coupon表的ID)
    pageData.userCouponId = selectedCoupon.userCouponId;

    console.log('设置的用户优惠券ID:', pageData.userCouponId, '选中的优惠券:', pageData.selectedCoupon);

    // 计算优惠金额
    calculateDiscount();

    couponModal.visible = false;
  };

  // 取消选择优惠券
  const cancelSelectCoupon = () => {
    couponModal.visible = false;
  };

  // 移除选中的优惠券
  const removeCoupon = () => {
    pageData.selectedCoupon = null;
    pageData.userCouponId = null;
    pageData.couponDiscount = 0;
    pageData.finalAmount = pageData.amount;
  };

  // 计算优惠金额
  const calculateDiscount = () => {
    if (!pageData.selectedCoupon) {
      pageData.couponDiscount = 0;
      pageData.finalAmount = pageData.amount;
      return;
    }

    // 满减券
    if (pageData.selectedCoupon.type === 1) {
      if (pageData.amount >= pageData.selectedCoupon.threshold) {
        pageData.couponDiscount = pageData.selectedCoupon.amount;
      } else {
        pageData.couponDiscount = 0;
      }
    }
    // 折扣券
    else if (pageData.selectedCoupon.type === 2) {
      if (pageData.amount >= pageData.selectedCoupon.threshold) {
        pageData.couponDiscount = pageData.amount * (1 - pageData.selectedCoupon.discount);
      } else {
        pageData.couponDiscount = 0;
      }
    }

    // 计算最终金额
    pageData.finalAmount = Math.max(0, pageData.amount - pageData.couponDiscount);
  };

  // 判断优惠券是否可用
  const isCouponAvailable = (coupon) => {
    // 满减券且金额不满足门槛
    if (coupon.type === 1 && pageData.amount < coupon.threshold) {
      return false;
    }
    return true;
  };

  // 获取优惠券不可用原因
  const getCouponDisabledReason = (coupon) => {
    if (coupon.type === 1 && pageData.amount < coupon.threshold) {
      return `订单金额不满${coupon.threshold}元`;
    }
    return '不满足使用条件';
  };

  const handleAdd = () => {
    if (!userStore.user_id) {
      message.warn('请先登录');
      return;
    }

    // 如果没有地址，则显示新增地址弹窗
    if (modal.addressList.length === 0) {
      resetModal();
      modal.visible = true;
      modal.editFlag = false;
      modal.title = '新增';
      // 重置表单
      for (const key in modal.form) {
        modal.form[key] = undefined;
      }
    } else {
      // 否则显示地址选择弹窗
      modal.visible = true;
      modal.title = '选择';
      modal.editFlag = true;
      // 默认选中当前使用的地址
      modal.selectedAddressId =
        modal.addressList.find((addr) => addr.name === pageData.receiverName && addr.mobile === pageData.receiverPhone)?.id ||
        modal.addressList[0].id;
    }
  };

  // 显示新增地址表单
  const handleShowAddNew = () => {
    resetModal();
    modal.editFlag = false;
    modal.title = '新增';
    // 重置表单
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
  };

  const handleOk = () => {
    if (!userStore.user_id) {
      message.warn('请先登录');
      return;
    }

    // 如果是选择已有地址
    if (modal.editFlag && modal.addressList.length > 0) {
      const selectedAddress = modal.addressList.find((addr) => addr.id === modal.selectedAddressId);
      if (selectedAddress) {
        // 更新页面上显示的地址信息
        pageData.receiverName = selectedAddress.name;
        pageData.receiverPhone = selectedAddress.mobile;
        pageData.receiverAddress = selectedAddress.description;
        pageData.isDefaultAddress = selectedAddress.default;

        hideModal();
        return;
      }
    }

    // 如果是新增地址
    myform.value
      ?.validate()
      .then(() => {
        const formData = new FormData();
        formData.append('userId', userStore.user_id);
        formData.append('def', modal.form.default ? '1' : '0');
        if (modal.form.name) {
          formData.append('name', modal.form.name);
        }
        if (modal.form.mobile) {
          formData.append('mobile', modal.form.mobile);
        }
        if (modal.form.desc) {
          formData.append('description', modal.form.desc);
        }
        createAddressApi(formData)
          .then((res) => {
            console.log(res);
            hideModal();

            // 更新页面上显示的地址信息
            pageData.receiverName = modal.form.name;
            pageData.receiverAddress = modal.form.desc;
            pageData.receiverPhone = modal.form.mobile;
            pageData.isDefaultAddress = modal.form.default;

            // 重新获取地址列表
            listAddressData();
          })
          .catch((err) => {
            message.error(err.msg || '新建失败');
          });
      })
      .catch((err) => {
        console.log(err);
        console.log('不能为空');
      });
  };

  const handleCancel = () => {
    hideModal();
  };

  // 恢复表单初始状态
  const resetModal = () => {
    myform.value?.resetFields();
  };

  // 关闭弹窗
  const hideModal = () => {
    modal.visible = false;
  };

  const onCountChange = (value) => {
    pageData.amount = pageData.price * value;
    // 重新计算优惠金额
    calculateDiscount();
  };

  const listAddressData = () => {
    let userId = userStore.user_id;
    if (!userId) {
      return;
    }

    listAddressListApi({ userId: userId })
      .then((res) => {
        if (res.data) {
          // 保存地址列表
          modal.addressList = res.data;

          if (res.data.length > 0) {
            // 默认选择默认地址或第一个地址
            let defaultAddress = res.data.find((item) => item.default) || res.data[0];

            // 设置当前选中的收货地址
            pageData.receiverName = defaultAddress.name;
            pageData.receiverPhone = defaultAddress.mobile;
            pageData.receiverAddress = defaultAddress.description;
            pageData.isDefaultAddress = defaultAddress.default;

            // 记录选中的地址ID
            modal.selectedAddressId = defaultAddress.id;
          }
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleBack = () => {
    router.back();
    console.log('back...');
  };
  const handleJiesuan = () => {
    if (!pageData.receiverAddress) {
      message.error('请先选择收货地址');
      return;
    }

    if (pageData.items.length === 0) {
      message.error('订单中没有商品');
      return;
    }

    if (!userStore.user_id) {
      message.warning('请先登录');
      return;
    }

    // 生成订单号
    let time = new Date().getTime().toString();

    // 由于后端不支持多商品订单，我们需要为每个商品创建一个单独的订单
    const orderPromises = pageData.items.map((item) => {
      // 构建要提交的数据 - 使用FormData来匹配后端期望的格式
      const formData = new FormData();
      formData.append('userId', userStore.user_id);
      formData.append('thingId', item.id);
      formData.append('count', item.quantity);
      formData.append('orderNumber', time + '_' + item.id); // 使用唯一订单号
      time = time + '_' + item.id;
      // 处理备注
      let remarkText = pageData.remark || '';
      // 添加优惠券信息到备注中
      if (pageData.selectedCoupon) {
        const couponInfo =
          pageData.selectedCoupon.type === 1
            ? `使用满${pageData.selectedCoupon.threshold}减${pageData.selectedCoupon.amount}优惠券`
            : `使用${pageData.selectedCoupon.discount * 10}折优惠券`;

        if (remarkText) {
          remarkText = `${remarkText} (${couponInfo})`;
        } else {
          remarkText = couponInfo;
        }
      }
      formData.append('remark', remarkText);

      formData.append('receiverName', pageData.receiverName);
      formData.append('receiverPhone', pageData.receiverPhone);
      formData.append('receiverAddress', pageData.receiverAddress);

      // 返回创建订单的Promise
      return createApi(formData);
    });

    // 显示加载状态
    message.loading({ content: '正在创建订单...', key: 'createOrder' });

    // 等待所有订单创建完成
    Promise.all(orderPromises)
      .then((responses) => {
        console.log('订单创建响应:', responses);

        // 获取第一个成功的订单ID用于优惠券绑定
        let orderId = null;
        for (const res of responses) {
          if (res && res.data) {
            orderId = res.data;
            break;
          }
        }

        // 如果有优惠券且获取到了订单ID，则应用优惠券
        if (pageData.userCouponId && orderId) {
          console.log(`准备应用优惠券，用户优惠券ID: ${pageData.userCouponId}, 订单ID: ${orderId}`);

          // 确保ID是数字类型
          const numericUserCouponId = Number(pageData.userCouponId);
          const numericOrderId = Number(orderId);

          if (!isNaN(numericUserCouponId) && !isNaN(numericOrderId)) {
            message.loading({ content: '正在应用优惠券...', key: 'createOrder' });

            // 使用优惠券
            useCoupon(numericUserCouponId, numericOrderId)
              .then((res) => {
                console.log('优惠券使用响应:', res);
              })
              .catch((err) => {
                console.error('使用优惠券失败', err);
              })
              .finally(() => {
                finishOrder(time, orderId);
              });
          } else {
            finishOrder(time, orderId);
          }
        } else {
          finishOrder(time, orderId || time);
        }
      })
      .catch((err) => {
        console.error('创建订单失败', err);
        message.error({ content: err?.message || err?.msg || '创建订单失败，请稍后重试', key: 'createOrder' });
      });
  };

  // 完成订单创建并跳转到支付页面
  const finishOrder = (orderTime, orderId) => {
    message.success({ content: '订单创建成功，请支付', key: 'createOrder' });

    // 保存当前的checkoutItems到支付页面使用
    const checkoutItemsStr = localStorage.getItem('checkoutItems');
    if (checkoutItemsStr) {
      // 先把购物车清空
      localStorage.removeItem('cart');
      // 但保留checkoutItems直到支付完成
      // localStorage.removeItem('checkoutItems'); // 移除这行，让支付页面能获取到商品信息
    }

    // 跳转到支付页面，使用完整的orderTime作为orderNum
    // orderTime包含了所有子订单的ID，例如: timestamp_id1_id2
    router.push({
      name: 'pay',
      query: {
        amount: typeof pageData.finalAmount === 'number' ? pageData.finalAmount.toFixed(2) : pageData.finalAmount,
        orderNum: orderTime,
        orderId: orderId, // 传递第一个订单ID，主要用于优惠券等关联
      },
    });
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };
</script>

<style scoped>
  /* 全局样式 */
  .confirm-page {
    background-color: #f5f7fa;
    min-height: 100vh;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    color: #333;
  }

  .confirm-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 70px 20px 60px;
  }

  /* 页面标题和进度条 */
  .page-title {
    margin-bottom: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-title h2 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  .order-progress {
    display: flex;
    align-items: center;
  }

  .progress-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
  }

  .step-number {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background-color: #e0e0e0;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .progress-step.active .step-number {
    background-color: #4684e2;
  }

  .step-text {
    font-size: 14px;
    color: #666;
  }

  .progress-step.active .step-text {
    color: #4684e2;
    font-weight: 500;
  }

  .progress-line {
    width: 60px;
    height: 2px;
    background-color: #e0e0e0;
    margin: 0 15px;
    margin-bottom: 25px;
  }

  /* 内容布局 */
  .confirm-content {
    display: flex;
    gap: 20px;
  }

  .left-content {
    flex: 3;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .right-content {
    flex: 2;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  /* 卡片样式 */
  .content-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    overflow: hidden;
  }

  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-header h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  .card-body {
    padding: 20px;
  }

  /* 商品样式 */
  .product-item {
    display: flex;
    align-items: center;
    padding: 15px 0;
  }

  .product-image {
    width: 80px;
    height: 100px;
    margin-right: 20px;
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .product-details {
    flex: 1;
    margin-right: 20px;
  }

  .product-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
    line-height: 1.4;
  }

  .product-price {
    font-size: 16px;
    color: #ff6a00;
    font-weight: 600;
  }

  .product-quantity {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 30px;
  }

  .quantity-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }

  .custom-input-number {
    width: 80px;
  }

  .product-subtotal {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 20px;
    min-width: 80px;
  }

  .subtotal-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }

  .subtotal-value {
    font-size: 16px;
    color: #ff6a00;
    font-weight: 600;
  }

  .product-actions {
    display: flex;
    align-items: center;
  }

  .delete-icon {
    width: 18px;
    height: 18px;
    cursor: pointer;
  }

  /* 优惠券样式 */
  .select-coupon-btn {
    font-size: 14px;
    display: flex;
    align-items: center;
  }

  .select-icon {
    margin-left: 4px;
    font-size: 16px;
    font-style: normal;
  }

  .no-coupon {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px 0;
    color: #999;
    font-size: 14px;
  }

  .coupon-icon {
    font-size: 20px;
    margin-right: 10px;
    font-style: normal;
  }

  .coupon-card {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: stretch;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    overflow: hidden;
  }

  .coupon-left {
    flex: 0 0 120px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #ff4d4f, #ff7875);
    color: white;
    font-size: 16px;
    padding: 15px 10px;
    position: relative;
  }

  .coupon-card.discount .coupon-left {
    background: linear-gradient(135deg, #ff9c27, #ffbd5c);
  }

  .coupon-left:after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    height: 100%;
    width: 10px;
    background: radial-gradient(circle at 0 8px, transparent 6px, white 7px) repeat-y;
    background-size: 10px 16px;
    background-position: right;
  }

  .coupon-right {
    flex: 1;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
  }

  .coupon-amount {
    font-size: 24px;
    margin-bottom: 8px;
    text-align: center;
  }

  .amount-value {
    font-weight: bold;
    font-size: 32px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .amount-unit {
    color: white;
    font-size: 14px;
  }

  .coupon-threshold {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.9);
  }

  .coupon-info {
    margin-bottom: 4px;
    flex: 1;
  }

  .coupon-name {
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 8px;
    color: #333;
  }

  .coupon-time {
    font-size: 12px;
    color: #999;
  }

  .coupon-action {
    margin-left: 15px;
  }

  /* 备注样式 */
  .remark-textarea {
    width: 100%;
    border-radius: 4px;
    resize: none;
  }

  /* 地址样式 */
  .add-address-btn {
    font-size: 14px;
  }

  .address-info {
    padding: 10px 0;
  }

  .address-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .receiver-info {
    display: flex;
    align-items: center;
  }

  .receiver-name {
    font-weight: 600;
    font-size: 16px;
    color: #333;
    margin-right: 15px;
  }

  .receiver-phone {
    color: #666;
  }

  .default-tag {
    padding: 2px 8px;
    background-color: #f9f0ff;
    color: #722ed1;
    font-size: 12px;
    border-radius: 4px;
  }

  .address-detail {
    display: flex;
    align-items: flex-start;
    color: #333;
    font-size: 14px;
    line-height: 1.6;
  }

  .location-icon {
    margin-right: 8px;
    font-style: normal;
    color: #4684e2;
  }

  .no-address {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px 0;
    color: #999;
    font-size: 14px;
  }

  .address-icon {
    font-size: 20px;
    margin-right: 10px;
    font-style: normal;
  }

  /* 结算样式 */
  .checkout-card {
    position: sticky;
    top: 20px;
  }

  .price-summary {
    padding-bottom: 20px;
  }

  .price-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .price-label {
    color: #666;
    font-size: 14px;
  }

  .price-value {
    font-size: 14px;
    color: #333;
  }

  .price-discount {
    font-size: 14px;
    color: #ff4d4f;
    font-weight: 500;
  }

  .price-divider {
    height: 1px;
    background-color: #f0f0f0;
    margin: 15px 0;
  }

  .price-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
  }

  .total-label {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .total-value {
    font-size: 24px;
    font-weight: 600;
    color: #ff6a00;
  }

  .checkout-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
  }

  .back-btn {
    min-width: 120px;
  }

  .checkout-btn {
    min-width: 120px;
    height: 40px;
    border-radius: 20px;
    font-size: 16px;
    background: linear-gradient(135deg, #4684e2, #5a9cf8);
    border: none;
    box-shadow: 0 4px 12px rgba(70, 132, 226, 0.3);
  }

  .checkout-btn:hover {
    background: linear-gradient(135deg, #3b77d9, #4684e2);
  }

  /* 弹窗样式 */
  .coupon-modal :deep(.ant-modal-content),
  .address-modal :deep(.ant-modal-content) {
    border-radius: 8px;
    overflow: hidden;
  }

  .coupon-select-list {
    max-height: 400px;
    overflow-y: auto;
    padding: 10px 0;
  }

  .coupon-item {
    margin-bottom: 15px;
    border-radius: 8px;
    display: flex;
    position: relative;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    overflow: hidden;
  }

  .coupon-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  .coupon-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 8px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #ff4d4f;
    font-size: 18px;
    font-weight: bold;
  }

  .empty-tips {
    text-align: center;
    padding: 40px 0;
    color: #999;
  }

  .empty-icon {
    font-size: 40px;
    display: block;
    margin-bottom: 15px;
    font-style: normal;
  }

  /* 地址选择弹窗样式 */
  .address-list {
    max-height: 350px;
    overflow-y: auto;
    margin-bottom: 15px;
  }

  .address-radio-group {
    width: 100%;
  }

  .address-item {
    padding: 12px;
    margin-bottom: 10px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    transition: all 0.3s;
  }

  .address-item:hover {
    border-color: #4684e2;
    box-shadow: 0 2px 8px rgba(70, 132, 226, 0.1);
  }

  .address-radio {
    width: 100%;
    display: flex;
  }

  .address-content {
    width: 100%;
    padding-left: 10px;
  }

  .address-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }

  .address-name {
    font-weight: 500;
    font-size: 15px;
    margin-right: 15px;
  }

  .address-phone {
    color: #666;
  }

  .address-default-tag {
    margin-left: auto;
    padding: 2px 8px;
    background-color: #f9f0ff;
    color: #722ed1;
    font-size: 12px;
    border-radius: 4px;
  }

  .address-text {
    color: #333;
    font-size: 14px;
    line-height: 1.5;
  }

  .address-actions {
    display: flex;
    justify-content: center;
    margin-top: 10px;
    padding-top: 15px;
    border-top: 1px dashed #f0f0f0;
  }
</style>
