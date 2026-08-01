import { describe, expect, it, vi } from 'vitest'

import {
  createJavaQuizClient,
  JavaQuizHttpError,
  JavaQuizInvalidResponseError,
  JavaQuizNetworkError,
  JavaQuizRequestCancelledError,
  JavaQuizTimeoutError,
} from '../src/index.js'

describe('createJavaQuizClient', () => {
  it('lists categories with the access token', async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(
      Response.json({
        items: [{ slug: 'OOP', name: 'Orientacao a objetos' }],
      }),
    )
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080/',
      tokenProvider: () => 'access-token',
      fetch: fetchMock,
    })

    await expect(client.categories.list()).resolves.toEqual({
      items: [{ slug: 'OOP', name: 'Orientacao a objetos' }],
    })
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/categories',
      expect.objectContaining({
        headers: expect.objectContaining({}),
        signal: expect.any(AbortSignal),
      }),
    )
    const request = fetchMock.mock.calls[0]?.[1]
    expect(new Headers(request?.headers).get('Authorization')).toBe(
      'Bearer access-token',
    )
  })

  it('returns a typed HTTP error with Problem Details', async () => {
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi
        .fn<typeof fetch>()
        .mockResolvedValue(
          Response.json(
            { title: 'Forbidden', detail: 'Access denied.' },
            { status: 403 },
          ),
        ),
    })

    const request = client.categories.list()
    await expect(request).rejects.toBeInstanceOf(JavaQuizHttpError)
    await expect(request).rejects.toMatchObject({
      status: 403,
      problem: { title: 'Forbidden', detail: 'Access denied.' },
    })
  })

  it('rejects invalid successful responses', async () => {
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi
        .fn<typeof fetch>()
        .mockResolvedValue(
          Response.json({ items: [{ slug: 10, name: 'Invalid' }] }),
        ),
    })

    await expect(client.categories.list()).rejects.toBeInstanceOf(
      JavaQuizInvalidResponseError,
    )
  })

  it('distinguishes network errors, cancellation and timeout', async () => {
    const networkClient = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi.fn<typeof fetch>().mockRejectedValue(new TypeError('offline')),
    })
    await expect(networkClient.categories.list()).rejects.toBeInstanceOf(
      JavaQuizNetworkError,
    )

    const pendingFetch = vi.fn<typeof fetch>()
    pendingFetch.mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () =>
            reject(new DOMException('Aborted', 'AbortError')),
          )
        }),
    )
    const cancelledClient = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: pendingFetch,
    })
    const controller = new AbortController()
    const cancelledRequest = cancelledClient.categories.list({
      signal: controller.signal,
    })
    controller.abort()
    await expect(cancelledRequest).rejects.toBeInstanceOf(
      JavaQuizRequestCancelledError,
    )

    const timeoutClient = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      timeoutMs: 1,
      fetch: pendingFetch,
    })
    await expect(timeoutClient.categories.list()).rejects.toBeInstanceOf(
      JavaQuizTimeoutError,
    )
  })
})
