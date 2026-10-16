<template>
  <div class="admin-login-page">
    <section class="visual-panel" aria-hidden="true">
      <img src="../../assets/images/loginPhoto.png" alt="" class="visual-image" />
      <div class="visual-mask"></div>
      <div class="visual-copy">
        <div class="eyebrow">CAPED ADMIN</div>
        <h1>商城管理后台</h1>
        <p>统一管理商品、订单、运营与用户数据，让日常管理更清晰。</p>
        <div class="feature-row">
          <span>商品运营</span>
          <span>订单处理</span>
          <span>数据看板</span>
        </div>
      </div>
    </section>

    <section class="form-panel">
      <div class="form-shell">
        <div class="brand-header">
          <img src="../../assets/images/logo.png" alt="Caped" class="brand-logo" />
          <span class="portal-badge">ADMIN PORTAL</span>
        </div>

        <div class="login-title">
          <h2>管理员登录</h2>
          <p>请输入管理员账号和密码进入后台系统。</p>
        </div>

        <a-form
          ref="myform"
          layout="vertical"
          :model="data.loginForm"
          :rules="data.rules"
          :hideRequiredMark="true"
          class="login-form"
          @keydown.enter.prevent="handleSubmit"
        >
          <a-form-item name="username" :colon="false">
            <template #label>
              <span class="form-label">管理员账号</span>
            </template>
            <a-input
              size="large"
              placeholder="请输入管理员账号"
              v-model:value="data.loginForm.username"
              class="custom-input"
            >
              <template #prefix>
                <svg
                  class="input-icon"
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" :colon="false">
            <template #label>
              <span class="form-label">登录密码</span>
            </template>
            <a-input-password
              size="large"
              placeholder="请输入登录密码"
              v-model:value="data.loginForm.password"
              class="custom-input"
            >
              <template #prefix>
                <svg
                  class="input-icon"
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button class="login-button" :loading="loginBtn" size="large" block @click="handleSubmit">
              登录系统
              <span class="button-arrow">→</span>
            </a-button>
          </a-form-item>
        </a-form>

        <div class="form-footer">
          <span>需要进入用户端？</span>
          <button type="button" @click="goUserLogin">返回用户登录</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { useUserStore } from '/@/store';

  const router = useRouter();
  const userStore = useUserStore();
  const myform = ref();
  const loginBtn = ref(false);

  const data = reactive({
    loginForm: {
      username: '',
      password: '',
    },
    rules: {
      username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
      password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
    },
  });

  const handleSubmit = () => {
    myform.value
      ?.validate()
      .then(() => {
        loginBtn.value = true;
        handleLogin();
      })
      .catch(() => {
        message.warning('请填写完整的登录信息');
      });
  };

  const handleLogin = () => {
    userStore
      .adminLogin({
        username: data.loginForm.username,
        password: data.loginForm.password,
      })
      .then(() => {
        loginSuccess();
      })
      .catch((err) => {
        loginBtn.value = false;
        message.error(err.msg || '登录失败，请检查账号密码');
      });
  };

  const loginSuccess = () => {
    router.push({ path: '/admin' });
    message.success('登录成功');
  };

  const goUserLogin = () => {
    router.push({ name: 'login' });
  };
</script>

<style lang="less" scoped>
  .admin-login-page {
    position: relative;
    width: 100vw;
    height: 100vh;
    min-height: 640px;
    overflow: hidden;
    display: flex;
    background: #f5f7fb;
    color: #151922;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  .visual-panel {
    position: absolute;
    inset: 0 auto 0 0;
    width: 64vw;
    min-width: 720px;
    overflow: hidden;
    background: #121722;

    .visual-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transform: scale(1.02);
      filter: saturate(0.92) contrast(1.02);
    }

    .visual-mask {
      position: absolute;
      inset: 0;
      background:
        linear-gradient(90deg, rgba(15, 18, 24, 0.78) 0%, rgba(15, 18, 24, 0.42) 48%, rgba(15, 18, 24, 0.12) 100%),
        radial-gradient(circle at 28% 35%, rgba(255, 209, 0, 0.24), transparent 28%);
    }

    .visual-copy {
      position: absolute;
      left: clamp(56px, 7vw, 108px);
      bottom: clamp(70px, 10vh, 120px);
      max-width: 460px;
      color: #fff;
    }

    .eyebrow {
      color: #ffd100;
      font-size: 13px;
      font-weight: 800;
      letter-spacing: 0.18em;
      margin-bottom: 18px;
    }

    h1 {
      margin: 0;
      font-size: clamp(42px, 4vw, 68px);
      line-height: 1;
      font-weight: 900;
      letter-spacing: 0;
    }

    p {
      margin: 22px 0 0;
      color: rgba(255, 255, 255, 0.82);
      font-size: 16px;
      line-height: 1.9;
    }

    .feature-row {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 30px;

      span {
        height: 34px;
        padding: 0 16px;
        border: 1px solid rgba(255, 255, 255, 0.22);
        border-radius: 999px;
        display: inline-flex;
        align-items: center;
        background: rgba(255, 255, 255, 0.1);
        color: rgba(255, 255, 255, 0.9);
        font-size: 13px;
        backdrop-filter: blur(8px);
      }
    }
  }

  .form-panel {
    position: relative;
    z-index: 2;
    margin-left: 46vw;
    flex: 1;
    min-width: 520px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 64px clamp(42px, 6vw, 96px) 64px clamp(76px, 9vw, 132px);
    background: #fff;
    clip-path: polygon(18% 0, 100% 0, 100% 100%, 0 100%);
    filter: drop-shadow(-34px 0 48px rgba(16, 20, 28, 0.24));

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      z-index: -1;
      background:
        linear-gradient(135deg, rgba(255, 209, 0, 0.08), transparent 30%),
        #fff;
      clip-path: inherit;
    }
  }

  .form-shell {
    width: min(420px, 100%);
  }

  .brand-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 34px;

    .brand-logo {
      width: auto;
      height: 48px;
      display: block;
    }

    .portal-badge {
      padding: 8px 12px;
      border-radius: 999px;
      background: #f5f6f8;
      color: #6c7280;
      font-size: 12px;
      font-weight: 800;
      letter-spacing: 0.12em;
    }
  }

  .login-title {
    margin-bottom: 34px;

    h2 {
      margin: 0;
      color: #151922;
      font-size: 32px;
      line-height: 1.2;
      font-weight: 900;
      letter-spacing: 0;
    }

    p {
      margin: 12px 0 0;
      color: #828a98;
      font-size: 15px;
      line-height: 1.7;
    }
  }

  .login-form {
    :deep(.ant-form-item) {
      margin-bottom: 22px;
    }

    :deep(.ant-form-item-label) {
      padding-bottom: 8px;
    }

    :deep(.ant-form-item-explain-error) {
      padding-top: 4px;
      color: #d84b4b;
      font-size: 13px;
    }

    .form-label {
      color: #323844;
      font-size: 14px;
      font-weight: 700;
    }

    .input-icon {
      margin-right: 8px;
      color: #a4acb8;
      transition: color 0.2s ease;
    }

    .custom-input {
      height: 52px;
      border-radius: 10px;
      border: 1px solid #e5e8ef;
      background: #f8f9fb;
      color: #161b24;
      font-size: 15px;
      transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;

      &:hover {
        border-color: #d2d7e0;
        background: #fff;
      }

      &:focus,
      &:focus-within {
        border-color: #c49a22;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(255, 209, 0, 0.14);
      }

      &:focus-within .input-icon {
        color: #c49a22;
      }
    }

    :deep(.ant-input) {
      background: transparent;
      font-size: 15px;
    }

    :deep(.ant-input::placeholder) {
      color: #b2bac7;
    }
  }

  .login-button {
    height: 52px;
    margin-top: 6px;
    border: none;
    border-radius: 10px;
    background: linear-gradient(135deg, #171d28 0%, #2b313d 100%);
    color: #fff;
    font-size: 16px;
    font-weight: 800;
    letter-spacing: 0;
    box-shadow: 0 14px 28px rgba(22, 28, 38, 0.24);
    transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;

    &:hover,
    &:focus {
      color: #fff;
      background: linear-gradient(135deg, #111722 0%, #252b36 100%);
      box-shadow: 0 18px 34px rgba(22, 28, 38, 0.28);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 10px 18px rgba(22, 28, 38, 0.22);
    }

    .button-arrow {
      margin-left: 10px;
      color: #ffd100;
      font-size: 18px;
      font-weight: 900;
    }
  }

  .form-footer {
    margin-top: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #8b93a1;
    font-size: 14px;

    button {
      border: none;
      padding: 0;
      background: transparent;
      color: #1f2733;
      font-weight: 800;
      cursor: pointer;
      transition: color 0.2s ease;

      &:hover {
        color: #c49a22;
      }
    }
  }

  @media (max-width: 1100px) {
    .visual-panel {
      width: 58vw;
      min-width: 560px;
    }

    .form-panel {
      margin-left: 38vw;
      padding-left: 100px;
      min-width: 500px;
    }
  }

  @media (max-width: 820px) {
    .admin-login-page {
      min-height: 620px;
      justify-content: center;
      padding: 24px;
      background:
        linear-gradient(rgba(15, 18, 24, 0.62), rgba(15, 18, 24, 0.62)),
        url('../../assets/images/loginPhoto.png') center/cover no-repeat;
    }

    .visual-panel {
      display: none;
    }

    .form-panel {
      width: min(100%, 460px);
      min-width: 0;
      height: auto;
      margin: 0;
      padding: 34px 26px;
      border-radius: 24px;
      clip-path: none;
      filter: none;
      box-shadow: 0 24px 64px rgba(0, 0, 0, 0.28);

      &::before {
        clip-path: none;
        border-radius: 24px;
      }
    }

    .brand-header {
      justify-content: center;
      margin-bottom: 28px;
    }

    .login-title {
      text-align: center;

      h2 {
        font-size: 28px;
      }
    }
  }

  @media (max-width: 480px) {
    .admin-login-page {
      padding: 14px;
    }

    .form-panel {
      padding: 28px 20px;
      border-radius: 18px;
    }

    .brand-header {
      flex-direction: column;
      gap: 12px;
    }

    .login-title h2 {
      font-size: 25px;
    }
  }
</style>
