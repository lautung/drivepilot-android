import { describe, expect, it, vi } from 'vitest'

import { requestWithSession, setAccessToken, setRefreshHandler } from './client'

describe('authenticated request boundary', () => {
  it('shares one refresh across concurrent 401 responses and retries once', async () => {
    let token = 'expired'
    let refreshCalls = 0
    setAccessToken(token)
    setRefreshHandler(async () => {
      refreshCalls += 1
      await new Promise((resolve) => setTimeout(resolve, 10))
      token = 'fresh'
      setAccessToken(token)
      return true
    })

    const request = vi.fn(async (headers?: HeadersInit) => {
      const authorization = new Headers(headers).get('Authorization')
      if (authorization === 'Bearer expired') {
        return { response: new Response(null, { status: 401 }) }
      }
      return { data: 'ok', response: new Response(null, { status: 200 }) }
    })

    const results = await Promise.all([
      requestWithSession<string>(request),
      requestWithSession<string>(request),
    ])

    expect(results).toEqual(['ok', 'ok'])
    expect(refreshCalls).toBe(1)
    expect(request).toHaveBeenCalledTimes(4)
  })
})
