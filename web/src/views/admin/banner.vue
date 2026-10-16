<template>
  <div class="banner-container">
    <!-- 搜索和筛选区域 -->
    <a-card :bordered="false" class="filter-card">
      <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" class="filter-col">
          <a-input v-model:value="data.keyword" placeholder="搜索关联商品ID" allowClear class="filter-input">
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="6" class="filter-col">
          <a-select v-model:value="data.linkStatus" placeholder="关联状态" style="width: 100%" class="filter-select" allowClear>
            <a-select-option value="linked">已关联商品</a-select-option>
            <a-select-option value="unlinked">未关联商品</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="12" class="filter-col">
          <div class="filter-btns">
            <a-button type="primary" @click="onSearch" class="btn-search">
              <template #icon><search-outlined /></template>
              搜索
            </a-button>
            <a-button @click="onReset" class="btn-reset">
              <template #icon><reload-outlined /></template>
              重置
            </a-button>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!--页面区域-->
    <div class="page-view">
      <div class="table-operations">
        <a-space>
          <a-button type="primary" @click="handleAdd" class="btn-add">
            <template #icon><plus-outlined /></template>
            新增轮播图
          </a-button>
          <a-button danger @click="handleBatchDelete" class="btn-delete">
            <template #icon><delete-outlined /></template>
            批量删除
          </a-button>
        </a-space>
      </div>
      <a-table
        size="middle"
        rowKey="id"
        :loading="data.loading"
        :columns="columns"
        :data-source="filteredBannerList"
        :scroll="{ x: 'max-content' }"
        :row-selection="rowSelection"
        :pagination="{
          size: 'default',
          current: data.page,
          pageSize: data.pageSize,
          onChange: (current) => (data.page = current),
          showSizeChanger: false,
          showTotal: (total) => `共${total}条数据`,
        }"
        class="custom-table"
      >
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.key === 'image'">
            <div class="banner-image-container">
              <img :src="BASE_URL + '/api/staticfiles/banner/' + text" alt="轮播图" class="banner-image" />
              <div class="image-overlay">
                <eye-outlined class="preview-icon" @click="previewImage(record)" />
              </div>
            </div>
          </template>
          <template v-if="column.key === 'thingId'">
            <a-tag v-if="text" color="blue">{{ text }}</a-tag>
            <a-tag v-else color="red">未关联</a-tag>
          </template>
          <template v-if="column.key === 'createTime'">
            <span>{{ formatDate(text) }}</span>
          </template>
          <template v-if="column.key === 'operation'">
            <div class="operation-btns">
              <a-button type="primary" size="small" @click="handleEdit(record)" class="btn-edit">
                <template #icon><edit-outlined /></template>
                编辑
              </a-button>
              <a-popconfirm title="确定删除此轮播图?" ok-text="是" cancel-text="否" @confirm="confirmDelete(record)" placement="topRight">
                <a-button type="primary" danger size="small" class="btn-delete">
                  <template #icon><delete-outlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 图片预览 -->
    <a-image :width="0" :visible="previewVisible" :src="previewImageUrl" @visibleChange="handlePreviewVisibleChange" />

    <!--弹窗区域-->
    <div>
      <a-modal
        :visible="modal.visible"
        :forceRender="true"
        :title="modal.title"
        ok-text="确认"
        cancel-text="取消"
        @cancel="handleCancel"
        @ok="handleOk"
        class="banner-modal"
        width="700px"
      >
        <div>
          <a-form ref="myform" :label-col="{ style: { width: '100px' } }" :model="modal.form" :rules="modal.rules" class="banner-form">
            <a-row :gutter="24">
              <a-col span="24">
                <a-form-item label="轮播图" name="imageFile">
                  <a-upload-dragger
                    name="file"
                    accept="image/*"
                    :multiple="false"
                    :before-upload="beforeUpload"
                    v-model:file-list="fileList"
                    class="upload-container"
                  >
                    <p class="ant-upload-drag-icon">
                      <template v-if="modal.form.coverUrl">
                        <img :src="modal.form.coverUrl" class="preview-image" />
                      </template>
                      <template v-else>
                        <file-image-outlined />
                      </template>
                    </p>
                    <p class="ant-upload-text"> 点击或拖拽图片到此区域上传 </p>
                    <p class="ant-upload-hint"> 支持单个图片上传，建议尺寸 1920×500px </p>
                  </a-upload-dragger>
                </a-form-item>
              </a-col>
              <a-col span="24">
                <a-form-item label="关联商品" name="thingId">
                  <a-select
                    v-model:value="modal.form.thingId"
                    show-search
                    placeholder="请选择关联商品"
                    option-filter-prop="children"
                    :options="productOptions"
                    :filter-option="filterOption"
                    class="product-select"
                    allowClear
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi } from '/@/api/banner';
  import { listApi as listThingApi } from '/@/api/thing';
  import { BASE_URL } from '/@/store/constants';
  import {
    PlusOutlined,
    FileImageOutlined,
    SearchOutlined,
    ReloadOutlined,
    EditOutlined,
    DeleteOutlined,
    EyeOutlined,
    PictureOutlined,
    LinkOutlined,
    CalendarOutlined,
    DisconnectOutlined,
  } from '@ant-design/icons-vue';
  import { ref, reactive, onMounted, computed } from 'vue';

  const columns = reactive([
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      align: 'center',
      width: 80,
    },
    {
      title: '轮播图',
      dataIndex: 'image',
      key: 'image',
      align: 'center',
      width: 200,
    },
    {
      title: '关联商品ID',
      dataIndex: 'thingId',
      key: 'thingId',
      align: 'center',
      width: 150,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      align: 'center',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 180,
    },
  ]);

  // 页面数据
  const data = reactive({
    bannerList: [],
    loading: false,
    keyword: '',
    linkStatus: undefined,
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 图片预览
  const previewVisible = ref(false);
  const previewImageUrl = ref('');

  // 产品选项
  const productOptions = ref([]);

  // 文件列表
  const fileList = ref([]);

  // 弹窗数据源
  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    form: {
      id: undefined,
      thingId: undefined,
      imageFile: undefined,
      image: undefined,
      coverUrl: undefined,
    },
    rules: {
      thingId: [{ required: false, message: '请选择关联商品', trigger: 'change' }],
    },
  });

  const myform = ref<FormInstance>();

  // 过滤后的轮播图列表
  const filteredBannerList = computed(() => {
    let result = [...data.bannerList];

    // 按关键字过滤
    if (data.keyword) {
      result = result.filter((item) => item.thingId && item.thingId.toString().includes(data.keyword));
    }

    // 按关联状态过滤
    if (data.linkStatus === 'linked') {
      result = result.filter((item) => item.thingId);
    } else if (data.linkStatus === 'unlinked') {
      result = result.filter((item) => !item.thingId);
    }

    return result;
  });

  // 获取已关联商品的轮播图数量
  const getLinkedProducts = () => {
    return data.bannerList.filter((item) => item.thingId).length;
  };

  // 获取未关联商品的轮播图数量
  const getUnlinkedProducts = () => {
    return data.bannerList.filter((item) => !item.thingId).length;
  };

  // 获取本周新增的轮播图数量
  const getThisWeekBanners = () => {
    const now = new Date();
    const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay());
    return data.bannerList.filter((item) => {
      const createTime = new Date(item.createTime);
      return createTime >= weekStart;
    }).length;
  };

  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (error) {
      return dateString;
    }
  };

  // 预览图片
  const previewImage = (record) => {
    previewImageUrl.value = BASE_URL + '/api/staticfiles/banner/' + record.image;
    previewVisible.value = true;
  };

  // 处理预览可见性变化
  const handlePreviewVisibleChange = (visible) => {
    previewVisible.value = visible;
  };

  onMounted(() => {
    getDataList();
    getProductList();
  });

  // 搜索
  const onSearch = () => {
    data.page = 1;
  };

  // 重置
  const onReset = () => {
    data.keyword = '';
    data.linkStatus = undefined;
    data.page = 1;
  };

  // 获取商品列表
  const getProductList = () => {
    listThingApi({})
      .then((res) => {
        if (res.data) {
          productOptions.value = res.data.map((item: any) => {
            return {
              value: item.id,
              label: `${item.title} (ID: ${item.id})`,
            };
          });
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  // 过滤选项
  const filterOption = (input: string, option: any) => {
    return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  };

  const getDataList = () => {
    data.loading = true;
    listApi()
      .then((res) => {
        data.loading = false;
        if (res.data) {
          res.data.forEach((item: any, index: any) => {
            item.index = index + 1;
          });
          data.bannerList = res.data;
        }
        data.loading = false;
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  const rowSelection = ref({
    onChange: (selectedRowKeys: (string | number)[], selectedRows: any[]) => {
      console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
      data.selectedRowKeys = selectedRowKeys;
    },
  });

  const handleAdd = () => {
    resetModal();
    modal.visible = true;
    modal.editFlag = false;
    modal.title = '新增轮播图';
    fileList.value = [];
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    modal.form.coverUrl = undefined;
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '编辑轮播图';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    for (const key in record) {
      if (record[key]) {
        modal.form[key] = record[key];
      }
    }

    // 显示已有图片
    if (modal.form.image) {
      modal.form.coverUrl = BASE_URL + '/api/staticfiles/banner/' + modal.form.image;
      modal.form.image = undefined;
    }
  };

  const confirmDelete = (record: any) => {
    deleteApi({ ids: record.id })
      .then((res) => {
        message.success('删除成功');
        getDataList();
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleBatchDelete = () => {
    if (data.selectedRowKeys.length <= 0) {
      message.warn('请勾选删除项');
      return;
    }
    deleteApi({ ids: data.selectedRowKeys.join(',') })
      .then((res) => {
        message.success('删除成功');
        data.selectedRowKeys = [];
        getDataList();
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  // 上传前检查
  const beforeUpload = (file: File) => {
    // 改文件名
    const fileName = new Date().getTime().toString() + '.' + file.type.substring(6);
    const copyFile = new File([file], fileName);
    console.log(copyFile);
    modal.form.imageFile = copyFile;

    // 创建预览URL
    modal.form.coverUrl = URL.createObjectURL(file);

    return false;
  };

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        // 创建FormData对象
        const formData = new FormData();
        if (modal.form.id) {
          formData.append('id', modal.form.id);
        }
        if (modal.form.thingId) {
          formData.append('thingId', modal.form.thingId);
        }
        if (modal.form.imageFile) {
          formData.append('imageFile', modal.form.imageFile);
        }

        if (modal.editFlag) {
          updateApi(formData)
            .then((res) => {
              hideModal();
              message.success('更新成功');
              getDataList();
            })
            .catch((err) => {
              console.log(err);
              message.error(err.msg || '操作失败');
            });
        } else {
          if (!modal.form.imageFile) {
            message.error('请上传轮播图');
            return;
          }
          createApi(formData)
            .then((res) => {
              hideModal();
              message.success('创建成功');
              getDataList();
            })
            .catch((err) => {
              console.log(err);
              message.error(err.msg || '操作失败');
            });
        }
      })
      .catch((err) => {
        console.log('表单验证失败', err);
      });
  };

  const handleCancel = () => {
    hideModal();
  };

  // 恢复表单初始状态
  const resetModal = () => {
    myform.value?.resetFields();
    fileList.value = [];
    modal.form.coverUrl = undefined;
  };

  // 关闭弹窗
  const hideModal = () => {
    modal.visible = false;
  };
</script>

<style scoped lang="less">
  .banner-container {
    padding: 24px;
    background-color: #f0f2f5;
    min-height: 100vh;
  }

  // 统计卡片样式
  .statistics-cards {
    margin-bottom: 24px;

    .stat-card {
      display: flex;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      background-color: #fff;
      height: 100%;
      transition: all 0.3s;
      overflow: hidden;
      position: relative;
      margin-bottom: 16px;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      }

      &.card-primary {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        color: #fff;
      }

      &.card-success {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        color: #fff;
      }

      &.card-warning {
        background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
        color: #fff;
      }

      &.card-danger {
        background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
        color: #fff;
      }

      .stat-icon {
        font-size: 36px;
        margin-right: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgba(255, 255, 255, 0.2);
        width: 60px;
        height: 60px;
        border-radius: 12px;
      }

      .stat-content {
        flex: 1;

        .stat-title {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          margin-bottom: 4px;
        }

        .stat-desc {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }

  // 筛选卡片样式
  .filter-card {
    margin-bottom: 24px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .filter-row {
      margin-bottom: -16px;
    }

    .filter-col {
      margin-bottom: 16px;
    }

    .filter-input,
    .filter-select {
      border-radius: 6px;
    }

    .filter-btns {
      display: flex;
      justify-content: flex-end;

      .btn-search,
      .btn-reset {
        border-radius: 6px;
        height: 32px;
        margin-left: 8px;
      }

      .btn-search {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        border: none;

        &:hover {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
        }
      }
    }
  }

  // 表格区域样式
  .page-view {
    background: #fff;
    padding: 24px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
  }

  .table-operations {
    margin-bottom: 16px;
    text-align: right;

    .btn-add,
    .btn-delete {
      border-radius: 6px;
      height: 36px;
    }

    .btn-add {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
      }
    }
  }

  // 表格样式
  .custom-table {
    :deep(.ant-table) {
      border-radius: 8px;
      overflow: hidden;

      .ant-table-thead > tr > th {
        background-color: #f5f7fa;
        font-weight: 600;
        color: #333;
      }

      .ant-table-tbody > tr:hover > td {
        background-color: #e6f7ff;
      }
    }
  }

  // 轮播图展示样式
  .banner-image-container {
    position: relative;
    width: 180px;
    height: 80px;
    margin: 0 auto;
    overflow: hidden;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .banner-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      opacity: 0;
      transition: opacity 0.3s;

      .preview-icon {
        color: #fff;
        font-size: 24px;
        cursor: pointer;
      }
    }

    &:hover {
      .banner-image {
        transform: scale(1.1);
      }

      .image-overlay {
        opacity: 1;
      }
    }
  }

  // 操作按钮样式
  .operation-btns {
    display: flex;
    justify-content: center;
    gap: 8px;

    .btn-edit,
    .btn-delete {
      border-radius: 4px;
    }
  }

  // 弹窗样式
  .banner-modal {
    :deep(.ant-modal-content) {
      border-radius: 12px;
      overflow: hidden;
    }

    :deep(.ant-modal-header) {
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-modal-body) {
      padding: 24px;
    }

    :deep(.ant-modal-footer) {
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
    }

    .banner-form {
      .upload-container {
        border-radius: 8px;
        overflow: hidden;

        :deep(.ant-upload-drag) {
          border-radius: 8px;
          border: 2px dashed #d9d9d9;

          &:hover {
            border-color: #1890ff;
          }
        }

        .preview-image {
          width: 100%;
          max-height: 200px;
          object-fit: contain;
          border-radius: 4px;
        }
      }

      .product-select {
        border-radius: 6px;
      }
    }
  }

  :deep(.ant-upload-drag-icon) {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 16px;

    img {
      max-width: 100%;
      max-height: 200px;
      object-fit: contain;
      border-radius: 4px;
    }
  }
</style>
