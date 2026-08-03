import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { AuthSessionContext } from './AuthContext'
import { AuthenticatedApplication } from './AuthenticatedApplication'
import type { AuthSession } from './types'

describe('AuthenticatedApplication', () => {
  it('shows session restoration state', () => {
    renderWithSession({ isLoading: true })

    expect(screen.getByRole('status')).toHaveTextContent(
      'Restaurando sua sessão',
    )
  })

  it('starts Google sign-in for an unauthenticated user', async () => {
    const user = userEvent.setup()
    const signIn = vi.fn<AuthSession['signIn']>().mockResolvedValue()
    renderWithSession({ isAuthenticated: false, signIn })

    await user.click(screen.getByRole('button', { name: 'Entrar com Google' }))

    expect(signIn).toHaveBeenCalledOnce()
  })

  it('offers a retry after an authentication error', async () => {
    const user = userEvent.setup()
    const signIn = vi.fn<AuthSession['signIn']>().mockResolvedValue()
    renderWithSession({ error: new Error('callback failed'), signIn })

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Não foi possível restaurar sua sessão',
    )
    await user.click(
      screen.getByRole('button', { name: 'Tentar entrar novamente' }),
    )
    expect(signIn).toHaveBeenCalledOnce()
  })
})

function renderWithSession(overrides: Partial<AuthSession>) {
  const session: AuthSession = {
    error: null,
    getAccessToken: async () => null,
    handleUnauthorized: async () => undefined,
    isAuthenticated: false,
    isLoading: false,
    signIn: async () => undefined,
    signOut: async () => undefined,
    user: null,
    ...overrides,
  }

  return render(
    <AuthSessionContext.Provider value={session}>
      <AuthenticatedApplication />
    </AuthSessionContext.Provider>,
  )
}
