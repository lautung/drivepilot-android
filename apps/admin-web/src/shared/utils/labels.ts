import type { ContentCategory, ContentStatus } from '../api/types'

export const categoryLabels: Record<ContentCategory, string> = {
  RECOMMENDED: '推荐',
  LOCAL: '本地',
  ACTIVITY: '活动',
  STORE: '门店',
}

export const statusLabels: Record<ContentStatus, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  UNPUBLISHED: '已下架',
}

export function statusTone(status?: ContentStatus) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'DRAFT') return 'warning'
  return 'muted'
}

export function categoryLabel(value: unknown) {
  return typeof value === 'string' && value in categoryLabels
    ? categoryLabels[value as ContentCategory]
    : '—'
}

export function statusLabel(value: unknown) {
  return typeof value === 'string' && value in statusLabels
    ? statusLabels[value as ContentStatus]
    : '—'
}
