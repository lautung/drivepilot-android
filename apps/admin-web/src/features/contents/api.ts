import { apiClient, requestWithSession } from '../../shared/api/client'
import type { Content, ContentFilters, ContentPage, ContentRequest } from '../../shared/api/types'

export function listContents(filters: ContentFilters = {}) {
  return requestWithSession<ContentPage>((headers) =>
    apiClient.GET('/api/v1/admin/discovery/contents', {
      headers,
      params: { query: filters },
    }),
  )
}

export function createContent(body: ContentRequest) {
  return requestWithSession<Content>((headers) =>
    apiClient.POST('/api/v1/admin/discovery/contents', { headers, body }),
  )
}

export function updateContent(id: string, body: ContentRequest) {
  return requestWithSession<Content>((headers) =>
    apiClient.PUT('/api/v1/admin/discovery/contents/{id}', {
      headers,
      params: { path: { id } },
      body,
    }),
  )
}

export function publishContent(id: string) {
  return requestWithSession<Content>((headers) =>
    apiClient.POST('/api/v1/admin/discovery/contents/{id}/publish', {
      headers,
      params: { path: { id } },
    }),
  )
}

export function unpublishContent(id: string) {
  return requestWithSession<Content>((headers) =>
    apiClient.POST('/api/v1/admin/discovery/contents/{id}/unpublish', {
      headers,
      params: { path: { id } },
    }),
  )
}

export function deleteContent(id: string) {
  return requestWithSession<void>(
    (headers) =>
      apiClient.DELETE('/api/v1/admin/discovery/contents/{id}', {
        headers,
        params: { path: { id } },
      }),
    { allowEmpty: true },
  )
}
