import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { apiClient, setAccessToken, setRefreshHandler } from '../../shared/api/client'
import { problemFromResponse } from '../../shared/api/problem'
import type { AdminSessionResponse, UserRole } from '../../shared/api/types'

export interface SessionUser {
  id: string
  username: string
  role: UserRole
}

function requireSession(response: AdminSessionResponse) {
  const user = response.user
  if (
    !response.accessToken ||
    !response.accessExpiresAt ||
    !user?.id ||
    !user.username ||
    !user.role
  ) {
    throw new Error('管理端会话响应不完整')
  }
  return {
    token: response.accessToken,
    expiresAt: response.accessExpiresAt,
    user: { id: user.id, username: user.username, role: user.role } satisfies SessionUser,
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<SessionUser | null>(null)
  const expiresAt = ref<string | null>(null)
  const bootstrapped = ref(false)
  const refreshing = ref(false)

  const isAuthenticated = computed(() => user.value !== null)
  const canWrite = computed(() => user.value?.role === 'ADMIN')
  const isViewer = computed(() => user.value?.role === 'ADMIN_VIEWER')

  function applySession(response: AdminSessionResponse) {
    const session = requireSession(response)
    user.value = session.user
    expiresAt.value = session.expiresAt
    setAccessToken(session.token)
  }

  function clearSession() {
    user.value = null
    expiresAt.value = null
    setAccessToken(null)
  }

  async function login(username: string, password: string) {
    const { data, error, response } = await apiClient.POST('/api/v1/auth/admin/login', {
      body: { username, password },
    })
    if (!response.ok || !data) throw await problemFromResponse(response, error)
    applySession(data)
  }

  async function refresh(): Promise<boolean> {
    if (refreshing.value) return false
    refreshing.value = true
    try {
      const { data, response } = await apiClient.POST('/api/v1/auth/admin/refresh')
      if (!response.ok || !data) {
        clearSession()
        return false
      }
      applySession(data)
      return true
    } catch {
      clearSession()
      return false
    } finally {
      refreshing.value = false
    }
  }

  async function bootstrap() {
    if (bootstrapped.value) return
    await refresh()
    bootstrapped.value = true
  }

  async function logout() {
    try {
      await apiClient.POST('/api/v1/auth/admin/logout')
    } finally {
      clearSession()
      bootstrapped.value = true
    }
  }

  setRefreshHandler(refresh)

  return {
    user,
    expiresAt,
    bootstrapped,
    refreshing,
    isAuthenticated,
    canWrite,
    isViewer,
    login,
    refresh,
    bootstrap,
    logout,
    clearSession,
  }
})
