<script setup lang="ts">
import {
  Collection,
  DataAnalysis,
  Expand,
  Fold,
  Picture,
  SwitchButton,
  View,
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '../features/auth/store'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

const navItems = [
  { route: 'dashboard', label: '概览', icon: DataAnalysis },
  { route: 'contents', label: '内容管理', icon: Collection },
  { route: 'media', label: '媒体资源', icon: Picture },
] as const

const activeName = computed(() => String(route.name ?? 'dashboard'))

async function logout() {
  await auth.logout()
  await router.replace({ name: 'login' })
}
</script>

<template>
  <div class="app-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="sidebar" aria-label="主导航">
      <RouterLink class="brand" :to="{ name: 'dashboard' }" aria-label="DrivePilot 管理中心">
        <span class="brand-mark">D</span>
        <span class="brand-text">DrivePilot</span>
      </RouterLink>

      <nav class="nav-list">
        <RouterLink
          v-for="item in navItems"
          :key="item.route"
          class="nav-item"
          :class="{ active: activeName === item.route }"
          :to="{ name: item.route }"
          :aria-label="item.label"
        >
          <ElIcon :size="20"><component :is="item.icon" /></ElIcon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <button class="collapse-button" type="button" @click="collapsed = !collapsed">
        <ElIcon :size="18"><component :is="collapsed ? Expand : Fold" /></ElIcon>
        <span>{{ collapsed ? '展开侧栏' : '收起侧栏' }}</span>
      </button>
    </aside>

    <div class="shell-main">
      <header class="topbar">
        <div class="topbar-account">
          <span v-if="auth.isViewer" class="viewer-label"
            ><ElIcon><View /></ElIcon>只读演示</span
          >
          <span class="username">{{ auth.user?.username }}</span>
          <span class="role-text">{{ auth.user?.role }}</span>
        </div>
        <button class="logout-button" type="button" @click="logout">
          <ElIcon><SwitchButton /></ElIcon>
          退出登录
        </button>
      </header>

      <div v-if="auth.isViewer" class="viewer-banner" role="status">
        当前为只读演示模式，所有写操作已禁用；后端仍会执行最终权限校验。
      </div>
      <main class="page-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
