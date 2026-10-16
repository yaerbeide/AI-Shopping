import axios from 'axios';
import { message } from 'ant-design-vue';
import { useUserStore } from '../store';

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api', // API的base_url
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
});

// request拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token;
    }
    return config;
  },
  (error) => {
    console.log(error);
    Promise.reject(error);
  },
);

// response拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 如果返回的状态码不是200，说明接口请求有误
    if (res.code !== 200) {
      message.error(res.message || '请求失败');

      // 401: 未登录或token过期
      if (res.code === 401) {
        const userStore = useUserStore();
        userStore.logout();
      }

      return Promise.reject(res);
    } else {
      return res;
    }
  },
  (error) => {
    console.log('err' + error);
    message.error(error.message || '请求失败');
    return Promise.reject(error);
  },
);

export default service;
