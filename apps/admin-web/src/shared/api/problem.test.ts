import { describe, expect, it } from 'vitest'

import { ApiProblem, normalizeProblem, problemMessage } from './problem'

describe('Problem Details boundary', () => {
  it('keeps stable codes and field errors', () => {
    const problem = normalizeProblem({
      status: 400,
      code: 'VALIDATION_FAILED',
      detail: 'Request validation failed',
      fieldErrors: [{ field: 'title', message: 'must not be blank' }],
    })

    expect(problem).toBeInstanceOf(ApiProblem)
    expect(problem.status).toBe(400)
    expect(problem.code).toBe('VALIDATION_FAILED')
    expect(problem.fieldErrors).toEqual([{ field: 'title', message: 'must not be blank' }])
    expect(problemMessage(problem)).toBe('请检查表单中的输入内容')
  })

  it('does not expose non-json response bodies', () => {
    const problem = normalizeProblem('<html>internal proxy error</html>', 502)
    expect(problem.status).toBe(502)
    expect(problem.message).toBe('请求失败，请稍后重试')
  })
})
