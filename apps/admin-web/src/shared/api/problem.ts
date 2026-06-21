import type { ApiProblemShape, FieldError } from './types'

export class ApiProblem extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: readonly FieldError[]
  readonly retryAfter?: string

  constructor(problem: ApiProblemShape, retryAfter?: string) {
    super(problem.detail || problem.title || '请求失败，请稍后重试')
    this.name = 'ApiProblem'
    this.status = problem.status ?? 0
    this.code = problem.code ?? 'UNKNOWN_ERROR'
    this.fieldErrors = isFieldErrorArray(problem.fieldErrors) ? problem.fieldErrors : []
    this.retryAfter = retryAfter
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isFieldErrorArray(value: unknown): value is readonly FieldError[] {
  return (
    Array.isArray(value) &&
    value.every(
      (item) =>
        isRecord(item) && typeof item.field === 'string' && typeof item.message === 'string',
    )
  )
}

export function normalizeProblem(value: unknown, status = 0, retryAfter?: string): ApiProblem {
  if (value instanceof ApiProblem) return value
  if (!isRecord(value)) return new ApiProblem({ status }, retryAfter)

  return new ApiProblem(
    {
      status: typeof value.status === 'number' ? value.status : status,
      code: typeof value.code === 'string' ? value.code : undefined,
      detail: typeof value.detail === 'string' ? value.detail : undefined,
      title: typeof value.title === 'string' ? value.title : undefined,
      fieldErrors: isFieldErrorArray(value.fieldErrors) ? value.fieldErrors : [],
    },
    retryAfter,
  )
}

export async function problemFromResponse(response: Response, fallback?: unknown) {
  let body = fallback
  if (body === undefined) {
    try {
      body = await response.clone().json()
    } catch {
      body = undefined
    }
  }
  return normalizeProblem(body, response.status, response.headers.get('Retry-After') ?? undefined)
}

export function problemMessage(error: unknown): string {
  const problem = normalizeProblem(error)
  const messages: Record<string, string> = {
    INVALID_CREDENTIALS: '用户名或密码不正确',
    ADMIN_ACCESS_REQUIRED: '该账号没有管理端访问权限',
    INVALID_REFRESH_TOKEN: '登录状态已失效，请重新登录',
    VALIDATION_FAILED: '请检查表单中的输入内容',
    MEDIA_IN_USE: '该媒体正在被内容引用，请先解除关联',
    MEDIA_TOO_LARGE: '文件不能超过 10 MiB',
    UNSUPPORTED_MEDIA: '仅支持 JPEG、PNG 或 WebP 图片',
    MEDIA_TYPE_MISMATCH: '文件内容与声明格式不一致',
    RATE_LIMITED: '请求过于频繁，请稍后再试',
    MEDIA_STORAGE_UNAVAILABLE: '媒体存储暂时不可用，请稍后重试',
    CONTENT_NOT_FOUND: '内容已不存在，列表将自动刷新',
  }
  return messages[problem.code] ?? problem.message
}
