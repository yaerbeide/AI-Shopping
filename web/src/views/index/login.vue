<template>
  <div class="login-page">
    <!-- 左侧斜切背景层 -->
    <div class="split-left">
      <div class="clipped-bg">
        <img src="../../assets/images/loginPhoto.png" alt="Background" class="bg-img" />
      </div>
      <!-- 边缘投影装饰，增加折纸层次感 -->
      <div class="edge-shadow"></div>
    </div>

    <!-- 右侧内容层 -->
    <div class="split-right">
      <div class="login-wrapper">
        <div class="brand-header">
          <div class="logo-box">
            <img src="../../assets/images/logo.png" alt="Logo" class="logo" />
          </div>
          <h1 class="system-title">线上产品购物商城数据平台</h1>
        </div>

        <div class="login-form" @keyup.enter="handleLogin">
          <div class="form-item">
            <div class="input-group">
              <span class="input-icon user-icon"></span>
              <input type="text" placeholder="请输入用户名" v-model="pageData.loginForm.username" class="custom-input" />
            </div>
          </div>

          <div class="form-item">
            <div class="input-group">
              <span class="input-icon lock-icon"></span>
              <input
                type="password"
                placeholder="请输入密码"
                v-model="pageData.loginForm.password"
                class="custom-input"
              />
            </div>
          </div>

          <div class="btn-group">
            <button class="submit-btn" @click="handleLogin">
              立即登录
              <span class="arrow">→</span>
            </button>
          </div>

          <div class="form-footer">
            <span class="hint">没有账号？</span>
            <a @click="handleCreateUser" class="link-text">立即注册</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '/@/store';
  import { message } from 'ant-design-vue';
  import { reactive } from 'vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const userStore = useUserStore();

  const pageData = reactive({
    loginForm: {
      username: '',
      password: '',
    },
  });

  const handleLogin = () => {
    if (!pageData.loginForm.username || !pageData.loginForm.password) {
      message.warning('请填写完整的登录信息');
      return;
    }

    userStore
      .login({
        username: pageData.loginForm.username,
        password: pageData.loginForm.password,
      })
      .then((res) => {
        loginSuccess();
      })
      .catch((err) => {
        message.warn(err.msg || '登录失败');
      });
  };

  const handleCreateUser = () => {
    router.push({ name: 'register' });
  };

  const loginSuccess = () => {
    router.push({ name: 'portal' });
    message.success('登录成功！');
  };
</script>

<style scoped lang="less">
  .login-page {
    position: relative;
    width: 100vw;
    height: 100vh;
    display: flex;
    overflow: hidden;
    background-color: #fff;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  // 左侧背景图片区域
  .split-left {
    position: absolute;
    top: 0;
    left: 0;
    width: 65vw;
    height: 100%;
    z-index: 1;
    background-color: #ffd100;

    .clipped-bg {
      width: 100%;
      height: 100%;
      overflow: hidden;

      .bg-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }

  // 右侧表单内容区域（折纸卡面层）
  .split-right {
    position: relative;
    flex: 1;
    height: 100%;
    margin-left: 48vw;
    background-color: #fff;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: 8vw;

    // 1. 核心裁剪：创建斜线边缘
    clip-path: polygon(18% 0, 100% 0, 100% 100%, 0 100%);

    // 2. 深度阴影：使用 drop-shadow 滤镜产生跟随斜线的自然投影
    filter: drop-shadow(-40px 0 50px rgba(0, 0, 0, 0.25));

    // 3. 增加“折纸边缘”厚度感
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: #fff;
      z-index: -1;
      clip-path: polygon(18% 0, 100% 0, 100% 100%, 0 100%);
    }

    // 4. 增加折痕光影：模拟纸张折叠后的物理暗部
    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 18%; // 对应 polygon 的起点
      width: 2px;
      height: 100%;
      background: linear-gradient(to right, rgba(0, 0, 0, 0.05), transparent);
      z-index: 11;
      pointer-events: none;
      transform: translateX(-1px);
    }
  }

  .login-wrapper {
    width: 100%;
    max-width: 400px;
    padding-right: 5vw;
    animation: fadeInRight 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  }

  .brand-header {
    margin-bottom: 50px;
    text-align: center;

    .logo-box {
      margin-bottom: 20px;
      .logo {
        height: 54px;
        width: auto;
      }
    }

    .system-title {
      font-size: 24px;
      color: #333;
      font-weight: 600;
      letter-spacing: 0.5px;
      margin: 0;
    }
  }

  .login-form {
    .form-item {
      margin-bottom: 24px;
    }

    .input-group {
      position: relative;
      display: flex;
      align-items: center;

      .input-icon {
        position: absolute;
        left: 15px;
        width: 20px;
        height: 20px;
        opacity: 0.6;
        background-size: contain;
        background-repeat: no-repeat;
        transition: all 0.3s;

        &.user-icon {
          background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ff9d3d' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'%3E%3C/path%3E%3Ccircle cx='12' cy='7' r='4'%3E%3C/circle%3E%3C/svg%3E");
        }

        &.lock-icon {
          background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ff9d3d' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='3' y='11' width='18' height='11' rx='2' ry='2'%3E%3C/rect%3E%3Cpath d='M7 11V7a5 5 0 0 1 10 0v4'%3E%3C/path%3E%3C/svg%3E");
        }
      }

      .custom-input {
        width: 100%;
        height: 52px;
        background-color: #f9f9f9;
        border: 1px solid #eee;
        border-radius: 8px;
        padding: 0 15px 0 50px;
        font-size: 15px;
        color: #333;
        transition: all 0.3s;

        &::placeholder {
          color: #bbb;
        }

        &:focus {
          outline: none;
          background-color: #fff;
          border-color: #ff9d3d;
          box-shadow: 0 0 0 4px rgba(255, 157, 61, 0.1);

          & + .input-icon {
            opacity: 1;
          }
        }
      }
    }
  }

  .btn-group {
    margin-top: 40px;

    .submit-btn {
      width: 100%;
      height: 52px;
      background: linear-gradient(135deg, #ff9d3d 0%, #ffb35a 100%);
      color: #fff;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      box-shadow: 0 6px 20px rgba(255, 157, 61, 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(255, 157, 61, 0.4);
      }

      &:active {
        transform: translateY(0);
      }

      .arrow {
        margin-left: 10px;
        font-size: 18px;
        transition: transform 0.3s;
      }

      &:hover .arrow {
        transform: translateX(4px);
      }
    }
  }

  .form-footer {
    margin-top: 30px;
    text-align: center;
    font-size: 14px;

    .hint {
      color: #999;
    }

    .link-text {
      color: #ff9d3d;
      text-decoration: none;
      font-weight: 600;
      margin-left: 6px;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  @keyframes fadeInRight {
    from {
      opacity: 0;
      transform: translateX(30px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  // 移动端适配
  @media (max-width: 768px) {
    .split-left {
      display: none;
    }
    .split-right {
      justify-content: center;
      padding: 0 30px;
    }
    .login-wrapper {
      max-width: 100%;
    }
  }
</style>
