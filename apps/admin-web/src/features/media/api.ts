import { apiClient, getAccessToken, requestWithSession } from '../../shared/api/client'
import { normalizeProblem } from '../../shared/api/problem'
import type { Media, MediaPage } from '../../shared/api/types'

export const MAX_MEDIA_BYTES = 10 * 1024 * 1024
export const ACCEPTED_MEDIA_TYPES = ['image/jpeg', 'image/png', 'image/webp'] as const

export function listMedia(page = 0, size = 20) {
  return requestWithSession<MediaPage>((headers) =>
    apiClient.GET('/api/v1/admin/media', {
      headers,
      params: { query: { page, size } },
    }),
  )
}

export function deleteMedia(id: string) {
  return requestWithSession<void>(
    (headers) =>
      apiClient.DELETE('/api/v1/admin/media/{id}', { headers, params: { path: { id } } }),
    { allowEmpty: true },
  )
}

interface UploadResult {
  data?: Media
  error?: unknown
  response: Response
}

function uploadAttempt(file: File, onProgress: (percent: number) => void): Promise<UploadResult> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', '/api/v1/admin/media')
    xhr.withCredentials = true
    const token = getAccessToken()
    if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`)
    xhr.upload.addEventListener('progress', (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100))
    })
    xhr.addEventListener('error', () => reject(normalizeProblem({ detail: '网络连接失败' })))
    xhr.addEventListener('load', () => {
      let body: unknown
      try {
        body = xhr.responseText ? JSON.parse(xhr.responseText) : undefined
      } catch {
        body = undefined
      }
      const headers = new Headers()
      const retryAfter = xhr.getResponseHeader('Retry-After')
      if (retryAfter) headers.set('Retry-After', retryAfter)
      const response = new Response(null, { status: xhr.status, headers })
      resolve({
        data: xhr.status >= 200 && xhr.status < 300 ? (body as Media) : undefined,
        error: xhr.status >= 400 ? body : undefined,
        response,
      })
    })
    const formData = new FormData()
    formData.append('file', file)
    xhr.send(formData)
  })
}

export function uploadMedia(file: File, onProgress: (percent: number) => void) {
  if (!ACCEPTED_MEDIA_TYPES.includes(file.type as (typeof ACCEPTED_MEDIA_TYPES)[number])) {
    throw normalizeProblem({ status: 415, code: 'UNSUPPORTED_MEDIA' })
  }
  if (file.size > MAX_MEDIA_BYTES) {
    throw normalizeProblem({ status: 413, code: 'MEDIA_TOO_LARGE' })
  }
  return requestWithSession<Media>(() => uploadAttempt(file, onProgress))
}
