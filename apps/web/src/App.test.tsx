import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { CategoryList, JavaQuizClient } from '@code-arena/java-quiz-sdk'

import App from './App'

function createClient(result: Promise<CategoryList>): JavaQuizClient {
  return {
    categories: {
      list: vi.fn(() => result),
    },
    attempts: {
      create: vi.fn<JavaQuizClient['attempts']['create']>(),
    },
  }
}

function renderApp(client: JavaQuizClient) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <App client={client} />
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

  it('enables continue after selecting a category', async () => {
    const user = userEvent.setup()
    renderApp(
      createClient(
        Promise.resolve({
          items: [{ slug: 'OOP', name: 'Orientação a objetos' }],
        }),
      ),
    )

    const continueButton = screen.getByRole('button', { name: /continuar/i })
    expect(continueButton).toBeDisabled()
    await user.click(
      await screen.findByRole('checkbox', { name: /orientação a objetos/i }),
    )
    expect(continueButton).toBeEnabled()
    expect(screen.getByText('1 selecionada')).toBeInTheDocument()
  })

  it('shows an empty state', async () => {
    renderApp(createClient(Promise.resolve({ items: [] })))

    expect(
      await screen.findByText('Nenhuma categoria disponível'),
    ).toBeInTheDocument()
  })

  it('shows an error and allows retrying', async () => {
    const user = userEvent.setup()
    const list = vi
      .fn<JavaQuizClient['categories']['list']>()
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ items: [{ slug: 'OOP', name: 'OOP' }] })
    renderApp({
      categories: { list },
      attempts: {
        create: vi.fn<JavaQuizClient['attempts']['create']>(),
      },
    })

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
