import { createRouter, createWebHistory } from 'vue-router'

import AppShell from './AppShell.vue'
import StatusView from './StatusView.vue'
import { useAuthStore } from '../features/auth/store'
import LoginView from '../features/auth/LoginView.vue'
import ContentsView from '../features/contents/ContentsView.vue'
import DashboardView from '../features/dashboard/DashboardView.vue'
import MediaView from '../features/media/MediaView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AppShell,
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
        },
        {
          path: 'contents',
          name: 'contents',
          component: ContentsView,
        },
        {
          path: 'media',
          name: 'media',
          component: MediaView,
        },
      ],
    },
    {
      path: '/forbidden',
      name: 'forbidden',
      component: StatusView,
      props: { code: '403', title: '无权访问', description: '当前账号没有访问此页面的权限。' },
      meta: { public: true },
    },
    {
      path: '/:pathMatch(.*)*',
      component: StatusView,
      props: { code: '404', title: '页面不存在', description: '请检查地址或返回系统概览。' },
      meta: { public: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.bootstrap()

  if (to.meta.public) {
    if (to.name === 'login' && auth.isAuthenticated) return { name: 'dashboard' }
    return true
  }
  if (!auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
  return true
})
