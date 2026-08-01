import {
  JavaQuizHttpError,
  JavaQuizInvalidResponseError,
  JavaQuizNetworkError,
  JavaQuizRequestCancelledError,
  JavaQuizSdkError,
  JavaQuizTimeoutError,
} from './errors.js'
import type {
  CategoryList,
  JavaQuizClient,
  JavaQuizClientOptions,
  ListCategoriesOptions,
  ProblemDetail,
} from './types.js'

const DEFAULT_TIMEOUT_MS = 10_000

export function createJavaQuizClient(
  options: JavaQuizClientOptions,
): JavaQuizClient {
  const baseUrl = normalizeBaseUrl(options.baseUrl)
  const timeoutMs = options.timeoutMs ?? DEFAULT_TIMEOUT_MS
  const fetchImplementation = options.fetch ?? globalThis.fetch

  if (!Number.isFinite(timeoutMs) || timeoutMs <= 0) {
    throw new TypeError('timeoutMs must be greater than zero.')
  }
  if (typeof fetchImplementation !== 'function') {
    throw new TypeError('A fetch implementation is required.')
  }

  async function request(path: string, signal?: AbortSignal): Promise<unknown> {
    const controller = new AbortController()
    let timedOut = false
    const cancelRequest = () => controller.abort(signal?.reason)
    signal?.addEventListener('abort', cancelRequest, { once: true })
    if (signal?.aborted) {
      controller.abort(signal.reason)
    }
    const timeout = setTimeout(() => {
      timedOut = true
      controller.abort()
    }, timeoutMs)

    try {
      const token = await options.tokenProvider?.()
      if (timedOut) {
        throw new JavaQuizTimeoutError(timeoutMs)
      }
      if (controller.signal.aborted) {
        throw new JavaQuizRequestCancelledError()
      }
      const headers = new Headers({ Accept: 'application/json' })
      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }
      const response = await fetchImplementation(`${baseUrl}${path}`, {
        headers,
        signal: controller.signal,
      })
      if (!response.ok) {
        throw new JavaQuizHttpError(
          response.status,
          await readProblemDetail(response),
        )
      }
      return await response.json()
    } catch (error) {
      if (error instanceof JavaQuizSdkError) {
        throw error
      }
      if (timedOut) {
        throw new JavaQuizTimeoutError(timeoutMs)
      }
      if (signal?.aborted || controller.signal.aborted) {
        throw new JavaQuizRequestCancelledError()
      }
      throw new JavaQuizNetworkError({ cause: error })
    } finally {
      clearTimeout(timeout)
      signal?.removeEventListener('abort', cancelRequest)
    }
  }

  return {
    categories: {
      async list(listOptions?: ListCategoriesOptions): Promise<CategoryList> {
        const body = await request('/api/v1/categories', listOptions?.signal)
        if (!isCategoryList(body)) {
          throw new JavaQuizInvalidResponseError()
        }
        return body
      },
    },
  }
}

function normalizeBaseUrl(baseUrl: string): string {
  const normalized = baseUrl.trim().replace(/\/+$/, '')
  if (!normalized) {
    throw new TypeError('baseUrl is required.')
  }
  return normalized
}

async function readProblemDetail(
  response: Response,
): Promise<ProblemDetail | undefined> {
  try {
    const body: unknown = await response.json()
    return isRecord(body) ? body : undefined
  } catch {
    return undefined
  }
}

function isCategoryList(value: unknown): value is CategoryList {
  return (
    isRecord(value) &&
    Array.isArray(value.items) &&
    value.items.every(
      (item) =>
        isRecord(item) &&
        typeof item.slug === 'string' &&
        typeof item.name === 'string',
    )
  )
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
