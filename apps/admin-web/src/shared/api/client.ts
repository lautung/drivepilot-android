import createClient from 'openapi-fetch'

import type { paths } from './schema'
import { problemFromResponse } from './problem'

export const apiClient = createClient<paths>({
  baseUrl: window.location.origin,
  credentials: 'include',
})

let accessToken: string | null = null
let refreshHandler: (() => Promise<boolean>) | null = null
let refreshPromise: Promise<boolean> | null = null

export function setAccessToken(value: string | null) {
  accessToken = value
}

export function getAccessToken() {
  return accessToken
}

export function setRefreshHandler(handler: () => Promise<boolean>) {
  refreshHandler = handler
}

export function authHeaders(token = accessToken): HeadersInit | undefined {
  return token ? { Authorization: `Bearer ${token}` } : undefined
}

interface ApiResult<T> {
  data?: T
  error?: unknown
  response: Response
}

interface RequestOptions {
  skipRefresh?: boolean
  allowEmpty?: boolean
}

async function refreshOnce(): Promise<boolean> {
  if (!refreshHandler) return false
  if (!refreshPromise) {
    refreshPromise = refreshHandler().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

export async function requestWithSession<T>(
  request: (headers: HeadersInit | undefined) => Promise<ApiResult<T>>,
  options: RequestOptions = {},
): Promise<T> {
  let result = await request(authHeaders())
  if (result.response.status === 401 && !options.skipRefresh && (await refreshOnce())) {
    result = await request(authHeaders())
  }

  if (!result.response.ok) {
    throw await problemFromResponse(result.response, result.error)
  }
  if (result.data === undefined && !options.allowEmpty) {
    throw await problemFromResponse(result.response)
  }
  return result.data as T
}
