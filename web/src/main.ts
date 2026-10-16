import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import piniaStore from './store';

import bootstrap from './core/bootstrap';
import '/@/styles/reset.less';
import '/@/styles/index.less';
import Antd from 'ant-design-vue';

// 导入dayjs及其插件
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

// 设置dayjs默认语言为中文
dayjs.locale('zh-cn');

const app = createApp(App);

app.use(Antd);
app.use(router);
app.use(piniaStore);
app.use(bootstrap);
app.mount('#app');
