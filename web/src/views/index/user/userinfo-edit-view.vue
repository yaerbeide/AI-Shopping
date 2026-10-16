<template>
  <div class="content-list">
    <div class="list-title">设置</div>
    <a-spin :spinning="loading" style="min-height: 200px">
      <div class="list-content">
        <div class="edit-view">
          <div class="item flex-view">
            <div class="label">头像</div>
            <div class="right-box avatar-box flex-view">
              <img v-if="tData.form && tData.form.avatar" :src="tData.form.avatar" class="avatar" />
              <img v-else :src="AvatarIcon" class="avatar" />
              <div class="change-tips flex-view">
                <a-upload name="file" accept="image/*" :multiple="false" :before-upload="beforeUpload">
                  <label>点击更换头像</label>
                </a-upload>
                <p class="tip">图片格式支持 GIF、PNG、JPEG，尺寸不小于 200 PX，小于 4 MB</p>
              </div>
            </div>
          </div>
          <div class="item flex-view">
            <div class="label">昵称</div>
            <div class="right-box">
              <input type="text" v-model="tData.form.nickname" placeholder="请输入昵称" maxlength="20" class="input-dom" />
              <p class="tip">支持中英文，长度不能超过 20 个字符</p>
            </div>
          </div>
          <div class="item flex-view">
            <div class="label">手机号</div>
            <div class="right-box">
              <input type="text" v-model="tData.form.mobile" placeholder="请输入邮箱" maxlength="100" class="input-dom web-input" />
            </div>
          </div>
          <div class="item flex-view">
            <div class="label">邮箱</div>
            <div class="right-box">
              <input type="text" v-model="tData.form.email" placeholder="请输入邮箱" maxlength="100" class="input-dom web-input" />
            </div>
          </div>
          <div class="item flex-view">
            <div class="label">个人简介</div>
            <div class="right-box">
              <textarea v-model="tData.form.description" placeholder="请输入简介" maxlength="200" class="intro"> </textarea>
              <p class="tip">限制200字以内</p>
            </div>
          </div>
          <button class="save mg" @click="submit()">保存</button>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
  import { message } from 'ant-design-vue';
  import { detailApi, updateUserInfoApi } from '/@/api/user';
  import { BASE_URL } from '/@/store/constants';
  import { useUserStore } from '/@/store';
  import AvatarIcon from '/@/assets/images/avatar.jpg';

  const router = useRouter();
  const userStore = useUserStore();

  let loading = ref(false);
  let tData = reactive({
    form: {
      avatar: undefined,
      avatarFile: undefined,
      nickname: undefined,
      email: undefined,
      mobile: undefined,
      description: undefined,
    },
  });

  onMounted(() => {
    getUserInfo();
  });

  const beforeUpload = (file) => {
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

    // 改文件名
    const fileName = new Date().getTime().toString() + '.' + file.type.substring(6);
    const copyFile = new File([file], fileName);
    tData.form.avatarFile = copyFile;

    // 创建预览
    const reader = new FileReader();
    reader.onload = (e) => {
      tData.form.avatar = e.target.result;
    };
    reader.readAsDataURL(file);

    return false;
  };

  const getUserInfo = () => {
    loading.value = true;
    let userId = userStore.user_id;
    detailApi({ userId: userId })
      .then((res) => {
        tData.form = res.data;
        if (tData.form.avatar) {
          tData.form.avatar = BASE_URL + '/api/staticfiles/avatar/' + tData.form.avatar;
        }
        loading.value = false;
      })
      .catch((err) => {
        console.log(err);
        loading.value = false;
      });
  };
  const submit = () => {
    loading.value = true;
    let formData = new FormData();
    let userId = userStore.user_id;
    formData.append('id', userId);
    if (tData.form.avatarFile) {
      formData.append('avatarFile', tData.form.avatarFile);
    }
    if (tData.form.nickname) {
      formData.append('nickname', tData.form.nickname);
    }
    if (tData.form.email) {
      formData.append('email', tData.form.email);
    }
    if (tData.form.mobile) {
      formData.append('mobile', tData.form.mobile);
    }
    if (tData.form.description) {
      formData.append('description', tData.form.description);
    }
    updateUserInfoApi(formData)
      .then((res) => {
        message.success('保存成功');
        getUserInfo();
        loading.value = false;
      })
      .catch((err) => {
        console.log(err);
        loading.value = false;
        message.error('保存失败，请重试');
      });
  };
</script>

<style scoped lang="less">
  input,
  textarea {
    border-style: none;
    outline: none;
    margin: 0;
    padding: 0;
  }

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
      line-height: 48px;
      height: 48px;
      margin-bottom: 4px;
      border-bottom: 1px solid #cedce4;
    }

    .edit-view {
      .item {
        -webkit-box-align: center;
        -ms-flex-align: center;
        align-items: center;
        margin: 24px 0;

        .label {
          width: 100px;
          color: #152844;
          font-weight: 600;
          font-size: 14px;
        }

        .right-box {
          -webkit-box-flex: 1;
          -ms-flex: 1;
          flex: 1;
        }

        .avatar {
          width: 64px;
          height: 64px;
          border-radius: 50%;
          margin-right: 16px;
        }

        .change-tips {
          -webkit-box-align: center;
          -ms-flex-align: center;
          align-items: center;
          -ms-flex-wrap: wrap;
          flex-wrap: wrap;
        }

        label {
          color: #4684e2;
          font-size: 14px;
          line-height: 22px;
          height: 22px;
          cursor: pointer;
          width: 100px;
          display: block;
        }

        .tip {
          color: #6f6f6f;
          font-size: 14px;
          height: 22px;
          line-height: 22px;
          margin: 0;
          width: 100%;
        }

        .right-box {
          -webkit-box-flex: 1;
          -ms-flex: 1;
          flex: 1;
        }

        .input-dom {
          width: 400px;
        }

        .input-dom {
          background: #f8fafb;
          border-radius: 4px;
          height: 40px;
          line-height: 40px;
          font-size: 14px;
          color: #152844;
          padding: 0 12px;
        }

        .tip {
          font-size: 12px;
          line-height: 16px;
          color: #6f6f6f;
          height: 16px;
          margin-top: 4px;
        }

        .intro {
          resize: none;
          background: #f8fafb;
          width: 100%;
          padding: 8px 12px;
          height: 82px;
          line-height: 22px;
          font-size: 14px;
          color: #152844;
        }
      }

      .save {
        background: #1f2937;
        border-radius: 10px;
        width: 112px;
        height: 40px;
        line-height: 40px;
        font-size: 14px;
        color: #fff;
        border: none;
        outline: none;
        cursor: pointer;
        font-weight: 800;
        box-shadow: 0 10px 22px rgba(31, 41, 55, 0.16);
      }

      .mg {
        margin-left: 100px;
      }
    }
  }

  .content-list {
    .list-title {
      height: auto;
      line-height: 1.3;
      padding-bottom: 18px;
      margin-bottom: 22px;
      color: #101828;
      border-bottom: 1px solid #edf0f4;
      font-size: 24px;
      font-weight: 900;
    }

    .edit-view {
      max-width: 760px;

      .item {
        margin: 0;
        padding: 18px 0;
        border-bottom: 1px solid #f0f2f5;

        &:first-child {
          padding-top: 0;
        }

        .label {
          width: 118px;
          color: #344054;
          font-weight: 800;
        }

        .avatar {
          width: 72px;
          height: 72px;
          border: 3px solid #fff;
          box-shadow: 0 10px 24px rgba(16, 24, 40, 0.12);
        }

        label {
          color: #1f2937;
          font-weight: 800;
        }

        .tip {
          color: #98a2b3;
        }

        .input-dom,
        .intro {
          width: min(100%, 460px);
          background: #fff;
          border: 1px solid #d0d5dd;
          border-radius: 12px;
          color: #101828;
          transition: border-color 0.2s ease, box-shadow 0.2s ease;

          &:focus {
            border-color: #667085;
            box-shadow: 0 0 0 3px rgba(16, 24, 40, 0.06);
          }
        }

        .input-dom {
          height: 44px;
          line-height: 44px;
        }

        .intro {
          min-height: 104px;
          line-height: 1.7;
        }
      }

      .mg {
        margin-left: 118px;
        margin-top: 22px;
      }
    }
  }

  @media (max-width: 720px) {
    .content-list .edit-view {
      .item {
        align-items: flex-start;
        flex-direction: column;
        gap: 10px;

        .label {
          width: 100%;
        }

        .input-dom,
        .intro {
          width: 100%;
        }
      }

      .mg {
        margin-left: 0;
      }
    }
  }
</style>
