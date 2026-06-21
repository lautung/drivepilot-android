import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { createPinia, setActivePinia } from 'pinia'
import { http, HttpResponse } from 'msw'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import { server } from '../../test/server'
import { useAuthStore } from '../auth/store'
import ContentsView from './ContentsView.vue'

const pageResponse = {
  items: [
    {
      id: 'f87a7779-33f7-4eef-8ffd-f887a3e07111',
      category: 'RECOMMENDED',
      title: '组件测试内容',
      summary: '摘要',
      body: '正文',
      status: 'DRAFT',
      createdAt: '2026-06-21T00:00:00Z',
      updatedAt: '2026-06-21T00:00:00Z',
    },
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
}

async function renderContents(role: 'ADMIN' | 'ADMIN_VIEWER') {
  const pinia = createPinia()
  setActivePinia(pinia)
  server.use(
    http.post('*/api/v1/auth/admin/login', () =>
      HttpResponse.json({
        accessToken: 'component-test-token',
        accessExpiresAt: '2030-01-01T00:00:00Z',
        user: {
          id: '9825520b-bfeb-40e5-ab9d-31be5451a6cd',
          username: role === 'ADMIN' ? 'admin' : 'demo_viewer',
          role,
        },
      }),
    ),
    http.get('*/api/v1/admin/discovery/contents', () => HttpResponse.json(pageResponse)),
    http.get('*/api/v1/admin/media', () =>
      HttpResponse.json({ items: [], page: 0, size: 100, totalElements: 0, totalPages: 0 }),
    ),
  )
  const auth = useAuthStore()
  await auth.login(role === 'ADMIN' ? 'admin' : 'demo_viewer', 'password-123')

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/contents', component: ContentsView }],
  })
  await router.push('/contents')
  await router.isReady()
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })

  render(ContentsView, {
    global: { plugins: [pinia, router, [VueQueryPlugin, { queryClient }]] },
  })
}

describe('content management view', () => {
  it('opens the content form and maps server field errors', async () => {
    server.use(
      http.post('*/api/v1/admin/discovery/contents', () =>
        HttpResponse.json(
          {
            status: 400,
            code: 'VALIDATION_FAILED',
            detail: 'Request validation failed',
            fieldErrors: [{ field: 'title', message: '服务端标题错误' }],
          },
          { status: 400 },
        ),
      ),
    )
    await renderContents('ADMIN')

    await fireEvent.click(await screen.findByRole('button', { name: '新建内容' }))
    await fireEvent.update(screen.getByLabelText('标题'), '有效标题')
    await fireEvent.update(screen.getByLabelText('摘要'), '有效摘要')
    await fireEvent.update(screen.getByLabelText('正文'), '有效正文')
    await fireEvent.click(screen.getByRole('button', { name: '创建内容' }))

    expect(await screen.findByText('服务端标题错误')).toBeInTheDocument()
  })

  it('removes all write entry points for the viewer role', async () => {
    await renderContents('ADMIN_VIEWER')

    expect(await screen.findByText('组件测试内容')).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByRole('button', { name: '新建内容' })).toBeNull())
    expect(screen.queryByRole('columnheader', { name: '操作' })).toBeNull()
  })
})
