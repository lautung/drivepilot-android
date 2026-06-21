import { listContents } from '../contents/api'
import { listMedia } from '../media/api'

export interface DashboardSummary {
  health: 'UP' | 'DOWN' | 'UNKNOWN'
  contentTotal: number
  mediaTotal: number
  recentContents: Awaited<ReturnType<typeof listContents>>['items']
}

async function readHealth(): Promise<DashboardSummary['health']> {
  try {
    const response = await fetch('/actuator/health', { headers: { Accept: 'application/json' } })
    if (!response.ok) return 'DOWN'
    const value: unknown = await response.json()
    if (typeof value === 'object' && value !== null && 'status' in value) {
      const status = (value as { status?: unknown }).status
      return status === 'UP' ? 'UP' : status === 'DOWN' ? 'DOWN' : 'UNKNOWN'
    }
  } catch {
    return 'UNKNOWN'
  }
  return 'UNKNOWN'
}

export async function loadDashboard(): Promise<DashboardSummary> {
  const [health, contents, media] = await Promise.all([
    readHealth(),
    listContents({ page: 0, size: 3 }),
    listMedia(0, 1),
  ])
  return {
    health,
    contentTotal: contents.totalElements ?? 0,
    mediaTotal: media.totalElements ?? 0,
    recentContents: contents.items ?? [],
  }
}
