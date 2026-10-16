<template>
  <div class="user-management">
    <div class="page-header">
      <div class="page-title">
        <team-outlined />
        <span>用户管理</span>
      </div>
      <div class="page-stats">
        <div class="stat-item">
          <div class="stat-value">{{ data.userList.length }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ getActiveUsers() }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
        <a-divider type="vertical" class="stats-divider" />
        <div class="stat-info">
          <clock-circle-outlined />
          <span>最近更新: {{ getLastUpdateTime() }}</span>
        </div>
      </div>
    </div>

    <a-card :bordered="false" class="card-container">
      <div class="table-toolbar">
        <div class="left-actions">
          <a-button type="primary" @click="handleAdd">
            <template #icon><user-add-outlined /></template>
            新增用户
          </a-button>
          <a-button danger :disabled="data.selectedRowKeys.length === 0" @click="handleBatchDelete">
            <template #icon><delete-outlined /></template>
            批量删除
          </a-button>
          <a-button>
            <template #icon><reload-outlined /></template>
            刷新
          </a-button>
        </div>
        <div class="right-actions">
          <a-input-search placeholder="搜索用户名/邮箱/手机" style="width: 320px" @search="onSearch" @change="onSearchChange" allow-clear>
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
        :data-source="data.userList"
        :row-selection="rowSelection"
        :pagination="{
          size: 'default',
          current: data.page,
          pageSize: data.pageSize,
          onChange: (current) => (data.page = current),
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          showTotal: (total) => `共 ${total} 条记录`,
        }"
      >
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.key === 'role'">
            <a-tag :color="getRoleColor(text)">
              <span v-if="text === '1'">普通用户</span>
              <span v-if="text === '2'">演示帐号</span>
              <span v-if="text === '3'">管理员</span>
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-badge :status="text === '0' ? 'success' : 'error'" />
            <span>{{ text === '0' ? '正常' : '封号' }}</span>
          </template>
          <template v-if="column.key === 'operation'">
            <div class="table-actions">
              <a-tooltip title="编辑">
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <template #icon><edit-outlined /></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="删除">
                <a-popconfirm title="确定要删除此用户吗?" ok-text="是" cancel-text="否" @confirm="confirmDelete(record)">
                  <a-button type="link" danger size="small">
                    <template #icon><delete-outlined /></template>
                  </a-button>
                </a-popconfirm>
              </a-tooltip>
              <a-tooltip title="重置密码">
                <a-button type="link" size="small">
                  <template #icon><key-outlined /></template>
                </a-button>
              </a-tooltip>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      :visible="modal.visible"
      :forceRender="true"
      :title="modal.title"
      :maskClosable="false"
      :width="580"
      ok-text="确认"
      cancel-text="取消"
      @cancel="handleCancel"
      @ok="handleOk"
      centered
    >
      <div class="modal-form-wrapper">
        <a-form ref="myform" :model="modal.form" :rules="modal.rules" layout="vertical">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="用户名" name="username">
                <a-input
                  :disabled="modal.editFlag"
                  placeholder="请输入用户名"
                  v-model:value="modal.form.username"
                  allowClear
                  autocomplete="off"
                >
                  <template #prefix><user-outlined /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12" v-if="!modal.editFlag">
              <a-form-item label="密码" name="password">
                <a-input placeholder="请输入密码" type="password" v-model:value="modal.form.password" allowClear autocomplete="off">
                  <template #prefix><lock-outlined /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="昵称" name="nickname">
                <a-input placeholder="请输入昵称" v-model:value="modal.form.nickname" allowClear>
                  <template #prefix><smile-outlined /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="角色" name="role">
                <a-select placeholder="请选择角色" v-model:value="modal.form.role" allowClear>
                  <template v-for="item in modal.roleData" :key="item.id">
                    <a-select-option :value="item.id">{{ item.title }}</a-select-option>
                  </template>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="状态" name="status">
                <a-select placeholder="请选择状态" v-model:value="modal.form.status" allowClear>
                  <a-select-option key="0" value="0">正常</a-select-option>
                  <a-select-option key="1" value="1">封号</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="邮箱" name="email">
                <a-input placeholder="请输入邮箱" v-model:value="modal.form.email" allowClear>
                  <template #prefix><mail-outlined /></template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="手机号" name="mobile">
                <a-input placeholder="请输入手机号" v-model:value="modal.form.mobile" allowClear>
                  <template #prefix><phone-outlined /></template>
                </a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item label="头像">
                <a-upload list-type="picture-card" :file-list="fileList" :before-upload="beforeUpload">
                  <div v-if="fileList.length < 1">
                    <plus-outlined />
                    <div style="margin-top: 8px">上传</div>
                  </div>
                </a-upload>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, message } from 'ant-design-vue';
  import { createApi, listApi, updateApi, deleteApi } from '/@/api/user';
  import { getFormatTime } from '/@/utils';
  import { ref, reactive, onMounted } from 'vue';
  import {
    TeamOutlined,
    UserOutlined,
    UserAddOutlined,
    DeleteOutlined,
    EditOutlined,
    SearchOutlined,
    ReloadOutlined,
    MailOutlined,
    PhoneOutlined,
    LockOutlined,
    SmileOutlined,
    ClockCircleOutlined,
    KeyOutlined,
    PlusOutlined,
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
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      align: 'center',
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
      key: 'nickname',
      align: 'center',
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      align: 'center',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      align: 'center',
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      key: 'mobile',
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      align: 'center',
      customRender: ({ text }) => getFormatTime(text, true),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'operation',
      align: 'center',
      fixed: 'right',
      width: 160,
    },
  ]);

  const beforeUpload = (file: File) => {
    // 改文件名
    const fileName = new Date().getTime().toString() + '.' + file.type.substring(6);
    const copyFile = new File([file], fileName);
    console.log(copyFile);
    modal.form.cover = copyFile;
    return false;
  };

  const fileList = ref([]);

  // 页面数据
  const data = reactive({
    userList: [],
    loading: false,
    currentAdminUserName: '',
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
    roleData: [
      {
        id: '1',
        title: '普通用户',
      },
      {
        id: '2',
        title: '演示帐号',
      },
      {
        id: '3',
        title: '管理员',
      },
    ],
    form: {
      id: undefined,
      username: undefined,
      password: undefined,
      role: undefined,
      status: undefined,
      nickname: undefined,
      email: undefined,
      mobile: undefined,
    },
    rules: {
      username: [{ required: true, message: '请输入用户名', trigger: 'change' }],
      password: [{ required: true, message: '请输入密码', trigger: 'change' }],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }],
    },
  });

  const myform = ref<FormInstance>();

  onMounted(() => {
    getUserList();
  });

  const getUserList = () => {
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
          data.userList = res.data;
        }
      })
      .catch((err) => {
        data.loading = false;
        console.log(err);
      });
  };

  // 获取活跃用户数量
  const getActiveUsers = () => {
    return data.userList.filter((user) => user.status === '0').length;
  };

  // 获取最后更新时间
  const getLastUpdateTime = () => {
    return new Date().toLocaleString();
  };

  // 获取角色标签颜色
  const getRoleColor = (role) => {
    switch (role) {
      case '1':
        return 'blue';
      case '2':
        return 'green';
      case '3':
        return 'purple';
      default:
        return 'default';
    }
  };

  const onSearchChange = (e: Event) => {
    data.keyword = e?.target?.value;
    console.log(data.keyword);
  };

  const onSearch = () => {
    getUserList();
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
    modal.title = '新增用户';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
  };

  const handleEdit = (record: any) => {
    resetModal();
    modal.visible = true;
    modal.editFlag = true;
    modal.title = '编辑用户';
    // 重置
    for (const key in modal.form) {
      modal.form[key] = undefined;
    }
    for (const key in record) {
      modal.form[key] = record[key];
    }
  };

  const confirmDelete = (record: any) => {
    console.log('delete', record);
    deleteApi({ ids: record.id })
      .then((res) => {
        message.success('删除成功');
        getUserList();
      })
      .catch((err) => {
        message.warn(err.msg || '操作失败');
      });
  };

  const handleBatchDelete = () => {
    console.log(data.selectedRowKeys);
    if (data.selectedRowKeys.length <= 0) {
      console.log('hello');
      message.warn('请勾选要删除的项目');
      return;
    }
    deleteApi({ ids: data.selectedRowKeys.join(',') })
      .then((res) => {
        message.success('批量删除成功');
        data.selectedRowKeys = [];
        getUserList();
      })
      .catch((err) => {
        message.warn(err.msg || '操作失败');
      });
  };

  const handleOk = () => {
    myform.value
      ?.validate()
      .then(() => {
        const formData = new FormData();
        if (modal.form.id) {
          formData.append('id', modal.form.id);
        }
        if (modal.form.username) {
          formData.append('username', modal.form.username);
        }
        if (modal.form.password) {
          formData.append('password', modal.form.password);
        }
        if (modal.form.nickname) {
          formData.append('nickname', modal.form.nickname);
        }
        if (modal.form.role) {
          formData.append('role', modal.form.role);
        }
        if (modal.form.status) {
          formData.append('status', modal.form.status);
        }
        if (modal.form.cover) {
          formData.append('cover', modal.form.cover);
        }
        if (modal.form.mobile) {
          formData.append('mobile', modal.form.mobile);
        }
        if (modal.form.email) {
          formData.append('email', modal.form.email);
        }
        if (modal.editFlag) {
          updateApi(formData)
            .then((res) => {
              message.success('更新成功');
              hideModal();
              getUserList();
            })
            .catch((err) => {
              console.log(err);
              message.warn(err.msg || '操作失败');
            });
        } else {
          createApi(formData)
            .then((res) => {
              message.success('创建成功');
              hideModal();
              getUserList();
            })
            .catch((err) => {
              console.log(err);
              message.warn(err.msg || '操作失败');
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
  .user-management {
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
