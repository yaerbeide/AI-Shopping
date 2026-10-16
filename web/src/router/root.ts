// 路由表
const constantRouterMap = [
  // ************* 前台路由 **************
  {
    path: '/',
    redirect: '/index',
  },
  {
    path: '/index',
    name: 'index',
    redirect: '/index/portal',
    component: () => import('/@/views/index/index.vue'),
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import('/@/views/index/login.vue'),
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('/@/views/index/register.vue'),
      },
      {
        path: 'portal',
        name: 'portal',
        component: () => import('/@/views/index/portal.vue'),
      },
      {
        path: 'detail',
        name: 'detail',
        component: () => import('/@/views/index/detail.vue'),
      },
      {
        path: 'confirm',
        name: 'confirm',
        component: () => import('/@/views/index/confirm.vue'),
      },
      {
        path: 'pay',
        name: 'pay',
        component: () => import('/@/views/index/pay.vue'),
      },
      {
        path: 'search',
        name: 'search',
        component: () => import('/@/views/index/search.vue'),
      },
      {
        path: 'cart',
        name: 'cartView',
        component: () => import('/@/views/index/cart.vue'),
      },
      {
        path: 'couponCenter',
        name: 'couponCenter',
        component: () => import('/@/views/index/coupon-center.vue'),
      },
      {
        path: 'usercenter',
        name: 'usercenter',
        redirect: '/index/usercenter/addressView',
        component: () => import('/@/views/index/usercenter.vue'),
        children: [
          {
            path: 'addressView',
            name: 'addressView',
            component: () => import('/@/views/index/user/address-view.vue'),
          },
          {
            path: 'wishThingView',
            name: 'wishThingView',
            component: () => import('/@/views/index/user/wish-thing-view.vue'),
            meta: {
              title: '我的购物车',
            },
          },
          {
            path: 'collectThingView',
            name: 'collectThingView',
            component: () => import('/@/views/index/user/collect-thing-view.vue'),
          },
          {
            path: 'userCouponsView',
            name: 'userCouponsView',
            component: () => import('/@/views/index/user/UserCoupons.vue'),
          },
          {
            path: 'orderView',
            name: 'orderView',
            component: () => import('/@/views/index/user/order-view.vue'),
          },
          {
            path: 'userInfoEditView',
            name: 'userInfoEditView',
            component: () => import('/@/views/index/user/userinfo-edit-view.vue'),
          },
          {
            path: 'followView',
            name: 'followView',
            component: () => import('/@/views/index/user/follow-view.vue'),
          },
          {
            path: 'fansView',
            name: 'fansView',
            component: () => import('/@/views/index/user/fans-view.vue'),
          },
          {
            path: 'scoreView',
            name: 'scoreView',
            component: () => import('/@/views/index/user/score-view.vue'),
          },
          {
            path: 'commentView',
            name: 'commentView',
            component: () => import('/@/views/index/user/comment-view.vue'),
          },
          {
            path: 'securityView',
            name: 'securityView',
            component: () => import('/@/views/index/user/security-view.vue'),
          },
          {
            path: 'pushView',
            name: 'pushView',
            component: () => import('/@/views/index/user/push-view.vue'),
          },
          {
            path: 'messageView',
            name: 'messageView',
            component: () => import('/@/views/index/user/message-view.vue'),
          },
        ],
      },
    ],
  },
  {
    path: '/adminLogin',
    name: 'adminLogin',
    component: () => import('/@/views/admin/admin-login.vue'),
  },
  {
    path: '/admin',
    name: 'admin',
    redirect: '/admin/overview',
    component: () => import('/@/views/admin/main.vue'),
    children: [
      { path: 'overview', name: 'overview', component: () => import('/@/views/admin/overview.vue') },
      { path: 'order', name: 'order', component: () => import('/@/views/admin/order.vue') },
      { path: 'thing', name: 'thing', component: () => import('/@/views/admin/thing.vue') },
      { path: 'comment', name: 'comment', component: () => import('/@/views/admin/comment.vue') },
      { path: 'user', name: 'user', component: () => import('/@/views/admin/user.vue') },
      { path: 'classification', name: 'classification', component: () => import('/@/views/admin/classification.vue') },
      { path: 'tag', name: 'tag', component: () => import('/@/views/admin/tag.vue') },
      { path: 'coupon', name: 'coupon', component: () => import('/@/views/admin/coupon.vue') },
      { path: 'ad', name: 'ad', component: () => import('/@/views/admin/ad.vue') },
      { path: 'notice', name: 'notice', component: () => import('/@/views/admin/notice.vue') },
      { path: 'banner', name: 'banner', component: () => import('/@/views/admin/banner.vue') },
      { path: 'loginLog', name: 'loginLog', component: () => import('/@/views/admin/login-log.vue') },
      { path: 'opLog', name: 'opLog', component: () => import('/@/views/admin/op-log.vue') },
      { path: 'errorLog', name: 'errorLog', component: () => import('/@/views/admin/error-log.vue') },
      { path: 'sysInfo', name: 'sysInfo', component: () => import('/@/views/admin/sys-info.vue') },
      { path: 'profile', name: 'profile', component: () => import('/@/views/admin/profile.vue') },
    ],
  },
];

export default constantRouterMap;
