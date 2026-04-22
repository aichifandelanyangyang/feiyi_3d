import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          meta: { title: '控制台', icon: 'DataBoard' },
          component: () => import('@/views/dashboard/index.vue')
        },
        {
          path: 'exhibit',
          name: 'Exhibit',
          meta: { title: '展品管理', icon: 'Picture' },
          component: () => import('@/views/exhibit/index.vue')
        },
        {
          path: 'heritage',
          name: 'Heritage',
          meta: { title: '非遗项目', icon: 'Collection' },
          component: () => import('@/views/heritage/index.vue')
        },
        {
          path: 'heritage-category',
          name: 'HeritageCategory',
          meta: { title: '非遗种类', icon: 'Menu' },
          component: () => import('@/views/heritage-category/index.vue')
        },
        {
          path: 'knowledge',
          name: 'Knowledge',
          meta: { title: '知识库', icon: 'Reading' },
          component: () => import('@/views/knowledge/index.vue')
        },
        {
          path: 'community',
          name: 'Community',
          meta: { title: '社区管理', icon: 'ChatDotRound' },
          component: () => import('@/views/community/index.vue')
        },
        {
          path: 'user',
          name: 'User',
          meta: { title: '用户管理', icon: 'User', roles: [1] },
          component: () => import('@/views/user/index.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          meta: { title: '个人中心', icon: 'UserFilled' },
          component: () => import('@/views/profile/index.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
