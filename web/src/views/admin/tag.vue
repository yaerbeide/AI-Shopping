<template>
  <div class="tag-management">
    <div class="page-header">
      <div class="page-title">
        <tag-outlined />
        <span>标签管理</span>
      </div>
      <div class="page-stats">
        <div class="stat-item">
          <div class="stat-value">{{ data.tagList.length }}</div>
          <div class="stat-label">标签总数</div>
        </div>
        <a-divider type="vertical" class="stats-divider" />
        <div class="stat-info">
          <clock-circle-outlined />
          <span>最近更新: {{ getLastUpdateTime() }}</span>
        </div>
      </div>
    </div>

    <div class="content-layout">
      <div class="left-panel">
        <a-card :bordered="false" class="tag-cloud-card">
          <template #title>
            <div class="card-title">
              <cluster-outlined />
              <span>标签云</span>
            </div>
          </template>
          <template #extra>
            <a-button type="text">
              <template #icon><reload-outlined /></template>
            </a-button>
          </template>
          <div class="tag-cloud-container">
            <a-spin :spinning="data.loading" tip="加载中...">
              <div class="tag-cloud">
                <a-tag
                  v-for="tag in data.tagList"
                  :key="tag.id"
                  :color="getRandomColor(tag.id)"
                  class="cloud-tag"
                  @click="handleTagClick(tag)"
                >
                  {{ tag.title }}
                </a-tag>
              </div>
            </a-spin>
          </div>
          <div class="tag-info-panel" v-if="selectedTag">
            <div class="tag-info-header">
              <a-tag :color="getRandomColor(selectedTag.id)" class="selected-tag">
                {{ selectedTag.title }}
              </a-tag>
              <div class="tag-actions">
                <a-button type="link" size="small" @click="handleEdit(selectedTag)">
                  <template #icon><edit-outlined /></template>
                  编辑
                </a-button>
                <a-popconfirm title="确定要删除此标签吗?" ok-text="是" cancel-text="否" @confirm="confirmDelete(selectedTag)">
                  <a-button type="link" danger size="small">
                    <template #icon><delete-outlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
            </div>
            <div class="tag-statistics">
              <div class="stat-row">
                <span class="stat-label">关联商品:</span>
                <span class="stat-value">{{ getRandomNumber(50) }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">使用次数:</span>
                <span class="stat-value">{{ getRandomNumber(200) }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">创建时间:</span>
                <span class="stat-value">{{ getFormattedDate() }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <div class="right-panel">
        <a-card :bordered="false" class="card-container">
          <div class="table-toolbar">
            <div class="left-actions">
              <a-button type="primary" @click="handleAdd">
                <template #icon><plus-outlined /></template>
                新增标签
              </a-button>
              <a-button danger :disabled="data.selectedRowKeys.length === 0" @click="handleBatchDelete">
                <template #icon><delete-outlined /></template>
                批量删除
              </a-button>
              <a-button @click="getDataList">
                <template #icon><reload-outlined /></template>
                刷新
              </a-button>
            </div>
            <div class="right-actions">
              <a-input-search placeholder="搜索标签名称" style="width: 250px" @search="onSearch" @change="onSearchChange" allow-clear>
                <template #prefix>
                  <search-outlined />
                </template>
              </a-input-search>
            </div>
          </div>

          <a-table
            class="custom-table"
            size="middle"
            rowKey="id"
            :loading="data.loading"
            :columns="columns"
            :data-source="data.tagList"
            :row-selection="rowSelection"
            :pagination="{
              size: 'default',
              current: data.page,
              pageSize: data.pageSize,
              onChange: (current) => (data.page = current),
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50'],
              showTotal: (total) => `共 ${total} 条记录`,
            }"
          >
            <template #bodyCell="{ text, record, column }">
              <template v-if="column.key === 'title'">
                <div class="tag-name">
                  <a-tag :color="getRandomColor(record.id)">{{ text }}</a-tag>
                </div>
              </template>
              <template v-if="column.key === 'usage'">
                <a-progress :percent="getRandomNumber(100)" size="small" :showInfo="false" :strokeColor="getRandomColor(record.id)" />
              </template>
              <template v-if="column.key === 'operation'">
                <div class="table-actions">
                  <a-tooltip title="编辑">
                    <a-button type="link" size="small" @click="handleEdit(record)">
                      <template #icon><edit-outlined /></template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="删除">
                    <a-popconfirm title="确定要删除此标签吗?" ok-text="是" cancel-text="否" @confirm="confirmDelete(record)">
                      <a-button type="link" danger size="small">
                        <template #icon><delete-outlined /></template>
                      </a-button>
                    </a-popconfirm>
                  </a-tooltip>
                </div>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </div>

    <a-modal
      :visible="modal.visible"
      :forceRender="true"
      :title="modal.title"
      :maskClosable="false"
      :width="480"
      ok-text="确认"
      cancel-text="取消"
      @cancel="handleCancel"
      @ok="handleOk"
      centered
    >
      <div class="modal-form-wrapper">
        <a-form ref="myform" :model="modal.form" :rules="modal.rules" layout="vertical">
          <a-form-item label="标签名称" name="title">
            <a-input placeholder="请输入标签名称" v-model:value="modal.form.title" allow-clear>
              <template #prefix><tag-outlined /></template>
            </a-input>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi } from '/@/api/tag';
  import { ref, reactive, onMounted } from 'vue';
  import {
    TagOutlined,
    PlusOutlined,
    DeleteOutlined,
    EditOutlined,
    SearchOutlined,
    ReloadOutlined,
    ClockCircleOutlined,
    ClusterOutlined,
  } from '@ant-design/icons-vue';

  const columns = reactive([
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      align: 'center',
      width: 80,
    },
    {
      title: '标签名称',
      dataIndex: 'title',
      key: 'title',
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      align: 'center',
      width: 180,
      customRender: () => getFormattedDate(),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 120,
    },
  ]);

  // 页面数据
  const data = reactive({
    tagList: [],
    loading: false,
    keyword: '',
    selectedRowKeys: [] as any[],
    pageSize: 10,
    page: 1,
  });

  // 弹窗数据源
  const modal = reactive({
    visible: false,
    editFlag: false,
    title: '',
    form: {
      id: undefined,
      title: undefined,
      color: '#1890ff',
      description: undefined,
    },
    rules: {
      title: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
    },
  });

  const myform = ref<FormInstance>();
  const selectedTag = ref(null);

  onMounted(() => {
    getDataList();
  });

  const getDataList = () => {
    data.loading = true;
    listApi({
      keyword: data.keyword,
    })
      .then((res) => {
        data.loading = false;
        console.log(res);
        if (res.data) {
          res.data.forEach((item: any, index: any) => {
            item.index = index + 1;
          });
          data.tagList = res.data;
        }
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  // 获取最后更新时间
  const getLastUpdateTime = () => {
    return new Date().toLocaleString();
  };

  // 获取格式化日期
  const getFormattedDate = () => {
    const date = new Date();
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };

  // 获取随机颜色
  const getRandomColor = (id) => {
    const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96'];
    return colors[id % colors.length];
  };

  // 获取随机数字
  const getRandomNumber = (max) => {
    return Math.floor(Math.random() * max) + 1;
  };

  // 处理标签点击
  const handleTagClick = (tag) => {
    selectedTag.value = tag;
  };

  const onSearchChange = (e: Event) => {
    data.keyword = e?.target?.value;
    console.log(data.keyword);
  };

  const onSearch = () => {
    getDataList();
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
    modal.title = '新增标签';
    // 重置
    for (const key in modal.form) {
      if (key === 'color') {
        modal.form[key] = '#1890ff';
      } else {
        modal.form[key] = undefined;
      }
    }
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '编辑标签';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    for (const key in record) {
      modal.form[key] = record[key];
    }
    // 如果没有这些字段，设置默认值
    if (modal.form.color === undefined) modal.form.color = '#1890ff';
  };

  const confirmDelete = (record: any) => {
    console.log('delete', record);
    deleteApi({ ids: record.id })
      .then((res) => {
        message.success('删除成功');
        getDataList();
        if (selectedTag.value && selectedTag.value.id === record.id) {
          selectedTag.value = null;
        }
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleBatchDelete = () => {
    console.log(data.selectedRowKeys);
    if (data.selectedRowKeys.length <= 0) {
      message.warn('请勾选要删除的标签');
      return;
    }
    deleteApi({ ids: data.selectedRowKeys.join(',') })
      .then((res) => {
        message.success('批量删除成功');
        data.selectedRowKeys = [];
        getDataList();
        selectedTag.value = null;
      })
      .catch((err) => {
        message.error(err.msg || '操作失败');
      });
  };

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        if (modal.editFlag) {
          updateApi({ id: modal.form.id }, modal.form)
            .then((res) => {
              message.success('更新成功');
              hideModal();
              getDataList();
            })
            .catch((err) => {
              message.error(err.msg || '操作失败');
            });
        } else {
          createApi(modal.form)
            .then((res) => {
              message.success('创建成功');
              hideModal();
              getDataList();
            })
            .catch((err) => {
              message.error(err.msg || '操作失败');
            });
        }
      })
      .catch((err) => {
        console.log('验证失败', err);
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
</script>

<style scoped lang="less">
  .tag-management {
    padding: 0;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: #fff;
      padding: 24px;
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #1f2937;
        display: flex;
        align-items: center;

        :deep(svg) {
          margin-right: 10px;
          font-size: 22px;
        }
      }

      .page-stats {
        display: flex;
        align-items: center;

        .stat-item {
          text-align: center;
          padding: 0 20px;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #1890ff;
            line-height: 1.2;
          }

          .stat-label {
            font-size: 13px;
            color: #6b7280;
          }
        }

        .stats-divider {
          height: 40px;
          margin: 0 20px;
        }

        .stat-info {
          display: flex;
          align-items: center;
          font-size: 13px;
          color: #6b7280;

          :deep(svg) {
            margin-right: 6px;
          }
        }
      }
    }

    .content-layout {
      display: flex;
      gap: 24px;

      .left-panel {
        width: 300px;
        flex-shrink: 0;

        .tag-cloud-card {
          border-radius: 8px;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
          height: 100%;

          :deep(.ant-card-head) {
            border-bottom: 1px solid #f0f0f0;

            .card-title {
              display: flex;
              align-items: center;
              font-size: 16px;
              font-weight: 600;

              svg {
                margin-right: 8px;
                font-size: 16px;
              }
            }
          }

          .tag-cloud-container {
            padding: 16px 0;
            min-height: 200px;

            .tag-cloud {
              display: flex;
              flex-wrap: wrap;
              gap: 8px;

              .cloud-tag {
                cursor: pointer;
                margin: 0;
                transition: all 0.3s;

                &:hover {
                  transform: scale(1.05);
                }
              }
            }
          }

          .tag-info-panel {
            margin-top: 16px;
            padding: 16px;
            background-color: #f9fafb;
            border-radius: 8px;

            .tag-info-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 16px;

              .selected-tag {
                font-size: 16px;
                padding: 4px 12px;
              }

              .tag-actions {
                display: flex;
                gap: 8px;
              }
            }

            .tag-statistics {
              .stat-row {
                display: flex;
                margin-bottom: 8px;

                .stat-label {
                  width: 80px;
                  color: #6b7280;
                }

                .stat-value {
                  font-weight: 500;
                }
              }
            }
          }
        }
      }

      .right-panel {
        flex: 1;
      }
    }

    .card-container {
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

      .table-toolbar {
        display: flex;
        justify-content: space-between;
        margin-bottom: 24px;

        .left-actions {
          display: flex;
          gap: 8px;
        }
      }

      .custom-table {
        :deep(.ant-table) {
          border-radius: 8px;
          overflow: hidden;
        }

        :deep(.ant-table-thead > tr > th) {
          background-color: #f9fafb;
          color: #374151;
          font-weight: 600;
        }

        :deep(.ant-table-tbody > tr:hover > td) {
          background-color: #f5f7ff;
        }

        .table-actions {
          display: flex;
          justify-content: center;

          .ant-btn {
            padding: 0 4px;
          }
        }

        .tag-name {
          display: flex;
          justify-content: center;
        }
      }
    }

    .modal-form-wrapper {
      padding: 8px;

      :deep(.ant-form-item-label > label) {
        font-weight: 500;
      }
    }
  }
</style>
