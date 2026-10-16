<template>
  <div class="address-page">
    <header class="page-head">
      <div>
        <span class="page-kicker">ADDRESS BOOK</span>
        <h2>&#x5730;&#x5740;&#x7BA1;&#x7406;</h2>
        <p>&#x7BA1;&#x7406;&#x6536;&#x8D27;&#x4EBA;&#x3001;&#x8054;&#x7CFB;&#x65B9;&#x5F0F;&#x548C;&#x914D;&#x9001;&#x5730;&#x5740;</p>
      </div>
      <a-button type="primary" class="head-btn" @click="handleAdd">&#x65B0;&#x5EFA;&#x5730;&#x5740;</a-button>
    </header>

    <section class="address-summary">
      <div class="summary-card dark">
        <span>Saved</span>
        <strong>{{ pageData.addressData.length }}</strong>
        <p>已保存地址</p>
      </div>
      <div class="summary-card">
        <span>Default</span>
        <strong>{{ defaultAddressCount }}</strong>
        <p>默认收货地址</p>
      </div>
      <div class="summary-card">
        <span>Checkout</span>
        <strong>Fast</strong>
        <p>结算时快速选择</p>
      </div>
    </section>

    <div v-if="pageData.addressData.length" class="address-list">
      <article v-for="item in pageData.addressData" :key="item.id" class="address-item">
        <div class="address-marker">
          <span>{{ item.name ? item.name.slice(0, 1) : '收' }}</span>
        </div>
        <div class="address-main">
          <div class="name-row">
            <strong>{{ item.name }}</strong>
            <span>{{ item.mobile }}</span>
            <em v-if="item.def === '1'">&#x9ED8;&#x8BA4;</em>
          </div>
          <p>{{ item.description }}</p>
        </div>

        <div class="actions">
          <button type="button" @click="handleEdit(item)">&#x7F16;&#x8F91;</button>
          <a-popconfirm
            title="&#x786E;&#x5B9A;&#x5220;&#x9664;&#x8FD9;&#x4E2A;&#x5730;&#x5740;&#x5417;&#xFF1F;"
            ok-text="&#x786E;&#x5B9A;"
            cancel-text="&#x53D6;&#x6D88;"
            @confirm="handleDelete(item)"
          >
            <button type="button" class="danger">&#x5220;&#x9664;</button>
          </a-popconfirm>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">&#x5730;</div>
      <h3>&#x6682;&#x65E0;&#x6536;&#x8D27;&#x5730;&#x5740;</h3>
      <p>&#x65B0;&#x5EFA;&#x4E00;&#x4E2A;&#x5E38;&#x7528;&#x5730;&#x5740;&#xFF0C;&#x7ED3;&#x7B97;&#x65F6;&#x4F1A;&#x66F4;&#x65B9;&#x4FBF;&#x3002;</p>
      <a-button type="primary" @click="handleAdd">&#x65B0;&#x5EFA;&#x5730;&#x5740;</a-button>
    </div>

    <a-modal
      :visible="modal.visible"
      :forceRender="true"
      :title="modal.title"
      :width="660"
      ok-text="&#x786E;&#x8BA4;"
      cancel-text="&#x53D6;&#x6D88;"
      @cancel="handleCancel"
      @ok="handleOk"
      class="address-modal"
    >
      <div class="address-form-intro">
        <strong>收货信息</strong>
        <span>请填写真实联系人和配送地址，默认地址将在结算时优先展示。</span>
      </div>

      <a-form ref="myform" class="business-form" :label-col="{ style: { width: '104px' } }" :model="modal.form" :rules="modal.rules">
        <a-form-item label="&#x59D3;&#x540D;" name="name">
          <a-input placeholder="&#x8BF7;&#x8F93;&#x5165;&#x6536;&#x8D27;&#x4EBA;" v-model:value="modal.form.name" />
        </a-form-item>

        <a-form-item label="&#x624B;&#x673A;&#x53F7;" name="mobile">
          <a-input placeholder="&#x8BF7;&#x8F93;&#x5165;&#x624B;&#x673A;&#x53F7;" v-model:value="modal.form.mobile" />
        </a-form-item>

        <a-form-item label="&#x8BE6;&#x7EC6;&#x5730;&#x5740;" name="description">
          <a-textarea
            placeholder="&#x8BF7;&#x8F93;&#x5165;&#x7701;&#x5E02;&#x533A;&#x53CA;&#x8BE6;&#x7EC6;&#x95E8;&#x724C;&#x53F7;"
            v-model:value="modal.form.description"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>

        <a-form-item label="&#x9ED8;&#x8BA4;&#x5730;&#x5740;">
          <a-switch v-model:checked="modal.form.default" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, reactive, onMounted } from 'vue';
  import { FormInstance, message } from 'ant-design-vue';
  import { listApi, deleteApi, createApi, updateApi } from '/@/api/address';
  import { useUserStore } from '/@/store';

  const userStore = useUserStore();

  const pageData = reactive({
    addressData: [] as any[],
  });

  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    form: {
      id: undefined as any,
      name: undefined as any,
      mobile: undefined as any,
      description: undefined as any,
      default: false,
    },
    rules: {
      name: [{ required: true, message: '\u8bf7\u8f93\u5165\u6536\u8d27\u4eba', trigger: 'change' }],
      mobile: [{ required: true, message: '\u8bf7\u8f93\u5165\u624b\u673a\u53f7', trigger: 'change' }],
      description: [{ required: true, message: '\u8bf7\u8f93\u5165\u8be6\u7ec6\u5730\u5740', trigger: 'change' }],
    },
  });

  const myform = ref<FormInstance>();

  const defaultAddressCount = computed(() => pageData.addressData.filter((item) => item.def === '1' || item.default === true).length);

  onMounted(() => {
    listAddressData();
  });

  const listAddressData = () => {
    const userId = userStore.user_id;
    if (!userId) return;

    listApi({ userId })
      .then((res) => {
        pageData.addressData = Array.isArray(res.data) ? res.data : [];
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleDelete = (item: any) => {
    deleteApi({ ids: item.id })
      .then(() => {
        message.success('\u5730\u5740\u5df2\u5220\u9664');
        listAddressData();
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleAdd = () => {
    resetModal();
    modal.visible = true;
    modal.editFlag = false;
    modal.title = '\u65b0\u5efa\u5730\u5740';
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '\u7f16\u8f91\u5730\u5740';
    modal.form.id = record.id;
    modal.form.name = record.name;
    modal.form.mobile = record.mobile;
    modal.form.description = record.description;
    modal.form.default = record.def === '1' || record.default === true;
  };

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        const formData = new FormData();
        formData.append('userId', userStore.user_id);
        formData.append('def', modal.form.default ? '1' : '0');
        if (modal.form.id) formData.append('id', modal.form.id);
        if (modal.form.name) formData.append('name', modal.form.name);
        if (modal.form.mobile) formData.append('mobile', modal.form.mobile);
        if (modal.form.description) formData.append('description', modal.form.description);

        const request = modal.editFlag ? updateApi(formData) : createApi(formData);
        request
          .then(() => {
            message.success(modal.editFlag ? '\u5730\u5740\u5df2\u66f4\u65b0' : '\u5730\u5740\u5df2\u65b0\u5efa');
            hideModal();
            listAddressData();
          })
          .catch((err) => {
            console.log(err);
          });
      })
      .catch(() => {
        message.warning('\u8bf7\u5b8c\u5584\u5730\u5740\u4fe1\u606f');
      });
  };

  const handleCancel = () => {
    hideModal();
  };

  const resetModal = () => {
    myform.value?.resetFields();
    modal.form.id = undefined;
    modal.form.name = undefined;
    modal.form.mobile = undefined;
    modal.form.description = undefined;
    modal.form.default = false;
  };

  const hideModal = () => {
    modal.visible = false;
  };
</script>

<style scoped lang="less">
  .address-page {
    width: 100%;
  }

  .page-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 20px;
    min-height: 150px;
    padding: 28px;
    border-radius: 22px;
    background:
      linear-gradient(90deg, rgba(16, 24, 40, 0.94), rgba(16, 24, 40, 0.74)),
      linear-gradient(135deg, #222831, #4b5563);
    color: #fff;
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      right: -42px;
      bottom: -58px;
      width: 180px;
      height: 180px;
      border-radius: 50%;
      background: rgba(255, 209, 0, 0.2);
    }
  }

  .page-head h2 {
    margin: 8px 0 0;
    color: #fff;
    font-size: 32px;
    line-height: 1.2;
    font-weight: 900;
  }

  .page-head p {
    margin: 10px 0 0;
    color: rgba(255, 255, 255, 0.72);
    font-size: 14px;
  }

  .page-kicker {
    color: #ffd100;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.1em;
  }

  .head-btn {
    position: relative;
    z-index: 1;
  }

  .address-summary {
    display: grid;
    grid-template-columns: 1.15fr 1fr 1fr;
    gap: 14px;
    margin: 18px 0 20px;
  }

  .summary-card {
    min-height: 104px;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    background: #fff;
    padding: 18px;
    box-shadow: 0 12px 30px rgba(16, 24, 40, 0.04);
  }

  .summary-card.dark {
    background: #fff8d7;
    border-color: #ffeaa3;
  }

  .summary-card span {
    color: #98a2b3;
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.08em;
  }

  .summary-card strong {
    display: block;
    margin-top: 8px;
    color: #222831;
    font-size: 30px;
    line-height: 1;
    font-weight: 900;
  }

  .summary-card p {
    margin: 9px 0 0;
    color: #667085;
  }

  .address-list {
    display: grid;
    gap: 14px;
    margin-top: 0;
  }

  .address-item {
    display: grid;
    grid-template-columns: 54px minmax(0, 1fr) auto;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding: 18px;
    border: 1px solid #edf0f4;
    border-radius: 18px;
    background: #fff;
    transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  }

  .address-item:hover {
    transform: translateY(-2px);
    border-color: #ffd100;
    box-shadow: 0 14px 30px rgba(16, 24, 40, 0.06);
  }

  .address-marker {
    width: 54px;
    height: 54px;
    border-radius: 18px;
    background: #222831;
    color: #ffd100;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 900;
  }

  .address-main {
    min-width: 0;
  }

  .name-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    color: #101828;
  }

  .name-row strong {
    font-size: 17px;
    font-weight: 900;
  }

  .name-row span {
    color: #667085;
  }

  .name-row em {
    padding: 4px 10px;
    border-radius: 999px;
    background: #fff8d7;
    color: #8a6f00;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
  }

  .address-main p {
    margin: 8px 0 0;
    color: #475467;
    font-size: 15px;
    line-height: 24px;
    word-break: break-word;
  }

  .actions {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    flex-shrink: 0;
  }

  .actions button {
    height: 36px;
    padding: 0 14px;
    border: 1px solid #dce3ee;
    border-radius: 999px;
    background: #fff;
    color: #222831;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;
    transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
  }

  .actions button:hover {
    border-color: #222831;
    background: #222831;
    color: #fff;
  }

  .actions button.danger {
    color: #e5484d;
  }

  .actions button.danger:hover {
    border-color: #f4c7c9;
    background: #fff7f7;
  }

  .empty-state {
    display: grid;
    justify-items: center;
    gap: 10px;
    padding: 76px 20px;
    color: #7a8495;
    text-align: center;
  }

  .empty-icon {
    width: 54px;
    height: 54px;
    display: grid;
    place-items: center;
    border-radius: 50%;
    background: #ffd100;
    color: #222831;
    font-weight: 700;
  }

  .empty-state h3 {
    margin: 6px 0 0;
    color: #18233a;
    font-size: 17px;
  }

  .empty-state p {
    margin: 0 0 8px;
  }

  :deep(.ant-btn-primary) {
    height: 40px;
    border-radius: 999px;
    background: #ffd100;
    border-color: #ffd100;
    color: #222831;
    font-weight: 900;
    box-shadow: 0 12px 24px rgba(255, 209, 0, 0.22);
  }

  :deep(.ant-btn-primary:hover),
  :deep(.ant-btn-primary:focus) {
    background: #f2c500;
    border-color: #f2c500;
    color: #222831;
  }

  :deep(.ant-modal-content) {
    border-radius: 18px;
    overflow: hidden;
    box-shadow: 0 26px 70px rgba(16, 24, 40, 0.24);
  }

  :deep(.address-modal .ant-modal-header) {
    padding: 22px 28px;
    border-bottom: 1px solid #edf0f4;
    background: #fbfcfd;
  }

  :deep(.address-modal .ant-modal-title) {
    color: #101828;
    font-size: 20px;
    font-weight: 900;
  }

  :deep(.address-modal .ant-modal-close-x) {
    color: #667085;
  }

  :deep(.address-modal .ant-modal-body) {
    padding: 26px 28px 20px;
    background: #fff;
  }

  :deep(.address-modal .ant-modal-footer) {
    padding: 18px 28px;
    border-top: 1px solid #edf0f4;
    background: #fbfcfd;
  }

  :deep(.address-modal .ant-btn) {
    height: 40px;
    min-width: 92px;
    border-radius: 10px;
    font-weight: 800;
  }

  :deep(.address-modal .ant-btn:not(.ant-btn-primary)) {
    color: #344054;
    border-color: #d0d5dd;
    background: #fff;
  }

  :deep(.address-modal .ant-btn-primary) {
    background: #1f2937;
    border-color: #1f2937;
    color: #fff;
    box-shadow: 0 10px 22px rgba(31, 41, 55, 0.18);
  }

  :deep(.address-modal .ant-btn-primary:hover),
  :deep(.address-modal .ant-btn-primary:focus) {
    background: #111827;
    border-color: #111827;
    color: #fff;
  }

  .address-form-intro {
    display: grid;
    gap: 6px;
    padding: 16px 18px;
    margin-bottom: 22px;
    border: 1px solid #edf0f4;
    border-radius: 14px;
    background: #f8fafc;
  }

  .address-form-intro strong {
    color: #101828;
    font-size: 15px;
    font-weight: 900;
  }

  .address-form-intro span {
    color: #667085;
    line-height: 1.7;
  }

  :deep(.business-form .ant-form-item) {
    margin-bottom: 22px;
  }

  :deep(.business-form .ant-form-item-label > label) {
    color: #344054;
    font-weight: 800;
  }

  :deep(.business-form .ant-form-item-label > label::after) {
    color: #98a2b3;
  }

  :deep(.business-form .ant-form-item-required::before) {
    color: #b42318 !important;
  }

  :deep(.ant-input),
  :deep(.ant-input-affix-wrapper),
  :deep(.ant-input-number) {
    border-radius: 12px;
    border-color: #d0d5dd;
    background: #fff;
    color: #101828;
    box-shadow: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }

  :deep(.address-modal .ant-input) {
    min-height: 42px;
    padding: 9px 14px;
    font-size: 14px;
  }

  :deep(.address-modal textarea.ant-input) {
    min-height: 96px;
    line-height: 1.7;
  }

  :deep(.ant-input:hover),
  :deep(.ant-input:focus),
  :deep(.ant-input-focused),
  :deep(.ant-input-affix-wrapper:hover),
  :deep(.ant-input-affix-wrapper-focused) {
    border-color: #667085;
    box-shadow: 0 0 0 3px rgba(16, 24, 40, 0.06);
  }

  :deep(.address-modal .ant-switch-checked) {
    background-color: #1f2937;
  }

  :deep(.address-modal .ant-form-item-explain-error) {
    color: #b42318;
    font-size: 12px;
  }

  @media (max-width: 720px) {
    .page-head {
      flex-direction: column;
    }

    .address-summary {
      grid-template-columns: 1fr;
    }

    .address-item {
      grid-template-columns: 1fr;
    }

    .address-marker {
      width: 48px;
      height: 48px;
    }

    .actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
</style>
