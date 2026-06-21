import { createPinia, setActivePinia } from 'pinia'
import { http, HttpResponse } from 'msw'
import { beforeEach, describe, expect, it } from 'vitest'

import { server } from '../../test/server'
import { useAuthStore } from './store'

const adminSession = {
  accessToken: 'access-token-not-persisted',
  accessExpiresAt: '2030-01-01T00:00:00Z',
  user: { id: '87f9b6fa-e38a-4c2f-87bd-a995cda7f842', username: 'admin', role: 'ADMIN' },
} as const

describe('admin auth store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('keeps the access token in memory only after login', async () => {
    server.use(http.post('*/api/v1/auth/admin/login', () => HttpResponse.json(adminSession)))
    const store = useAuthStore()

    await store.login('admin', 'admin-change-me')

    expect(store.user?.username).toBe('admin')
    expect(store.canWrite).toBe(true)
    expect(JSON.stringify(store.$state)).not.toContain('access-token-not-persisted')
  })

  it('derives viewer write capability from the server role', async () => {
    server.use(
      http.post('*/api/v1/auth/admin/login', () =>
        HttpResponse.json({
          ...adminSession,
          user: { ...adminSession.user, username: 'demo_viewer', role: 'ADMIN_VIEWER' },
        }),
      ),
    )
    const store = useAuthStore()

    await store.login('demo_viewer', 'viewer-change-me')

    expect(store.isViewer).toBe(true)
    expect(store.canWrite).toBe(false)
  })
})
