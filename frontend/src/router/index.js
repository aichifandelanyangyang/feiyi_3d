import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      meta: {
        title: '首页'
      },
      component: () => import('@/views/home/home-index.vue')
    },
    {
      path: '/login',
      name: 'Login',
      meta: {
        title: '登录'
      },
      component: () => import('@/views/login/login-index.vue')
    },
    {
      path: '/exhibition-hall',
      name: 'ExhibitionHall',
      meta: {
        title: '虚拟展厅'
      },
      component: () => import('@/views/exhibition-hall/exhibition-hall-index.vue')
    },
    {
      path: '/heritage-list',
      name: 'HeritageList',
      meta: {
        title: '非遗项目'
      },
      component: () => import('@/views/heritage/heritage-list.vue')
    },
    {
      path: '/heritage-detail/:id',
      name: 'HeritageDetail',
      meta: {
        title: '非遗详情'
      },
      component: () => import('@/views/heritage/heritage-detail.vue')
    },
    {
      path: '/exhibit/:name',
      name: 'ExhibitDetail',
      meta: {
        title: '展品详情'
      },
      component: () => import('@/views/exhibit/exhibit-detail.vue')
    },
    {
      path: '/ai-assistant',
      name: 'AiAssistant',
      meta: {
        title: '非遗智识'
      },
      component: () => import('@/views/ai/ai-index.vue')
    },
    {
      path: '/community',
      name: 'Community',
      meta: {
        title: '社区互动'
      },
      component: () => import('@/views/community/community-list.vue')
    },
    {
      path: '/community/:id',
      name: 'CommunityDetail',
      meta: {
        title: '帖子详情'
      },
      component: () => import('@/views/community/community-detail.vue')
    },
    {
      path: '/profile',
      name: 'Profile',
      meta: {
        title: '个人中心'
      },
      component: () => import('@/views/profile/profile-index.vue')
    }
  ]
})

// 路由守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 非遗3D数字化交互平台` : '非遗3D数字化交互平台'
  next()
})

export default router
