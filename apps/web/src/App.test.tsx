import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import type {
  CategoryList,
  CompletedQuizAttempt,
  JavaQuizClient,
  QuizAttemptDetails,
  QuizAttemptSummary,
  QuizQuestion,
} from '@code-arena/java-quiz-sdk'

import App from './App'
import type { AuthSession } from './auth/types'

const ATTEMPT: QuizAttemptSummary = {
  id: 'df34978a-2024-4234-9ba8-1af6cf7af994',
  status: 'IN_PROGRESS',
  difficulty: 'INTERMEDIATE',
  categories: ['OOP'],
  totalQuestions: 10,
  answeredQuestions: 0,
  startedAt: '2026-07-22T18:30:00Z',
}

const FIRST_ALTERNATIVE_ID = 'cc187215-c0f2-44a6-803f-392fbd21a2df'

const FIRST_QUESTION: QuizQuestion = {
  id: '41b42e76-7657-4d31-ae42-f47a31dd18ca',
  position: 1,
  statement: 'Qual interface representa uma lista ordenada?',
  categories: ['COLLECTIONS'],
  alternatives: [
    {
      id: FIRST_ALTERNATIVE_ID,
      text: 'List',
    },
    {
      id: 'db6f48c1-139c-4d6c-ad65-975bab231463',
      text: 'Set',
    },
  ],
  selectedAlternativeId: null,
}

const SECOND_QUESTION: QuizQuestion = {
  id: 'e8cc977f-da03-46c5-9731-8ebd959e106f',
  position: 2,
  statement: 'Qual coleção não aceita elementos duplicados?',
  categories: ['COLLECTIONS'],
  alternatives: [
    {
      id: '254f637a-bd2f-43f4-8141-697647ac31ed',
      text: 'List',
    },
    {
      id: '209dc2ca-7612-4d85-8871-5a0406dc071f',
      text: 'Set',
    },
  ],
  selectedAlternativeId: null,
}

const ATTEMPT_DETAILS: QuizAttemptDetails = {
  id: ATTEMPT.id,
  status: 'IN_PROGRESS',
  difficulty: 'INTERMEDIATE',
  totalQuestions: 2,
  answeredQuestions: 0,
  startedAt: ATTEMPT.startedAt,
  questions: [FIRST_QUESTION, SECOND_QUESTION],
}

const COMPLETED_ATTEMPT: CompletedQuizAttempt = {
  attemptId: ATTEMPT.id,
  status: 'COMPLETED',
  totalQuestions: 2,
  correctAnswers: 1,
  score: 50,
  startedAt: ATTEMPT.startedAt,
  completedAt: '2026-07-22T18:40:00Z',
  performanceByCategory: [{ category: 'COLLECTIONS', correct: 1, total: 2 }],
  questions: [
    {
      id: FIRST_QUESTION.id,
      position: 1,
      selectedAlternativeId: FIRST_ALTERNATIVE_ID,
      correctAlternativeId: FIRST_ALTERNATIVE_ID,
      correct: true,
      explanation: 'List mantém a ordem.',
    },
    {
      id: SECOND_QUESTION.id,
      position: 2,
      selectedAlternativeId: SECOND_QUESTION.alternatives[0]!.id,
      correctAlternativeId: SECOND_QUESTION.alternatives[1]!.id,
      correct: false,
      explanation: 'Set não aceita duplicados.',
    },
  ],
}

function createClient(
  categoriesResult: Promise<CategoryList> = Promise.resolve({ items: [] }),
  createAttempt: JavaQuizClient['attempts']['create'] = vi
    .fn<JavaQuizClient['attempts']['create']>()
    .mockResolvedValue(ATTEMPT),
  getAttempt: JavaQuizClient['attempts']['get'] = vi
    .fn<JavaQuizClient['attempts']['get']>()
    .mockResolvedValue(ATTEMPT_DETAILS),
  saveAnswer: JavaQuizClient['attempts']['saveAnswer'] = vi
    .fn<JavaQuizClient['attempts']['saveAnswer']>()
    .mockImplementation(async (_attemptId, questionId, request) => ({
      questionId,
      selectedAlternativeId: request.selectedAlternativeId,
      answeredAt: '2026-07-22T18:32:00Z',
    })),
  completeAttempt: JavaQuizClient['attempts']['complete'] = vi
    .fn<JavaQuizClient['attempts']['complete']>()
    .mockResolvedValue(COMPLETED_ATTEMPT),
): JavaQuizClient {
  return {
    categories: {
      list: vi.fn(() => categoriesResult),
    },
    attempts: {
      list: vi.fn<JavaQuizClient['attempts']['list']>().mockResolvedValue({
        items: [],
        page: 0,
        size: 10,
        totalItems: 0,
        totalPages: 0,
      }),
      create: createAttempt,
      get: getAttempt,
      saveAnswer,
      complete: completeAttempt,
    },
  }
}

function renderApp(
  client: JavaQuizClient,
  initialEntry = '/quiz/categories',
  session?: AuthSession,
) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialEntry]}>
        <App client={client} {...(session ? { session } : {})} />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('App', () => {
  it('ends the authenticated session from the application header', async () => {
    const user = userEvent.setup()
    const signOut = vi.fn<AuthSession['signOut']>().mockResolvedValue()
    const session: AuthSession = {
      error: null,
      getAccessToken: async () => 'access-token',
      handleUnauthorized: async () => undefined,
      isAuthenticated: true,
      isLoading: false,
      signIn: async () => undefined,
      signOut,
      user: {
        subject: 'user-subject',
        displayName: 'Code Arena Developer',
        email: 'developer@example.com',
      },
    }
    renderApp(createClient(), '/quiz/categories', session)

    await user.click(screen.getByRole('button', { name: 'Sair' }))

    expect(signOut).toHaveBeenCalledOnce()
  })

  it('shows loading and then the available categories', async () => {
    renderApp(
      createClient(
        Promise.resolve({
          items: [
            { slug: 'OOP', name: 'Orientação a objetos' },
            { slug: 'STREAMS', name: 'Streams' },
          ],
        }),
      ),
    )

    expect(screen.getByRole('status')).toHaveTextContent(
      'Carregando categorias',
    )
    expect(
      await screen.findByRole('checkbox', { name: /orientação a objetos/i }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('checkbox', { name: /streams/i }),
    ).toBeInTheDocument()
  })

  it('navigates to difficulty and preserves categories when going back', async () => {
    const user = userEvent.setup()
    renderApp(
      createClient(
        Promise.resolve({
          items: [{ slug: 'OOP', name: 'Orientação a objetos' }],
        }),
      ),
    )

    await user.click(
      await screen.findByRole('checkbox', { name: /orientação a objetos/i }),
    )
    await user.click(screen.getByRole('button', { name: /continuar/i }))
    expect(
      screen.getByRole('heading', { name: 'Qual será a intensidade?' }),
    ).toBeInTheDocument()

    await user.click(screen.getByRole('link', { name: /voltar/i }))
    expect(
      await screen.findByRole('checkbox', { name: /orientação a objetos/i }),
    ).toBeChecked()
  })

  it('creates an attempt once and navigates to its route', async () => {
    const user = userEvent.setup()
    let resolveAttempt: ((attempt: QuizAttemptSummary) => void) | undefined
    const pendingAttempt = new Promise<QuizAttemptSummary>((resolve) => {
      resolveAttempt = resolve
    })
    const createAttempt = vi
      .fn<JavaQuizClient['attempts']['create']>()
      .mockReturnValue(pendingAttempt)
    renderApp(
      createClient(Promise.resolve({ items: [] }), createAttempt),
      '/quiz/difficulty?categories=OOP&difficulty=INTERMEDIATE',
    )

    const startButton = screen.getByRole('button', { name: 'Iniciar quiz' })
    await user.click(startButton)
    expect(
      screen.getByRole('button', { name: 'Preparando quiz...' }),
    ).toBeDisabled()
    expect(createAttempt).toHaveBeenCalledOnce()
    expect(createAttempt).toHaveBeenCalledWith({
      difficulty: 'INTERMEDIATE',
      categories: ['OOP'],
    })

    resolveAttempt?.(ATTEMPT)
    expect(
      await screen.findByRole('heading', {
        name: FIRST_QUESTION.statement,
      }),
    ).toBeInTheDocument()
  })

  it('shows a creation error and allows retrying', async () => {
    const user = userEvent.setup()
    const createAttempt = vi
      .fn<JavaQuizClient['attempts']['create']>()
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(ATTEMPT)
    renderApp(
      createClient(Promise.resolve({ items: [] }), createAttempt),
      '/quiz/difficulty?categories=OOP&difficulty=INTERMEDIATE',
    )

    await user.click(screen.getByRole('button', { name: 'Iniciar quiz' }))
    expect(
      await screen.findByText('Não foi possível iniciar o quiz.'),
    ).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Iniciar quiz' }))
    expect(
      await screen.findByRole('heading', {
        name: FIRST_QUESTION.statement,
      }),
    ).toBeInTheDocument()
    expect(createAttempt).toHaveBeenCalledTimes(2)
  })

  it('shows an empty category state', async () => {
    renderApp(createClient(Promise.resolve({ items: [] })))

    expect(
      await screen.findByText('Nenhuma categoria disponível'),
    ).toBeInTheDocument()
  })

  it('shows a category error and allows retrying', async () => {
    const user = userEvent.setup()
    const list = vi
      .fn<JavaQuizClient['categories']['list']>()
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ items: [{ slug: 'OOP', name: 'OOP' }] })
    const client = createClient()
    client.categories.list = list
    renderApp(client)

    expect(
      await screen.findByText('Não foi possível carregar as categorias'),
    ).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Tentar novamente' }))
    expect(
      await screen.findByRole('checkbox', { name: /oop/i }),
    ).toBeInTheDocument()
    expect(list).toHaveBeenCalledTimes(2)
  })

  it('loads an attempt directly and restores its saved answer', async () => {
    const getAttempt = vi
      .fn<JavaQuizClient['attempts']['get']>()
      .mockResolvedValue({
        ...ATTEMPT_DETAILS,
        answeredQuestions: 1,
        questions: ATTEMPT_DETAILS.questions.map((question, index) =>
          index === 0
            ? {
                ...question,
                selectedAlternativeId: FIRST_ALTERNATIVE_ID,
              }
            : question,
        ),
      })
    renderApp(
      createClient(undefined, undefined, getAttempt),
      `/quiz-attempts/${ATTEMPT.id}/questions/1`,
    )

    expect(
      await screen.findByRole('heading', {
        name: FIRST_QUESTION.statement,
      }),
    ).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: /list/i })).toBeChecked()
    expect(getAttempt).toHaveBeenCalledWith(ATTEMPT.id, {
      signal: expect.any(AbortSignal),
    })
  })

  it('saves the selected answer before advancing', async () => {
    const user = userEvent.setup()
    const saveAnswer = vi
      .fn<JavaQuizClient['attempts']['saveAnswer']>()
      .mockResolvedValue({
        questionId: FIRST_QUESTION.id,
        selectedAlternativeId: FIRST_ALTERNATIVE_ID,
        answeredAt: '2026-07-22T18:32:00Z',
      })
    renderApp(
      createClient(undefined, undefined, undefined, saveAnswer),
      `/quiz-attempts/${ATTEMPT.id}/questions/1`,
    )

    await user.click(await screen.findByRole('radio', { name: /list/i }))
    await user.click(screen.getByRole('button', { name: /salvar e avançar/i }))

    expect(saveAnswer).toHaveBeenCalledWith(ATTEMPT.id, FIRST_QUESTION.id, {
      selectedAlternativeId: FIRST_ALTERNATIVE_ID,
    })
    expect(
      await screen.findByRole('heading', {
        name: SECOND_QUESTION.statement,
      }),
    ).toBeInTheDocument()
  })

  it('retries loading an attempt after an error', async () => {
    const user = userEvent.setup()
    const getAttempt = vi
      .fn<JavaQuizClient['attempts']['get']>()
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(ATTEMPT_DETAILS)
    renderApp(
      createClient(undefined, undefined, getAttempt),
      `/quiz-attempts/${ATTEMPT.id}/questions/1`,
    )

    expect(
      await screen.findByText('Não foi possível carregar a tentativa'),
    ).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Tentar novamente' }))
    expect(
      await screen.findByRole('heading', {
        name: FIRST_QUESTION.statement,
      }),
    ).toBeInTheDocument()
    expect(getAttempt).toHaveBeenCalledTimes(2)
  })

  it('marks the last saved answer as ready for conclusion', async () => {
    const user = userEvent.setup()
    const details = {
      ...ATTEMPT_DETAILS,
      answeredQuestions: 1,
      questions: ATTEMPT_DETAILS.questions.map((question, index) =>
        index === 0
          ? {
              ...question,
              selectedAlternativeId: FIRST_ALTERNATIVE_ID,
            }
          : question,
      ),
    }
    renderApp(
      createClient(
        undefined,
        undefined,
        vi.fn<JavaQuizClient['attempts']['get']>().mockResolvedValue(details),
      ),
      `/quiz-attempts/${ATTEMPT.id}/questions/2`,
    )

    await user.click(await screen.findByRole('radio', { name: /set/i }))
    await user.click(screen.getByRole('button', { name: /salvar resposta/i }))

    expect(
      await screen.findByText('Respostas prontas para conclusão.'),
    ).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Concluir quiz' }))
    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
    await user.click(
      screen.getByRole('button', { name: 'Confirmar conclusão' }),
    )
    expect(
      await screen.findByRole('heading', { name: '50%' }),
    ).toBeInTheDocument()
  })

  it('reloads a completed quiz result with its review', async () => {
    const completedDetails: QuizAttemptDetails = {
      ...ATTEMPT_DETAILS,
      status: 'COMPLETED',
      answeredQuestions: 2,
      questions: ATTEMPT_DETAILS.questions.map((question) => ({
        ...question,
        selectedAlternativeId:
          question.id === FIRST_QUESTION.id
            ? FIRST_ALTERNATIVE_ID
            : SECOND_QUESTION.alternatives[0]!.id,
      })),
    }
    renderApp(
      createClient(
        undefined,
        undefined,
        vi
          .fn<JavaQuizClient['attempts']['get']>()
          .mockResolvedValue(completedDetails),
      ),
      `/quiz-attempts/${ATTEMPT.id}/result`,
    )
    expect(
      await screen.findByRole('heading', { name: '50%' }),
    ).toBeInTheDocument()
    expect(screen.getByText('Set não aceita duplicados.')).toBeInTheDocument()
    expect(screen.getByText(/Resposta correta:/)).toBeInTheDocument()
  })

  it('lists quiz history and filters completed attempts', async () => {
    const user = userEvent.setup()
    const client = createClient()
    client.attempts.list = vi
      .fn<JavaQuizClient['attempts']['list']>()
      .mockResolvedValue({
        items: [
          {
            id: ATTEMPT.id,
            status: 'COMPLETED',
            difficulty: 'INTERMEDIATE',
            categories: ['OOP'],
            score: 80,
            startedAt: ATTEMPT.startedAt,
            completedAt: '2026-07-22T18:40:00Z',
          },
        ],
        page: 0,
        size: 10,
        totalItems: 1,
        totalPages: 1,
      })
    renderApp(client, '/quiz-attempts')
    expect(await screen.findByText('80%')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultado' })).toHaveAttribute(
      'href',
      `/quiz-attempts/${ATTEMPT.id}/result`,
    )
    await user.click(screen.getByRole('button', { name: 'Concluídas' }))
    expect(client.attempts.list).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 0, size: 10, status: 'COMPLETED' }),
    )
  })

  it('continues an attempt at its first unanswered question', async () => {
    const user = userEvent.setup()
    const client = createClient()
    client.attempts.list = vi
      .fn<JavaQuizClient['attempts']['list']>()
      .mockResolvedValue({
        items: [
          {
            id: ATTEMPT.id,
            status: 'IN_PROGRESS',
            difficulty: 'INTERMEDIATE',
            categories: ['OOP'],
            score: null,
            startedAt: ATTEMPT.startedAt,
            completedAt: null,
          },
        ],
        page: 0,
        size: 10,
        totalItems: 1,
        totalPages: 1,
      })
    client.attempts.get = vi
      .fn<JavaQuizClient['attempts']['get']>()
      .mockResolvedValue({
        ...ATTEMPT_DETAILS,
        questions: [
          { ...FIRST_QUESTION, selectedAlternativeId: FIRST_ALTERNATIVE_ID },
          SECOND_QUESTION,
        ],
      })
    renderApp(client, '/quiz-attempts')
    await user.click(await screen.findByRole('button', { name: 'Continuar' }))
    expect(
      await screen.findByRole('heading', { name: SECOND_QUESTION.statement }),
    ).toBeInTheDocument()
  })
})
