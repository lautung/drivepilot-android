import { describe, expect, it } from 'vitest'

import { formatBytes, formatDate } from './format'

describe('display formatting', () => {
  it('formats file sizes without losing units', () => {
    expect(formatBytes(1024)).toBe('1.0 KiB')
    expect(formatBytes(10 * 1024 * 1024)).toBe('10.0 MiB')
  })

  it('returns a safe placeholder for invalid dates', () => {
    expect(formatDate('not-a-date')).toBe('—')
  })
})
