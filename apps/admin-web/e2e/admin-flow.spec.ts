import { expect, test, type Page } from '@playwright/test'
import path from 'node:path'

const adminUsername = process.env.E2E_ADMIN_USERNAME ?? 'admin'
const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? 'admin-change-me'
const viewerUsername = process.env.E2E_VIEWER_USERNAME ?? 'demo_viewer'
const viewerPassword = process.env.E2E_VIEWER_PASSWORD ?? 'viewer-change-me'

async function login(page: Page, username: string, password: string) {
  await page.goto('/login')
  await page.getByLabel('用户名').fill(username)
  await page.getByLabel('密码').fill(password)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page.getByRole('heading', { name: '系统概览' })).toBeVisible()
}

test('ADMIN completes media and content lifecycle', async ({ page }) => {
  await login(page, adminUsername, adminPassword)

  await page.getByRole('link', { name: '媒体资源' }).click()
  const fixture = path.resolve(import.meta.dirname, '../../../docs/screenshots/home.png')
  await page.locator('input[type="file"]').setInputFiles(fixture)
  await expect(page.getByText('媒体上传成功')).toBeVisible()
  const mediaRow = page.getByRole('row').filter({ hasText: 'home.png' }).first()
  await expect(mediaRow).toBeVisible()
  await mediaRow.getByRole('button', { name: '删除' }).click()
  await page.getByRole('button', { name: '删除' }).last().click()
  await expect(page.getByText('媒体已删除')).toBeVisible()

  await page.getByRole('link', { name: '内容管理' }).click()
  const suffix = Date.now().toString()
  const title = `E2E 内容 ${suffix}`
  const updatedTitle = `${title} 已编辑`
  await page.getByRole('button', { name: '新建内容' }).click()
  await page.getByLabel('标题').fill(title)
  await page.getByLabel('摘要').fill('Playwright 创建的端到端测试内容')
  await page.getByLabel('正文').fill('普通多行文本正文。')
  await page.getByRole('button', { name: '创建内容' }).click()
  await expect(page.getByText('内容已创建')).toBeVisible()

  let row = page.getByRole('row').filter({ hasText: title })
  await expect(row).toBeVisible()
  await row.getByRole('button', { name: '编辑' }).click()
  await page.getByLabel('标题').fill(updatedTitle)
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page.getByText('内容已更新')).toBeVisible()

  row = page.getByRole('row').filter({ hasText: updatedTitle })
  await row.getByRole('button', { name: '发布' }).click()
  await page.getByRole('button', { name: '发布' }).last().click()
  await expect(row.getByText('已发布')).toBeVisible()
  await row.getByRole('button', { name: '下架' }).click()
  await page.getByRole('button', { name: '下架' }).last().click()
  await expect(row.getByText('已下架')).toBeVisible()
  await row.getByRole('button', { name: '删除' }).click()
  await page.getByRole('button', { name: '删除' }).last().click()
  await expect(page.getByText('删除成功')).toBeVisible()
})

test('ADMIN_VIEWER remains read-only in UI and API', async ({ page, request }) => {
  await login(page, viewerUsername, viewerPassword)
  await expect(page.getByRole('status')).toContainText('当前为只读演示模式')
  await page.getByRole('link', { name: '内容管理' }).click()
  await expect(page.getByRole('button', { name: '新建内容' })).toHaveCount(0)
  await expect(page.getByRole('columnheader', { name: '操作' })).toHaveCount(0)

  const loginResponse = await request.post('http://127.0.0.1:8080/api/v1/auth/admin/login', {
    data: { username: viewerUsername, password: viewerPassword },
  })
  expect(loginResponse.ok()).toBeTruthy()
  const session = (await loginResponse.json()) as { accessToken: string }
  const writeResponse = await request.post(
    'http://127.0.0.1:8080/api/v1/admin/discovery/contents',
    {
      headers: { Authorization: `Bearer ${session.accessToken}` },
      data: {
        category: 'RECOMMENDED',
        title: 'Viewer must not create content',
        summary: 'Permission boundary probe',
        body: 'Permission boundary probe',
      },
    },
  )
  expect(writeResponse.status()).toBe(403)
})
