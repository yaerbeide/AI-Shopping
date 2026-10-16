<template>
  <div class="profile-container">
    <div class="page-header">
      <div class="page-title">
        <user-outlined />
        <span>个人信息</span>
      </div>
    </div>

    <a-spin :spinning="loading">
      <div class="profile-content">
        <a-row :gutter="24">
          <a-col :xs="24" :sm="24" :md="8" :lg="8" :xl="6">
            <a-card :bordered="false" class="profile-card">
              <div class="profile-avatar-container">
                <div class="avatar-wrapper">
                  <img v-if="profileForm.avatar" :src="profileForm.avatar" class="profile-avatar-img" />
                  <a-avatar v-else :size="120" class="profile-avatar">
                    <template #icon><user-outlined /></template>
                  </a-avatar>
                </div>
                <div class="profile-name">{{ userStore.admin_user_name }}</div>
                <div class="profile-role">系统管理员</div>

                <!-- 上传头像表单 -->
                <a-form ref="avatarForm" enctype="multipart/form-data">
                  <a-form-item>
                    <a-upload name="avatarFile" accept="image/*" :multiple="false" :before-upload="beforeUpload" :show-upload-list="false">
                      <a-button type="primary" class="upload-btn">
                        <template #icon><upload-outlined /></template>
                        更换头像
                      </a-button>
                    </a-upload>
                  </a-form-item>

                  <a-form-item v-if="profileForm.avatarFile">
                    <a-button type="primary" @click="uploadAvatar" :loading="avatarUploading"> 保存头像 </a-button>
                  </a-form-item>
                </a-form>

                <div class="upload-tip">支持 JPG、PNG 格式，小于 4MB</div>
              </div>
              <a-divider />
              <div class="profile-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ loginDays }}</div>
                  <div class="stat-label">登录天数</div>
                </div>
                <a-divider type="vertical" />
                <div class="stat-item">
                  <div class="stat-value">{{ lastLoginTime }}</div>
                  <div class="stat-label">上次登录</div>
                </div>
              </div>
            </a-card>
          </a-col>

          <a-col :xs="24" :sm="24" :md="16" :lg="16" :xl="18">
            <a-card :bordered="false" class="info-card">
              <a-tabs default-active-key="1">
                <a-tab-pane key="1" tab="基本信息">
                  <a-form :model="profileForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
                    <a-form-item label="用户名">
                      <a-input v-model:value="profileForm.username" placeholder="用户名" />
                    </a-form-item>
                    <a-form-item label="真实姓名">
                      <a-input v-model:value="profileForm.realName" placeholder="真实姓名" />
                    </a-form-item>
                    <a-form-item label="手机号码">
                      <a-input v-model:value="profileForm.phone" placeholder="手机号码" />
                    </a-form-item>
                    <a-form-item label="电子邮箱">
                      <a-input v-model:value="profileForm.email" placeholder="电子邮箱" />
                    </a-form-item>
                    <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
                      <a-button type="primary" @click="saveProfile">
                        <template #icon><save-outlined /></template>
                        保存修改
                      </a-button>
                    </a-form-item>
                  </a-form>
                </a-tab-pane>
              </a-tabs>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { useUserStore } from '/@/store';
  import { detailApi, updateApi, updateUserPwdApi } from '/@/api/user';
  import { BASE_URL } from '/@/store/constants';
  import { UserOutlined, UploadOutlined, SaveOutlined, LockOutlined } from '@ant-design/icons-vue';

  const userStore = useUserStore();
  const loading = ref(false);
  const avatarUploading = ref(false);
  const avatarForm = ref(null);

  // 模拟登录天数和上次登录时间
  const loginDays = ref(365);
  const lastLoginTime = ref('今天');

  // 个人信息表单
  const profileForm = reactive({
    username: userStore.admin_user_name || '',
    realName: '管理员',
    phone: '13800138000',
    email: 'admin@example.com',
    avatar: undefined,
    avatarFile: undefined,
  });

  // 修改密码表单
  const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  // 上传前检查
  const beforeUpload = (file: File) => {
    // 检查文件类型
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件!');
      return false;
    }

    // 检查文件大小
    const isLt4M = file.size / 1024 / 1024 < 4;
    if (!isLt4M) {
      message.error('图片大小不能超过4MB!');
      return false;
    }

    // 改文件名 - 确保文件扩展名正确
    let fileExt = 'jpg';
    if (file.type === 'image/png') fileExt = 'png';
    if (file.type === 'image/jpeg') fileExt = 'jpg';
    if (file.type === 'image/gif') fileExt = 'gif';

    const fileName = new Date().getTime().toString() + '.' + fileExt;
    const copyFile = new File([file], fileName);
    profileForm.avatarFile = copyFile;

    console.log('头像文件准备上传:', fileName, file.type);

    // 创建预览
    const reader = new FileReader();
    reader.onload = (e) => {
      profileForm.avatar = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    return false;
  };

  // 单独上传头像
  const uploadAvatar = () => {
    if (!profileForm.avatarFile) {
      message.error('请先选择头像图片');
      return;
    }

    avatarUploading.value = true;
    const formData = new FormData();
    formData.append('id', userStore.admin_user_id);
    formData.append('avatarFile', profileForm.avatarFile);

    console.log('正在单独上传头像:', profileForm.avatarFile.name);

    updateApi(formData)
      .then((res) => {
        console.log('头像上传响应:', res);
        message.success('头像上传成功');

        // 刷新用户信息
        getUserInfo();

        // 发布自定义事件，通知其他组件头像已更新
        const event = new CustomEvent('avatar-updated', { detail: { success: true } });
        window.dispatchEvent(event);

        avatarUploading.value = false;
      })
      .catch((err) => {
        console.log('头像上传失败:', err);
        avatarUploading.value = false;
        message.error('头像上传失败，请重试');
      });
  };

  // 获取用户信息
  const getUserInfo = () => {
    loading.value = true;
    const userId = userStore.admin_user_id;

    console.log('获取管理员信息, ID:', userId);

    // 使用detailApi获取用户详情
    detailApi({ userId: userId })
      .then((res) => {
        console.log('获取到的用户信息:', res.data);
        const userData = res.data;

        // 更新表单数据
        profileForm.username = userData.username || userStore.admin_user_name;
        profileForm.realName = userData.realName || '管理员';
        profileForm.phone = userData.mobile || '13800138000';
        profileForm.email = userData.email || 'admin@example.com';

        // 清除之前选择但未上传的头像文件
        profileForm.avatarFile = undefined;

        // 处理头像
        if (userData.avatar) {
          console.log('用户头像路径:', userData.avatar);
          profileForm.avatar = BASE_URL + '/api/staticfiles/avatar/' + userData.avatar;
        } else {
          console.log('用户没有头像');
          profileForm.avatar = undefined;
        }

        loading.value = false;
      })
      .catch((err) => {
        console.log('获取用户信息失败:', err);
        loading.value = false;
        message.error('获取个人信息失败');

        // 使用默认值
        profileForm.username = userStore.admin_user_name || 'admin';
      });
  };

  // 保存个人信息 (不包括头像)
  const saveProfile = () => {
    loading.value = true;

    const formData = new FormData();
    const userId = userStore.admin_user_id;

    formData.append('id', userId);

    if (profileForm.username) {
      formData.append('username', profileForm.username);
    }

    if (profileForm.realName) {
      formData.append('realName', profileForm.realName);
    }

    if (profileForm.phone) {
      formData.append('mobile', profileForm.phone);
    }

    if (profileForm.email) {
      formData.append('email', profileForm.email);
    }

    // 检查FormData内容
    console.log('表单数据内容:');
    for (let pair of formData.entries()) {
      console.log(pair[0], pair[1]);
    }

    // 使用updateApi更新用户信息
    updateApi(formData)
      .then((res) => {
        console.log('保存响应:', res);
        message.success('保存成功');
        getUserInfo();

        // 更新store中的用户名，确保界面同步更新
        if (profileForm.username) {
          userStore.$patch((state) => {
            state.admin_user_name = profileForm.username;
          });
          localStorage.setItem('ADMIN_USER_NAME', profileForm.username);
        }

        loading.value = false;
      })
      .catch((err) => {
        console.log('保存错误:', err);
        loading.value = false;
        message.error(err.msg || '保存失败，请重试');
      });
  };

  // 修改密码
  const changePassword = () => {
    if (!passwordForm.oldPassword) {
      message.error('请输入当前密码');
      return;
    }

    if (!passwordForm.newPassword) {
      message.error('请输入新密码');
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      message.error('两次输入的新密码不一致');
      return;
    }

    loading.value = true;

    const params = {
      userId: userStore.admin_user_id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    };

    console.log('准备修改密码:', params);

    updateUserPwdApi(params)
      .then((res) => {
        console.log('密码修改响应:', res);
        message.success('密码修改成功');
        passwordForm.oldPassword = '';
        passwordForm.newPassword = '';
        passwordForm.confirmPassword = '';
        loading.value = false;
      })
      .catch((err) => {
        console.log('密码修改失败:', err);
        loading.value = false;
        message.error(err.msg || '密码修改失败，请重试');
      });
  };

  onMounted(() => {
    getUserInfo();
  });
</script>

<style scoped lang="less">
  .profile-container {
    padding: 0 16px;
  }

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-size: 20px;
      font-weight: 500;
      display: flex;
      align-items: center;

      .anticon {
        margin-right: 8px;
        font-size: 24px;
      }
    }
  }

  .profile-card {
    margin-bottom: 24px;
    border-radius: 8px;

    .profile-avatar-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px 0;

      .avatar-wrapper {
        margin-bottom: 16px;

        .profile-avatar {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        }

        .profile-avatar-img {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          object-fit: cover;
          box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
        }
      }

      .profile-name {
        font-size: 20px;
        font-weight: 500;
        margin-bottom: 8px;
      }

      .profile-role {
        color: rgba(0, 0, 0, 0.45);
        margin-bottom: 16px;
      }

      .upload-btn {
        border-radius: 20px;
      }

      .upload-tip {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
        margin-top: 8px;
      }
    }

    .profile-stats {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 16px 0;

      .stat-item {
        text-align: center;
        flex: 1;

        .stat-value {
          font-size: 20px;
          font-weight: 500;
          color: #1890ff;
        }

        .stat-label {
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .ant-divider-vertical {
        height: 40px;
      }
    }
  }

  .info-card {
    border-radius: 8px;

    :deep(.ant-tabs-nav) {
      margin-bottom: 24px;
    }

    :deep(.ant-form-item:last-child) {
      margin-bottom: 0;
      margin-top: 24px;
    }
  }
</style>
