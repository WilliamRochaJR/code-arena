import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import type {
  CategoryList,
  JavaQuizClient,
  QuizAttemptSummary,
} from '@code-arena/java-quiz-sdk'

import App from './App'

const ATTEMPT: QuizAttemptSummary = {
  id: 'df34978a-2024-4234-9ba8-1af6cf7af994',
  status: 'IN_PROGRESS',
  difficulty: 'INTERMEDIATE',
  categories: ['OOP'],
  totalQuestions: 10,
  answeredQuestions: 0,
  startedAt: '2026-07-22T18:30:00Z',
}

function createClient(
  categoriesResult: Promise<CategoryList> = Promise.resolve({ items: [] }),
  createAttempt: JavaQuizClient['attempts']['create'] = vi
    .fn<JavaQuizClient['attempts']['create']>()
    .mockResolvedValue(ATTEMPT),
): JavaQuizClient {
  return {
    categories: {
      list: vi.fn(() => categoriesResult),
    },
    attempts: {
      create: createAttempt,
      get: vi
        .fn<JavaQuizClient['attempts']['get']>()
        .mockRejectedValue(new Error('Unexpected attempt details request.')),
      saveAnswer: vi
        .fn<JavaQuizClient['attempts']['saveAnswer']>()
        .mockRejectedValue(new Error('Unexpected save answer request.')),
    },
  }
}

function renderApp(client: JavaQuizClient, initialEntry = '/quiz/categories') {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialEntry]}>
        <App client={client} />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('App', () => {
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
      await screen.findByRole('heading', { name: 'Seu quiz está pronto.' }),
    ).toBeInTheDocument()
    expect(screen.getByText(ATTEMPT.id)).toBeInTheDocument()
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
      await screen.findByRole('heading', { name: 'Seu quiz está pronto.' }),
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
})
