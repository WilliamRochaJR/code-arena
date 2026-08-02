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

  it('creates a quiz attempt with a JSON body', async () => {
    const attempt = {
      id: 'df34978a-2024-4234-9ba8-1af6cf7af994',
      status: 'IN_PROGRESS' as const,
      difficulty: 'INTERMEDIATE' as const,
      categories: ['OOP', 'COLLECTIONS'],
      totalQuestions: 10,
      answeredQuestions: 0,
      startedAt: '2026-07-22T18:30:00Z',
    }
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValue(Response.json(attempt, { status: 201 }))
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })

    await expect(
      client.attempts.create({
        difficulty: 'INTERMEDIATE',
        categories: ['OOP', 'COLLECTIONS'],
      }),
    ).resolves.toEqual(attempt)
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/quiz-attempts',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          difficulty: 'INTERMEDIATE',
          categories: ['OOP', 'COLLECTIONS'],
        }),
      }),
    )
    const request = fetchMock.mock.calls[0]?.[1]
    expect(new Headers(request?.headers).get('Content-Type')).toBe(
      'application/json',
    )
  })

  it('rejects an invalid quiz attempt response', async () => {
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi
        .fn<typeof fetch>()
        .mockResolvedValue(Response.json({ id: 'missing-fields' })),
    })

    await expect(
      client.attempts.create({
        difficulty: 'BEGINNER',
        categories: ['OOP'],
      }),
    ).rejects.toBeInstanceOf(JavaQuizInvalidResponseError)
  })

  it('lists filtered quiz attempts with pagination', async () => {
    const history = {
      items: [
        {
          id: 'attempt-id',
          status: 'COMPLETED' as const,
          difficulty: 'INTERMEDIATE' as const,
          categories: ['OOP'],
          score: 80,
          startedAt: '2026-07-22T18:30:00Z',
          completedAt: '2026-07-22T18:40:00Z',
        },
      ],
      page: 1,
      size: 10,
      totalItems: 12,
      totalPages: 2,
    }
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValue(Response.json(history))
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })
    await expect(
      client.attempts.list({ page: 1, size: 10, status: 'COMPLETED' }),
    ).resolves.toEqual(history)
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/quiz-attempts?page=1&size=10&status=COMPLETED',
      expect.anything(),
    )
  })

  it('accepts nullable result fields in an in-progress history item', async () => {
    const history = {
      items: [
        {
          id: 'attempt-id',
          status: 'IN_PROGRESS',
          difficulty: 'BEGINNER',
          categories: ['OOP'],
          score: null,
          startedAt: '2026-07-22T18:30:00Z',
          completedAt: null,
        },
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1,
    }
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi.fn<typeof fetch>().mockResolvedValue(Response.json(history)),
    })
    await expect(client.attempts.list()).resolves.toEqual(history)
  })

  it('gets a quiz attempt without exposing answer correctness', async () => {
    const attempt = {
      id: 'df34978a-2024-4234-9ba8-1af6cf7af994',
      status: 'IN_PROGRESS' as const,
      difficulty: 'INTERMEDIATE' as const,
      totalQuestions: 1,
      answeredQuestions: 0,
      startedAt: '2026-07-22T18:30:00Z',
      questions: [
        {
          id: '41b42e76-7657-4d31-ae42-f47a31dd18ca',
          position: 1,
          statement: 'Qual interface representa uma lista ordenada?',
          categories: ['COLLECTIONS'],
          alternatives: [
            {
              id: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
              text: 'List',
            },
          ],
          selectedAlternativeId: null,
        },
      ],
    }
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValue(Response.json(attempt))
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })

    await expect(client.attempts.get(attempt.id)).resolves.toEqual(attempt)
    expect(fetchMock).toHaveBeenCalledWith(
      `http://localhost:8080/api/v1/quiz-attempts/${attempt.id}`,
      expect.objectContaining({ method: 'GET' }),
    )
  })

  it('gets the question text for a completed quiz attempt', async () => {
    const completedDetails = {
      id: 'df34978a-2024-4234-9ba8-1af6cf7af994',
      status: 'COMPLETED' as const,
      difficulty: 'INTERMEDIATE' as const,
      totalQuestions: 1,
      answeredQuestions: 1,
      startedAt: '2026-07-22T18:30:00Z',
      questions: [
        {
          id: '41b42e76-7657-4d31-ae42-f47a31dd18ca',
          position: 1,
          statement: 'Qual interface representa uma lista ordenada?',
          categories: ['COLLECTIONS'],
          alternatives: [
            {
              id: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
              text: 'List',
            },
          ],
          selectedAlternativeId: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
        },
      ],
    }
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi
        .fn<typeof fetch>()
        .mockResolvedValue(Response.json(completedDetails)),
    })

    await expect(client.attempts.get(completedDetails.id)).resolves.toEqual(
      completedDetails,
    )
  })

  it('saves a quiz answer with a PUT request', async () => {
    const savedAnswer = {
      questionId: '41b42e76-7657-4d31-ae42-f47a31dd18ca',
      selectedAlternativeId: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
      answeredAt: '2026-07-22T18:32:00Z',
    }
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValue(Response.json(savedAnswer))
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })

    await expect(
      client.attempts.saveAnswer(
        'df34978a-2024-4234-9ba8-1af6cf7af994',
        savedAnswer.questionId,
        { selectedAlternativeId: savedAnswer.selectedAlternativeId },
      ),
    ).resolves.toEqual(savedAnswer)
    expect(fetchMock).toHaveBeenCalledWith(
      `http://localhost:8080/api/v1/quiz-attempts/df34978a-2024-4234-9ba8-1af6cf7af994/answers/${savedAnswer.questionId}`,
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({
          selectedAlternativeId: savedAnswer.selectedAlternativeId,
        }),
      }),
    )
  })

  it('rejects invalid quiz attempt details and saved answers', async () => {
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValueOnce(
        Response.json({ id: 'attempt-with-missing-fields' }),
      )
      .mockResolvedValueOnce(
        Response.json({ questionId: 'question-with-missing-fields' }),
      )
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })

    await expect(client.attempts.get('attempt-id')).rejects.toBeInstanceOf(
      JavaQuizInvalidResponseError,
    )
    await expect(
      client.attempts.saveAnswer('attempt-id', 'question-id', {
        selectedAlternativeId: 'alternative-id',
      }),
    ).rejects.toBeInstanceOf(JavaQuizInvalidResponseError)
  })

  it('completes a quiz attempt with a POST request', async () => {
    const completedAttempt = {
      attemptId: 'df34978a-2024-4234-9ba8-1af6cf7af994',
      status: 'COMPLETED' as const,
      totalQuestions: 1,
      correctAnswers: 1,
      score: 100,
      startedAt: '2026-07-22T18:30:00Z',
      completedAt: '2026-07-22T18:40:00Z',
      performanceByCategory: [
        { category: 'COLLECTIONS', correct: 1, total: 1 },
      ],
      questions: [
        {
          id: '41b42e76-7657-4d31-ae42-f47a31dd18ca',
          position: 1,
          selectedAlternativeId: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
          correctAlternativeId: 'cc187215-c0f2-44a6-803f-392fbd21a2df',
          correct: true,
          explanation: 'List representa uma lista ordenada.',
        },
      ],
    }
    const fetchMock = vi
      .fn<typeof fetch>()
      .mockResolvedValue(Response.json(completedAttempt))
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: fetchMock,
    })

    await expect(
      client.attempts.complete(completedAttempt.attemptId),
    ).resolves.toEqual(completedAttempt)
    expect(fetchMock).toHaveBeenCalledWith(
      `http://localhost:8080/api/v1/quiz-attempts/${completedAttempt.attemptId}/completion`,
      expect.objectContaining({ method: 'POST' }),
    )
  })

  it('rejects an invalid completed quiz response', async () => {
    const client = createJavaQuizClient({
      baseUrl: 'http://localhost:8080',
      fetch: vi.fn<typeof fetch>().mockResolvedValue(
        Response.json({
          attemptId: 'attempt-with-missing-result',
          status: 'COMPLETED',
        }),
      ),
    })

    await expect(client.attempts.complete('attempt-id')).rejects.toBeInstanceOf(
      JavaQuizInvalidResponseError,
    )
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
