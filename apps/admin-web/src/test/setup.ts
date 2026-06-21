import '@testing-library/jest-dom/vitest'

import { cleanup } from '@testing-library/vue'
import { afterAll, afterEach } from 'vitest'

import { server } from './server'

// openapi-fetch captures global fetch when the client module is imported, so
// install the MSW interceptor before test modules are evaluated.
server.listen({ onUnhandledRequest: 'error' })
afterEach(() => {
  cleanup()
  server.resetHandlers()
})
afterAll(() => server.close())
