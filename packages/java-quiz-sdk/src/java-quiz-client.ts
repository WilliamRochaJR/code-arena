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
  CompletedQuizAttempt,
  CreateQuizAttemptRequest,
  QuizAttemptDetails,
  JavaQuizClient,
  JavaQuizClientOptions,
  ListCategoriesOptions,
  ListQuizAttemptsOptions,
  ProblemDetail,
  QuizAttemptSummary,
  QuizAttemptHistory,
  QuizDifficulty,
  SavedQuizAnswer,
  SaveQuizAnswerRequest,
} from './types.js'

const DEFAULT_TIMEOUT_MS = 10_000

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT'
  body?: unknown
  signal?: AbortSignal
}

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

  async function request(
    path: string,
    requestOptions: RequestOptions = {},
  ): Promise<unknown> {
    const controller = new AbortController()
    let timedOut = false
    const cancelRequest = () => controller.abort(requestOptions.signal?.reason)
    requestOptions.signal?.addEventListener('abort', cancelRequest, {
      once: true,
    })
    if (requestOptions.signal?.aborted) {
      controller.abort(requestOptions.signal.reason)
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
      if (requestOptions.body !== undefined) {
        headers.set('Content-Type', 'application/json')
      }
      const response = await fetchImplementation(`${baseUrl}${path}`, {
        headers,
        method: requestOptions.method ?? 'GET',
        signal: controller.signal,
        ...(requestOptions.body === undefined
          ? {}
          : { body: JSON.stringify(requestOptions.body) }),
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
      if (requestOptions.signal?.aborted || controller.signal.aborted) {
        throw new JavaQuizRequestCancelledError()
      }
      throw new JavaQuizNetworkError({ cause: error })
    } finally {
      clearTimeout(timeout)
      requestOptions.signal?.removeEventListener('abort', cancelRequest)
    }
  }

  return {
    categories: {
      async list(listOptions?: ListCategoriesOptions): Promise<CategoryList> {
        const body = await request('/api/v1/categories', {
          ...(listOptions?.signal === undefined
            ? {}
            : { signal: listOptions.signal }),
        })
        if (!isCategoryList(body)) {
          throw new JavaQuizInvalidResponseError()
        }
        return body
      },
    },
    attempts: {
      async list(
        listOptions: ListQuizAttemptsOptions = {},
      ): Promise<QuizAttemptHistory> {
        const search = new URLSearchParams()
        if (listOptions.page !== undefined)
          search.set('page', `${listOptions.page}`)
        if (listOptions.size !== undefined)
          search.set('size', `${listOptions.size}`)
        if (listOptions.status !== undefined)
          search.set('status', listOptions.status)
        const query = search.toString()
        const body = await request(
          `/api/v1/quiz-attempts${query ? `?${query}` : ''}`,
          listOptions.signal === undefined
            ? {}
            : { signal: listOptions.signal },
        )
        if (!isQuizAttemptHistory(body))
          throw new JavaQuizInvalidResponseError()
        return body
      },
      async create(
        createRequest: CreateQuizAttemptRequest,
        createOptions,
      ): Promise<QuizAttemptSummary> {
        const body = await request('/api/v1/quiz-attempts', {
          method: 'POST',
          body: createRequest,
          ...(createOptions?.signal === undefined
            ? {}
            : { signal: createOptions.signal }),
        })
        if (!isQuizAttemptSummary(body)) {
          throw new JavaQuizInvalidResponseError()
        }
        return body
      },
      async get(attemptId, getOptions): Promise<QuizAttemptDetails> {
        const body = await request(
          `/api/v1/quiz-attempts/${encodeURIComponent(attemptId)}`,
          {
            ...(getOptions?.signal === undefined
              ? {}
              : { signal: getOptions.signal }),
          },
        )
        if (!isQuizAttemptDetails(body)) {
          throw new JavaQuizInvalidResponseError()
        }
        return body
      },
      async saveAnswer(
        attemptId: string,
        questionId: string,
        saveRequest: SaveQuizAnswerRequest,
        saveOptions,
      ): Promise<SavedQuizAnswer> {
        const body = await request(
          `/api/v1/quiz-attempts/${encodeURIComponent(attemptId)}/answers/${encodeURIComponent(questionId)}`,
          {
            method: 'PUT',
            body: saveRequest,
            ...(saveOptions?.signal === undefined
              ? {}
              : { signal: saveOptions.signal }),
          },
        )
        if (!isSavedQuizAnswer(body)) {
          throw new JavaQuizInvalidResponseError()
        }
        return body
      },
      async complete(
        attemptId,
        completeOptions,
      ): Promise<CompletedQuizAttempt> {
        const body = await request(
          `/api/v1/quiz-attempts/${encodeURIComponent(attemptId)}/completion`,
          {
            method: 'POST',
            ...(completeOptions?.signal === undefined
              ? {}
              : { signal: completeOptions.signal }),
          },
        )
        if (!isCompletedQuizAttempt(body)) {
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

function isQuizAttemptSummary(value: unknown): value is QuizAttemptSummary {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    value.status === 'IN_PROGRESS' &&
    isQuizDifficulty(value.difficulty) &&
    Array.isArray(value.categories) &&
    value.categories.every((category) => typeof category === 'string') &&
    typeof value.totalQuestions === 'number' &&
    typeof value.answeredQuestions === 'number' &&
    typeof value.startedAt === 'string'
  )
}

function isQuizAttemptDetails(value: unknown): value is QuizAttemptDetails {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    (value.status === 'IN_PROGRESS' || value.status === 'COMPLETED') &&
    isQuizDifficulty(value.difficulty) &&
    typeof value.totalQuestions === 'number' &&
    typeof value.answeredQuestions === 'number' &&
    typeof value.startedAt === 'string' &&
    Array.isArray(value.questions) &&
    value.questions.every(isQuizQuestion)
  )
}

function isQuizQuestion(value: unknown): boolean {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.position === 'number' &&
    typeof value.statement === 'string' &&
    Array.isArray(value.categories) &&
    value.categories.every((category) => typeof category === 'string') &&
    Array.isArray(value.alternatives) &&
    value.alternatives.every(
      (alternative) =>
        isRecord(alternative) &&
        typeof alternative.id === 'string' &&
        typeof alternative.text === 'string',
    ) &&
    (value.selectedAlternativeId === null ||
      typeof value.selectedAlternativeId === 'string')
  )
}

function isSavedQuizAnswer(value: unknown): value is SavedQuizAnswer {
  return (
    isRecord(value) &&
    typeof value.questionId === 'string' &&
    typeof value.selectedAlternativeId === 'string' &&
    typeof value.answeredAt === 'string'
  )
}

function isCompletedQuizAttempt(value: unknown): value is CompletedQuizAttempt {
  return (
    isRecord(value) &&
    typeof value.attemptId === 'string' &&
    value.status === 'COMPLETED' &&
    typeof value.totalQuestions === 'number' &&
    typeof value.correctAnswers === 'number' &&
    typeof value.score === 'number' &&
    typeof value.startedAt === 'string' &&
    typeof value.completedAt === 'string' &&
    Array.isArray(value.performanceByCategory) &&
    value.performanceByCategory.every(isQuizCategoryPerformance) &&
    Array.isArray(value.questions) &&
    value.questions.every(isCompletedQuizQuestion)
  )
}

function isQuizCategoryPerformance(value: unknown): boolean {
  return (
    isRecord(value) &&
    typeof value.category === 'string' &&
    typeof value.correct === 'number' &&
    typeof value.total === 'number'
  )
}

function isCompletedQuizQuestion(value: unknown): boolean {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.position === 'number' &&
    typeof value.selectedAlternativeId === 'string' &&
    typeof value.correctAlternativeId === 'string' &&
    typeof value.correct === 'boolean' &&
    typeof value.explanation === 'string'
  )
}

function isQuizAttemptHistory(value: unknown): value is QuizAttemptHistory {
  return (
    isRecord(value) &&
    Array.isArray(value.items) &&
    value.items.every(isQuizAttemptHistoryItem) &&
    typeof value.page === 'number' &&
    typeof value.size === 'number' &&
    typeof value.totalItems === 'number' &&
    typeof value.totalPages === 'number'
  )
}

function isQuizAttemptHistoryItem(value: unknown): boolean {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    (value.status === 'IN_PROGRESS' || value.status === 'COMPLETED') &&
    isQuizDifficulty(value.difficulty) &&
    Array.isArray(value.categories) &&
    value.categories.every((item) => typeof item === 'string') &&
    (value.score === null || typeof value.score === 'number') &&
    typeof value.startedAt === 'string' &&
    (value.completedAt === null || typeof value.completedAt === 'string')
  )
}

function isQuizDifficulty(value: unknown): value is QuizDifficulty {
  return (
    value === 'BEGINNER' || value === 'INTERMEDIATE' || value === 'ADVANCED'
  )
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
